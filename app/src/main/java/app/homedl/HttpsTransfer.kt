package app.homedl

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

/** Bounded HTTPS transfers; a redirect is revalidated before any request is sent. */
object HttpsTransfer {
    fun active(token: AtomicBoolean, deadline: Long) {
        if (token.get() || Thread.currentThread().isInterrupted) throw InterruptedException()
        check(System.nanoTime() < deadline) { "network timeout" }
    }

    fun bytes(address: String, limit: Int, token: AtomicBoolean, deadline: Long,
              allowed: (String) -> Boolean, headers: Map<String, String> = emptyMap(),
              form: ByteArray? = null, cookies: java.net.CookieManager? = null): ByteArray {
        val out = ByteArrayOutputStream()
        stream(address, limit.toLong(), token, deadline, allowed, out, headers, form, cookies)
        return out.toByteArray()
    }

    fun stream(address: String, limit: Long, token: AtomicBoolean, deadline: Long,
               allowed: (String) -> Boolean, output: OutputStream,
               headers: Map<String, String> = emptyMap(), form: ByteArray? = null,
               cookies: java.net.CookieManager? = null,
               progress: (Long, Long) -> Unit = { _, _ -> }): Long {
        var url = URL(address)
        repeat(5) {
            active(token, deadline)
            require(url.protocol == "https" && url.userInfo == null && url.port in listOf(-1, 443) && allowed(url.host.lowercase())) {
                "Untrusted HTTPS origin"
            }
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("User-Agent", "Home-DL/0.2")
                connection.setRequestProperty("Accept-Encoding", "identity")
                headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
                cookies?.get(url.toURI(), emptyMap())?.forEach { (name, values) ->
                    connection.setRequestProperty(name, values.joinToString("; "))
                }
                if (form != null) {
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    connection.setFixedLengthStreamingMode(form.size)
                    connection.outputStream.use { it.write(form) }
                }
                val code = connection.responseCode
                AnalysisTrace.http(code, connection.contentType, connection.contentLengthLong)
                // CookieManager is scoped to one analysis, never global or persisted.
                cookies?.put(url.toURI(), connection.headerFields.filterKeys { it != null })
                if (code in listOf(301, 302, 303, 307, 308)) {
                    require(form == null) { "Public query redirected; login or access change" }
                    url = URL(url, connection.getHeaderField("Location") ?: error("Redirect missing"))
                    if (url.path.startsWith("/accounts/login") || url.path.startsWith("/challenge")) {
                        error("Login required or human verification requested")
                    }
                } else {
                    if (code != 200) throw HttpStatusException(code)
                    val length = connection.contentLengthLong
                    require(length <= limit) { "Response too large" }
                    var total = 0L
                    connection.inputStream.use { input ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            active(token, deadline)
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= limit) { "Response too large" }
                            output.write(buffer, 0, count)
                            progress(total, length)
                        }
                    }
                    require(length < 0 || total == length) { "Incomplete response" }
                    AnalysisTrace.bytes(total)
                    return total
                }
            } finally { connection.disconnect() }
        }
        error("Too many redirects")
    }
}
