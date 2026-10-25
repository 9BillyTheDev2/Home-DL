package app.homedl

import org.json.JSONObject
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

/** Read the public photo data exposed by TikTok's canonical post page. No account session. */
object TikTokPublicPhotos {
    private val pageHosts = setOf("www.tiktok.com", "tiktok.com", "vm.tiktok.com", "vt.tiktok.com", "m.tiktok.com")
    private val script = Regex("""<script\b[^>]*\bid=["']__UNIVERSAL_DATA_FOR_REHYDRATION__["'][^>]*>(.*?)</script>""",
        setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))

    fun canonical(source: String): Pair<String, String> {
        val uri = URI(source)
        require(uri.scheme == "https" && uri.host?.lowercase() in pageHosts && uri.userInfo == null &&
            uri.port in listOf(-1, 443)) { "Unsupported TikTok URL" }
        val match = requireNotNull(Regex("^/(@[A-Za-z0-9_.-]*|share)/(?:photo|video)/([0-9]{1,25})/?$")
            .matchEntire(uri.path)) { "Unsupported TikTok post URL" }
        return "https://www.tiktok.com/${match.groupValues[1]}/video/${match.groupValues[2]}" to match.groupValues[2]
    }

    fun cdn(host: String) = listOf("tiktokcdn.com", "tiktokcdn-us.com", "tiktokcdn-eu.com")
        .any { host == it || host.endsWith(".$it") }

    fun analyze(source: String, token: AtomicBoolean): List<Media> {
        val deadline = System.nanoTime() + 60_000_000_000L
        fun read(url: String, resolved: (String) -> Unit = {}) = HttpsTransfer.bytes(url,
            6 * 1024 * 1024, token, deadline, { it in pageHosts }, resolved = resolved).toString(Charsets.UTF_8)
        val known = runCatching { canonical(source) }.getOrNull()
        val pair: Pair<String, String>
        val html: String
        if (known != null) {
            pair = known
            html = AnalysisTrace.step(AnalysisTrace.Step.TIKTOK_PUBLIC_PHOTOS) { read(pair.first) }
        } else {
            var finalUrl = source
            val initial = AnalysisTrace.step(AnalysisTrace.Step.TIKTOK_RESOLVE) { read(source) { finalUrl = it } }
            pair = canonical(finalUrl)
            html = if (URI(finalUrl).path == URI(pair.first).path) initial
                else AnalysisTrace.step(AnalysisTrace.Step.TIKTOK_PUBLIC_PHOTOS) { read(pair.first) }
        }
        return AnalysisTrace.step(AnalysisTrace.Step.MEDIA_PARSE) { parse(source, pair.second, html) }
            .also { AnalysisTrace.media(it) }
    }

    fun parse(source: String, expectedId: String, html: String): List<Media> {
        val raw = script.find(html)?.groupValues?.get(1) ?: error("No public TikTok photo data in page")
        val root = try { JSONObject(raw) }
            catch (_: org.json.JSONException) { throw EngineOutputException(raw.length) }
        val detail = root.optJSONObject("__DEFAULT_SCOPE__")?.optJSONObject("webapp.video-detail")
            ?: error("No public TikTok photo detail")
        check(detail.optInt("statusCode", -1) == 0) { "TikTok did not expose public post data" }
        val item = detail.optJSONObject("itemInfo")?.optJSONObject("itemStruct")
            ?: error("No public TikTok photo item")
        check(item.optString("id") == expectedId) { "TikTok photo response identity mismatch" }
        val images = item.optJSONObject("imagePost")?.optJSONArray("images")
            ?: error("No public TikTok photo collection")
        require(images.length() in 1..50) { "Unsupported TikTok image count" }
        return (0 until images.length()).map { index ->
            val image = images.getJSONObject(index)
            val urls = image.optJSONObject("imageURL")?.optJSONArray("urlList")
            val url = (0 until (urls?.length() ?: 0)).mapNotNull { urls?.optString(it) }
                .firstOrNull { trustedImage(it) } ?: error("No trusted TikTok photo URL")
            val width = image.optInt("imageWidth", 0)
            val height = image.optInt("imageHeight", 0)
            Media(source, null, "Photo ${index + 1}", url,
                if (width > 0 && height > 0) "$width × $height" else "Meilleure version exposée par TikTok",
                directUrl = url, kind = MediaKind.PHOTO)
        }
    }

    fun trustedImage(raw: String) = runCatching {
        val uri = URI(raw)
        uri.scheme == "https" && uri.userInfo == null && uri.port in listOf(-1, 443) && cdn(uri.host.orEmpty())
    }.getOrDefault(false)
}
