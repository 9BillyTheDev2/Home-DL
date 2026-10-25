package app.homedl

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

data class AppRelease(val versionCode: Long, val versionName: String, val url: String,
                      val sha256: String, val size: Long)

class AppUpdates(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
    private val apk get() = File(context.cacheDir, "updates/Home-DL.apk")
    private val metadata = "https://github.com/9BillyTheDev2/Home-DL/releases/latest/download/update.json"

    fun enabled() = prefs.getBoolean("automatic", true)
    fun setEnabled(value: Boolean) { prefs.edit().putBoolean("automatic", value).apply() }
    fun due(): Boolean {
        val now = System.currentTimeMillis()
        val previous = prefs.getLong("attempt", 0)
        return enabled() && (previous == 0L || now < previous || now - previous >= 6 * 60 * 60 * 1000L)
    }

    @Suppress("DEPRECATION")
    private fun installed() = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)

    fun check(token: AtomicBoolean): AppRelease? {
        prefs.edit().putLong("attempt", System.currentTimeMillis()).apply()
        val text = try {
            HttpsTransfer.bytes(metadata, 32 * 1024, token, System.nanoTime() + 45_000_000_000L,
                ::trusted).toString(Charsets.UTF_8)
        } catch (e: IllegalStateException) {
            if (e.message == "HTTP Error 404") return null
            throw e
        }
        val release = try { manifest(JSONObject(text)) }
            catch (_: org.json.JSONException) { error("Invalid application update manifest") }
        return release.takeIf { it.versionCode > installed().longVersionCode }
    }

    fun download(release: AppRelease, token: AtomicBoolean, progress: (Int) -> Unit): File {
        val directory = apk.parentFile!!.apply { mkdirs() }
        val part = File(directory, "Home-DL.part")
        apk.delete()
        var lastPercent = 0
        progress(0)
        try {
            part.outputStream().use { output ->
                val total = HttpsTransfer.stream(release.url, release.size, token,
                    System.nanoTime() + 180_000_000_000L, ::trusted, output) { count, _ ->
                    // The verified release manifest provides a size even without Content-Length.
                    val percent = (count * 100 / release.size).toInt().coerceIn(0, 100)
                    if (percent != lastPercent) { lastPercent = percent; progress(percent) }
                }
                require(total == release.size) { "Incomplete application update" }
            }
            HttpsTransfer.active(token, Long.MAX_VALUE)
            val digest = MessageDigest.getInstance("SHA-256")
            part.inputStream().use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    HttpsTransfer.active(token, Long.MAX_VALUE)
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                }
            }
            val actual = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
            require(actual.equals(release.sha256, true)) { "Application update checksum mismatch" }
            verify(part, release.versionCode)
            HttpsTransfer.active(token, Long.MAX_VALUE)
            require(part.renameTo(apk)) { "Unable to store application update" }
            return apk
        } finally { part.delete() }
    }

    @Suppress("DEPRECATION")
    private fun verify(file: File, expectedVersion: Long? = null) {
        val candidate = context.packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
            ?: error("Invalid APK")
        val current = installed()
        val before = current.signingInfo?.apkContentsSigners?.map { it.toCharsString() }?.toSet().orEmpty()
        val after = candidate.signingInfo?.apkContentsSigners?.map { it.toCharsString() }?.toSet().orEmpty()
        UpdateIdentity.requireCompatible(context.packageName, current.longVersionCode, before,
            candidate.packageName, candidate.longVersionCode, after, expectedVersion)
    }

    fun hasDownloaded(): Boolean {
        if (!apk.isFile) return false
        return try { verify(apk); true }
        catch (_: Exception) { apk.delete(); false }
    }

    fun install(activity: Activity) {
        verify(apk)
        if (!activity.packageManager.canRequestPackageInstalls()) {
            activity.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + context.packageName)))
            return
        }
        val uri = FileProvider.getUriForFile(activity, context.packageName + ".updates", apk)
        activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("Home-DL", uri)
        })
    }

    companion object {
        fun trusted(host: String) = host == "github.com" || host == "release-assets.githubusercontent.com" ||
            host == "objects.githubusercontent.com"

        fun manifest(json: JSONObject): AppRelease {
            val code = json.getLong("versionCode")
            val name = json.getString("versionName")
            val url = json.getString("url")
            val hash = json.getString("sha256")
            val size = json.getLong("size")
            val parsed = java.net.URI(url)
            require(code > 0 && name.length in 1..80 && size in 1..(150L * 1024 * 1024)) { "Invalid update metadata" }
            require(Regex("[a-fA-F0-9]{64}").matches(hash)) { "Invalid update checksum" }
            require(parsed.scheme == "https" && parsed.host == "github.com" && parsed.userInfo == null &&
                parsed.port == -1 && parsed.query == null && parsed.fragment == null &&
                Regex("/9BillyTheDev2/Home-DL/releases/download/v[0-9A-Za-z._-]+/Home-DL\\.apk").matches(parsed.path)) {
                "Invalid update origin"
            }
            return AppRelease(code, name, url, hash, size)
        }
    }
}
