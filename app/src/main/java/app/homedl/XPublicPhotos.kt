package app.homedl

import org.json.JSONObject
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

/** Public X embed metadata, used only when the video extractor cannot handle the post. */
object XPublicPhotos {
    fun reference(source: String): String {
        val uri = URI(source)
        require(uri.scheme == "https" && uri.userInfo == null && uri.port in listOf(-1, 443) &&
            uri.host?.lowercase() in setOf("x.com", "www.x.com", "twitter.com", "www.twitter.com", "mobile.twitter.com")) {
            "Unsupported X URL"
        }
        return Regex("^/(?:[A-Za-z0-9_]+/status|i/web/status)/([0-9]{1,20})(?:/(?:photo|video)/[1-4])?/?$")
            .matchEntire(uri.path)?.groupValues?.get(1) ?: error("Unsupported X URL")
    }

    // Public embed query parameter derived from the post ID, not an account credential.
    fun embedToken(id: String): String {
        require(Regex("[0-9]{1,20}").matches(id))
        val number = id.toDouble() / 1e15 * Math.PI
        val whole = number.toLong()
        var fraction = number - whole
        val digits = "0123456789abcdefghijklmnopqrstuvwxyz"
        return buildString {
            append(whole.toString(36))
            repeat(12) {
                fraction *= 36
                val digit = fraction.toInt().coerceIn(0, 35)
                append(digits[digit])
                fraction -= digit
            }
        }.replace("0", "")
    }

    fun analyze(source: String, token: AtomicBoolean): List<Media> =
        AnalysisTrace.step(AnalysisTrace.Step.X_PUBLIC_PHOTOS) {
            val id = reference(source)
            val text = HttpsTransfer.bytes(
                "https://cdn.syndication.twimg.com/tweet-result?id=$id&token=${embedToken(id)}",
                2 * 1024 * 1024, token, System.nanoTime() + 30_000_000_000L,
                { it == "cdn.syndication.twimg.com" }, mapOf("Accept" to "application/json")
            ).toString(Charsets.UTF_8)
            val json = try { JSONObject(text) }
                catch (_: org.json.JSONException) { throw EngineOutputException(text.length) }
            parse(source, json).also { AnalysisTrace.media(it) }
        }

    fun parse(source: String, json: JSONObject): List<Media> {
        val id = reference(source)
        check(json.optString("id_str") == id) { "X photo response identity mismatch" }
        val details = json.optJSONArray("mediaDetails") ?: error("No public photo data from X")
        require(details.length() in 1..50) { "Unsupported X media count" }
        // Never silently omit a video or present its thumbnail as a photo.
        check((0 until details.length()).all { details.optJSONObject(it)?.optString("type") == "photo" }) {
            "Mixed X media not supported by photo fallback"
        }
        return (0 until details.length()).map { index ->
            val item = details.getJSONObject(index)
            val direct = imageUrl(item.optString("media_url_https"), "orig")
            val info = item.optJSONObject("original_info")
            val width = info?.optInt("width", 0) ?: 0
            val height = info?.optInt("height", 0) ?: 0
            Media(source, null, "Photo ${index + 1}", imageUrl(item.optString("media_url_https"), "small"),
                if (width > 0 && height > 0) "$width × $height" else "Version originale exposée par X",
                directUrl = direct, kind = MediaKind.PHOTO)
        }
    }

    fun imageUrl(raw: String, size: String): String {
        require(size in setOf("orig", "small"))
        val uri = URI(raw)
        require(uri.scheme == "https" && uri.host == "pbs.twimg.com" && uri.userInfo == null &&
            uri.port in listOf(-1, 443) && uri.fragment == null) { "Untrusted X image origin" }
        val match = requireNotNull(Regex("^(/media/[A-Za-z0-9_-]+)\\.(jpg|jpeg|png|webp)$").matchEntire(uri.path)) {
            "Unsupported X image path"
        }
        return "https://pbs.twimg.com${match.groupValues[1]}?format=${match.groupValues[2]}&name=$size"
    }
}
