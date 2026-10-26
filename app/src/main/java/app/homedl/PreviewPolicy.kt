package app.homedl

import java.net.URI

/** Image-only preview policy; redirects use the same CDN allowlist. */
object PreviewPolicy {
    const val MAX_BYTES = 8 * 1024 * 1024

    fun cdn(host: String): Boolean = listOf(
        "cdninstagram.com", "fbcdn.net", "twimg.com", "tiktokcdn.com",
        "tiktokcdn-us.com", "tiktokcdn-eu.com", "byteoversea.com", "ibytedtos.com",
        "ytimg.com", "ggpht.com", "googleusercontent.com"
    ).any { host == it || host.endsWith("." + it) }

    fun valid(address: String): Boolean = runCatching {
        val uri = URI(address)
        uri.scheme == "https" && uri.userInfo == null && uri.port in listOf(-1, 443) &&
            cdn(uri.host.orEmpty().lowercase())
    }.getOrDefault(false)

    fun candidates(media: Media): List<String> =
        listOfNotNull(media.preview, media.directUrl.takeIf { media.kind == MediaKind.PHOTO })
            .distinct().filter(::valid)

    fun headers(source: String): Map<String, String> {
        val referer = when (Links.platform(source)) {
            "Instagram" -> "https://www.instagram.com/"
            "TikTok" -> "https://www.tiktok.com/"
            "X" -> "https://x.com/"
            else -> null
        }
        return mapOf("Accept" to "image/*") + if (referer == null) emptyMap() else mapOf("Referer" to referer)
    }

    fun sample(width: Int, height: Int): Int {
        require(width in 1..100_000 && height in 1..100_000) { "Invalid preview dimensions" }
        var value = 1
        while (width / value > 720 || height / value > 720) value *= 2
        return value
    }
}
