package app.homedl

/** One bounded, memory-only trace per worker operation. Never accepts URLs or server text. */
object AnalysisTrace {
    enum class Step { ENGINE_INIT, INSTAGRAM_REFERENCE, INSTAGRAM_SESSION, INSTAGRAM_ACCESS,
        INSTAGRAM_SHORTCODE, INSTAGRAM_POLARIS, ENGINE_ANALYZE, MEDIA_PARSE, MEDIA_DOWNLOAD }
    private class Session(val started: Long = System.nanoTime(), val lines: MutableList<String> = mutableListOf())
    private val current = ThreadLocal<Session>()

    fun start() { current.set(Session()) }
    fun clear() { current.remove() }

    private fun add(message: String) {
        val session = current.get() ?: return
        if (session.lines.size < 64) {
            val elapsed = (System.nanoTime() - session.started) / 1_000_000
            session.lines.add("+${elapsed}ms $message")
        }
    }

    fun <T> step(step: Step, action: () -> T): T {
        add("${step.name} BEGIN")
        try {
            val result = action()
            add("${step.name} OK")
            return result
        } catch (error: Exception) {
            // Exception messages may contain private server responses: record a fixed category only.
            val kind = when (error) {
                is HttpStatusException -> "HTTP_${error.status}"
                is InterruptedException -> "STOP"
                is java.net.UnknownHostException -> "DNS"
                is java.net.SocketTimeoutException -> "TIMEOUT"
                is EngineOutputException -> "JSON"
                else -> "ERROR"
            }
            add("${step.name} FAIL $kind")
            throw error
        }
    }

    fun http(code: Int, contentType: String?, length: Long) {
        val type = when (contentType?.substringBefore(';')?.trim()?.lowercase()) {
            "application/json" -> "JSON"
            "text/html" -> "HTML"
            "image/jpeg", "image/png", "image/webp" -> "IMAGE"
            "video/mp4" -> "VIDEO"
            else -> "OTHER"
        }
        add("HTTP status=$code type=$type announcedBytes=${length.coerceAtLeast(-1)}")
    }

    fun bytes(count: Long) { add("READ bytes=${count.coerceAtLeast(0)}") }
    fun media(items: List<Media>) {
        add("MEDIA total=${items.size} photos=${items.count { it.kind == MediaKind.PHOTO }} videos=${items.count { it.kind == MediaKind.VIDEO }}")
    }
    fun snapshot(): String = current.get()?.lines?.joinToString("\n").orEmpty()
}

class HttpStatusException(val status: Int) : IllegalStateException("HTTP Error $status")
