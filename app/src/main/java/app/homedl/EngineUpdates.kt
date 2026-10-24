package app.homedl

import android.content.Context
import android.util.AtomicFile
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

    fun version(): String = prefs.getString("version", null) ?: "embarqué / bibliothèque 0.18.1"

    /** A verified recent engine remains available even when GitHub DNS is unavailable. */
    fun installBundled() {
        val target = File(context.noBackupFilesDir, "youtubedl-android/yt-dlp/yt-dlp")
        val current = prefs.getString("version", "").orEmpty()
        if (target.isFile && current >= EngineManifest.VERSION) return
        val bytes = context.assets.open("engine/yt-dlp").use { it.readBytes() }
        require(sha256(bytes) == EngineManifest.SHA256) { "bundled engine checksum mismatch" }
        replace(target, bytes) { }
        prefs.edit().putString("version", EngineManifest.VERSION).apply()
    }

    private fun sha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        .joinToString("") { "%02x".format(it.toInt() and 255) }

    private fun replace(target: File, bytes: ByteArray, active: () -> Unit) {
        val atomic = AtomicFile(target)
        val output = atomic.startWrite()
        try {
            output.write(bytes)
            active()
            atomic.finishWrite(output)
        } catch (error: Exception) {
            atomic.failWrite(output)
            throw error
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
        // Use the official releases redirect: no api.github.com dependency on the phone.
        val tag = latestTag(token, started)
        val target = File(context.noBackupFilesDir, "youtubedl-android/yt-dlp/yt-dlp")
        if (prefs.getString("version", null) == tag && target.isFile) {
            prefs.edit().putLong("success", now).apply()
            return "Moteur déjà à jour."
        }
        val base = "https://github.com/yt-dlp/yt-dlp/releases/download/" + tag + "/"
        val sums = read(base + "SHA2-256SUMS", 64 * 1024, token, started).toString(Charsets.UTF_8)
        val digest = EngineManifest.checksum(sums)
        val bytes = read(base + "yt-dlp", 20 * 1024 * 1024, token, started)
        require(bytes.isNotEmpty() && sha256(bytes).equals(digest, true)) { "engine checksum mismatch" }
        checkActive(token, started)
        // AtomicFile keeps the previous engine until the replacement is complete.
        replace(target, bytes) { checkActive(token, started) }
        prefs.edit().putString("version", tag).putLong("success", now).apply()
        return "Moteur mis à jour."
    }

    private fun checkActive(token: AtomicBoolean, started: Long) {
        if (token.get() || Thread.currentThread().isInterrupted) throw CancellationException()
        if (System.nanoTime() - started > 90_000_000_000L) error("engine update timeout")
    }

    private fun latestTag(token: AtomicBoolean, started: Long): String {
        checkActive(token, started)
        val origin = URL("https://github.com/yt-dlp/yt-dlp/releases/latest")
        val connection = origin.openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("User-Agent", "Home-DL")
            require(connection.responseCode in listOf(301, 302, 303, 307, 308)) { "engine release redirect missing" }
            val destination = URL(origin, connection.getHeaderField("Location") ?: error("engine redirect missing"))
            require(destination.protocol == "https" && destination.host == "github.com" && destination.userInfo == null)
            val tag = Regex("^/yt-dlp/yt-dlp/releases/tag/([0-9]{4}\\.[0-9]{2}\\.[0-9]{2})$")
                .matchEntire(destination.path)?.groupValues?.get(1) ?: error("engine release tag invalid")
            checkActive(token, started)
            return tag
        } finally { connection.disconnect() }
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
                connection.setRequestProperty("Accept", "application/octet-stream")
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

    private fun trusted(host: String) = host == "github.com" ||
        host == "release-assets.githubusercontent.com" || host == "objects.githubusercontent.com"
}
