package app.homedl

import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicBoolean

/** Anonymous public Instagram flow, no account cookies and no third-party service. */
object InstagramPublic {
    private const val ROOT = "https://www.instagram.com/"
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    fun reference(url: String): String {
        val uri = URI(url)
        require(uri.scheme == "https" && (uri.host == "instagram.com" || uri.host == "www.instagram.com")) {
            "Unsupported Instagram URL"
        }
        return Regex("^/(?:p|reel|reels|tv)/([A-Za-z0-9_-]{1,28})/?$")
            .matchEntire(uri.path)?.groupValues?.get(1) ?: error("Unsupported Instagram URL")
    }

    fun mediaId(shortcode: String): String {
        require(shortcode.isNotEmpty() && shortcode.length <= 28)
        var result = BigInteger.ZERO
        for (c in shortcode) {
            val digit = ALPHABET.indexOf(c)
            require(digit >= 0)
            result = result.shiftLeft(6).add(BigInteger.valueOf(digit.toLong()))
        }
        return result.toString()
    }

    fun cdn(host: String) = host.endsWith(".cdninstagram.com") || host.endsWith(".fbcdn.net")

    fun analyze(source: String, token: AtomicBoolean): List<Media> {
        val shortcode = AnalysisTrace.step(AnalysisTrace.Step.INSTAGRAM_REFERENCE) { reference(source) }
        val id = mediaId(shortcode)
        val deadline = System.nanoTime() + 90_000_000_000L
        val cookies = CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER)
        fun read(url: String, headers: Map<String, String> = emptyMap(), form: ByteArray? = null) =
            HttpsTransfer.bytes(url, 8 * 1024 * 1024, token, deadline,
                { it == "www.instagram.com" }, headers, form, cookies).toString(Charsets.UTF_8)
        val html = AnalysisTrace.step(AnalysisTrace.Step.INSTAGRAM_SESSION) { read(ROOT) }
        val lsd = Regex("""\["LSD",\[\],\{"token":"([^"]+)"""").find(html)?.groupValues?.get(1)
            ?: run {
                val eqmc = Regex("""<script\b[^>]*\bid="__eqmc"[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL)
                    .find(html)?.groupValues?.get(1)
                eqmc?.let { runCatching { JSONObject(it).optString("l") }.getOrNull() }
            }
        check(!lsd.isNullOrBlank()) { "Anonymous session unavailable; extraction failed" }
        val csrf = cookies.cookieStore.cookies.firstOrNull { it.name == "csrftoken" }?.value
        check(!csrf.isNullOrBlank()) { "Anonymous CSRF cookie unavailable; extraction failed" }
        val headers = mapOf("X-IG-App-ID" to "936619743392459", "Origin" to ROOT.removeSuffix("/"),
            "X-CSRFToken" to csrf, "Accept" to "application/json", "X-Requested-With" to "XMLHttpRequest")
        val ruling = try { AnalysisTrace.step(AnalysisTrace.Step.INSTAGRAM_ACCESS) {
            parse(read(ROOT + "api/v1/web/get_ruling_for_content/?content_type=MEDIA&target_id=" + id, headers))
        }
        } catch (error: HttpStatusException) {
            if (error.status != 404) throw error
            null // Missing auxiliary response does not establish that the publication is missing.
        }
        check(ruling == null || ruling.optString("status") == "ok") {
            // Do not reproduce server titles/account information in the exception.
            "Public access not granted; login required or audience restricted"
        }
        fun query(strategy: InstagramQuery): JSONObject {
            val variables = when (strategy) {
                InstagramQuery.SHORTCODE -> JSONObject().put("shortcode", shortcode)
                    .put("__relay_internal__pv__PolarisAIGMMediaWebLabelEnabledrelayprovider", false)
                InstagramQuery.POLARIS -> JSONObject().put("media_id", id)
            }
            val fields = mapOf("lsd" to lsd, "fb_api_caller_class" to "RelayModern",
                "fb_api_req_friendly_name" to strategy.friendlyName, "server_timestamps" to "true",
                "variables" to variables.toString(), "doc_id" to strategy.document)
            val form = fields.entries.joinToString("&") { (key, value) ->
                URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8")
            }.toByteArray(Charsets.UTF_8)
            val response = parse(read(ROOT + "api/graphql", headers + mapOf(
                "X-FB-LSD" to lsd, "X-FB-Friendly-Name" to strategy.friendlyName,
                "Referer" to ROOT + "p/" + shortcode + "/"), form))
            return InstagramResponse.product(response, strategy, shortcode, id)
        }
        val product = try { AnalysisTrace.step(AnalysisTrace.Step.INSTAGRAM_SHORTCODE) { query(InstagramQuery.SHORTCODE) } }
        catch (first: InstagramFailure) {
            if (!InstagramResponse.canTryAlternative(first)) throw first
            try { AnalysisTrace.step(AnalysisTrace.Step.INSTAGRAM_POLARIS) { query(InstagramQuery.POLARIS) } }
            catch (second: InstagramFailure) {
                throw InstagramFailure(second.category, first.summary + " | " + second.summary)
            }
        }
        return AnalysisTrace.step(AnalysisTrace.Step.MEDIA_PARSE) { media(source, product) }
            .also { AnalysisTrace.media(it) }
    }

    private fun parse(text: String): JSONObject = try { JSONObject(text) }
        catch (_: org.json.JSONException) { throw EngineOutputException(text.length) }

    fun media(source: String, product: JSONObject): List<Media> {
        val carousel = product.optJSONArray("carousel_media")
        val children = carousel ?: JSONArray().put(product)
        require(children.length() in 1..50) { "Publication trop volumineuse : maximum 50 éléments." }
        return (0 until children.length()).map { index ->
            val item = children.getJSONObject(index)
            val videos = item.optJSONArray("video_versions")
            val video = item.optInt("media_type") == 2 || (videos?.length() ?: 0) > 0
            val images = item.optJSONObject("image_versions2")?.optJSONArray("candidates")
            val variants = if (video) videos else images
            val chosen = best(variants) ?: error(if (video) "No video URL; cover is not a video" else "No public image URL")
            val width = chosen.optInt("width", 0)
            val height = chosen.optInt("height", 0)
            val preview = best(images)?.optString("url")
            Media(source, null, (if (video) "Vidéo " else "Photo ") + (index + 1),
                preview ?: chosen.getString("url").takeUnless { video },
                if (width > 0 && height > 0) width.toString() + " × " + height else "Meilleure version exposée",
                directUrl = chosen.getString("url"), kind = if (video) MediaKind.VIDEO else MediaKind.PHOTO)
        }
    }

    private fun best(variants: JSONArray?): JSONObject? = (0 until (variants?.length() ?: 0))
        .mapNotNull { variants?.optJSONObject(it) }
        .filter {
            runCatching { val uri = URI(it.optString("url")); uri.scheme == "https" && uri.userInfo == null &&
                uri.port in listOf(-1, 443) && cdn(uri.host.orEmpty()) }.getOrDefault(false)
        }.maxWithOrNull(compareBy<JSONObject> { it.optLong("width", 0) * it.optLong("height", 0) }
            .thenBy { it.optLong("bit_rate", 0) })
}
