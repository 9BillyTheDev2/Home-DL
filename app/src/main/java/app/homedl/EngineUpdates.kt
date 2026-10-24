package app.homedl

import android.content.Context
import android.util.AtomicFile
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean

/** Updates only the interpreted yt-dlp engine, never the APK or native libraries. */
class EngineUpdates(private val context: Context) {
    private val prefs = context.getSharedPreferences("engine_updates", Context.MODE_PRIVATE)

    fun recover() {
        val target = File(context.noBackupFilesDir, "youtubedl-android/yt-dlp/yt-dlp")
        if (File(target.path + ".bak").exists() || File(target.path + ".new").exists()) {
            AtomicFile(target).openRead().use { /* Recover an interrupted replacement before engine initialization. */ }
        }
    }

    fun enabled(): Boolean = prefs.getBoolean("automatic", true)
    fun setEnabled(value: Boolean) { prefs.edit().putBoolean("automatic", value).apply() }

    fun due(now: Long = System.currentTimeMillis()): Boolean {
        val lastAttempt = prefs.getLong("attempt", 0)
        val lastSuccess = prefs.getLong("success", 0)
        return enabled() && elapsed(now, lastAttempt, 6 * 60 * 60 * 1000L) &&
            elapsed(now, lastSuccess, 24 * 60 * 60 * 1000L)
    }

    private fun elapsed(now: Long, previous: Long, delay: Long) =
        previous == 0L || now < previous || now - previous >= delay

    fun check(token: AtomicBoolean): String {
        val started = System.nanoTime()
        val now = System.currentTimeMillis()
        prefs.edit().putLong("attempt", now).apply()
        val metadata = JSONObject(read(
            "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest",
            1024 * 1024, token, started
        ).toString(Charsets.UTF_8))
        require(!metadata.optBoolean("draft") && !metadata.optBoolean("prerelease"))
        val tag = metadata.getString("tag_name")
        val target = File(context.noBackupFilesDir, "youtubedl-android/yt-dlp/yt-dlp")
        if (prefs.getString("version", null) == tag && target.isFile) {
            prefs.edit().putLong("success", now).apply()
            return "Moteur déjà à jour."
        }
        val assets = metadata.getJSONArray("assets")
        val asset = (0 until assets.length()).map { assets.getJSONObject(it) }
            .firstOrNull { it.optString("name") == "yt-dlp" }
            ?: error("engine asset missing")
        val digest = asset.optString("digest")
        require(Regex("sha256:[0-9a-fA-F]{64}").matches(digest)) { "engine checksum unavailable" }
        val expectedSize = asset.getLong("size")
        require(expectedSize in 1..(20L * 1024 * 1024)) { "engine size invalid" }
        val downloadUrl = asset.getString("browser_download_url")
        val initial = URL(downloadUrl)
        require(initial.protocol == "https" && initial.host == "github.com" &&
            initial.path.startsWith("/yt-dlp/yt-dlp/releases/download/")) { "engine origin invalid" }
        val bytes = read(downloadUrl, expectedSize.toInt(), token, started)
        require(bytes.size.toLong() == expectedSize) { "engine size mismatch" }
        val actual = MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        require(actual.equals(digest.removePrefix("sha256:"), true)) { "engine checksum mismatch" }
        checkActive(token, started)
        // AtomicFile keeps the previous engine until the replacement is complete.
        val atomic = AtomicFile(target)
        val output = atomic.startWrite()
        try {
            output.write(bytes)
            checkActive(token, started)
            atomic.finishWrite(output)
        } catch (error: Exception) {
            atomic.failWrite(output)
            throw error
        }
        prefs.edit().putString("version", tag).putLong("success", now).apply()
        return "Moteur mis à jour."
    }

    private fun checkActive(token: AtomicBoolean, started: Long) {
        if (token.get() || Thread.currentThread().isInterrupted) throw CancellationException()
        if (System.nanoTime() - started > 90_000_000_000L) error("engine update timeout")
    }

    private fun read(address: String, limit: Int, token: AtomicBoolean, started: Long): ByteArray {
        var url = URL(address)
        repeat(6) {
            checkActive(token, started)
            require(url.protocol == "https" && url.userInfo == null && trusted(url.host)) { "engine origin invalid" }
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("User-Agent", "Home-DL")
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                val code = connection.responseCode
                if (code in listOf(301, 302, 303, 307, 308)) {
                    url = URL(url, connection.getHeaderField("Location") ?: error("engine redirect missing"))
                } else {
                    require(code == 200) { "engine HTTP $code" }
                    require(connection.contentLengthLong <= limit) { "engine response too large" }
                    return connection.inputStream.use { input ->
                        val result = ByteArrayOutputStream()
                        val buffer = ByteArray(16 * 1024)
                        while (true) {
                            checkActive(token, started)
                            val count = input.read(buffer)
                            if (count < 0) break
                            require(result.size() + count <= limit) { "engine response too large" }
                            result.write(buffer, 0, count)
                        }
                        result.toByteArray()
                    }
                }
            } finally { connection.disconnect() }
        }
        error("engine too many redirects")
    }

    private fun trusted(host: String) = host == "api.github.com" || host == "github.com" ||
        host == "release-assets.githubusercontent.com" || host == "objects.githubusercontent.com"
}
