package app.homedl

import org.json.JSONObject
import java.net.URI

/** Treat all values returned by page JavaScript as untrusted; validate again natively. */
object InstagramBrowserResult {
    fun allowedPage(raw: String): Boolean = runCatching {
        val uri = URI(raw)
        uri.scheme == "https" && uri.host in setOf("www.instagram.com", "instagram.com") &&
            uri.userInfo == null && uri.port in listOf(-1, 443)
    }.getOrDefault(false)

    fun accessPage(raw: String): Boolean = runCatching {
        val path = URI(raw).path
        listOf("/accounts/", "/challenge", "/checkpoint", "/consent/").any { path.startsWith(it) }
    }.getOrDefault(true)

    enum class Navigation { PAGE, ACCESS, IGNORE }

    fun navigation(raw: String): Navigation = when {
        !allowedPage(raw) -> Navigation.IGNORE
        accessPage(raw) -> Navigation.ACCESS
        else -> Navigation.PAGE
    }

    /** Fixed categories only. Never reproduce an intent, host, path or query in a report. */
    fun navigationSummary(raw: String): String = runCatching {
        val uri = URI(raw)
        val scheme = when (uri.scheme?.lowercase()) {
            "https" -> "HTTPS"
            "http" -> "HTTP"
            "intent" -> "APP_INTENT"
            "instagram" -> "INSTAGRAM_APP"
            "about" -> "ABOUT"
            else -> "OTHER"
        }
        val origin = when (uri.host?.lowercase()) {
            "instagram.com", "www.instagram.com" -> "INSTAGRAM_WEB"
            else -> "OTHER"
        }
        "scheme=$scheme origin=$origin"
    }.getOrDefault("scheme=INVALID origin=OTHER")

    fun media(source: String, raw: String): List<Media> {
        require(raw.toByteArray(Charsets.UTF_8).size <= 240000) { "Browser response too large" }
        val shortcode = InstagramPublic.reference(source)
        val product = JSONObject(raw).getJSONObject("product")
        check(product.optString("code") == shortcode && product.optString("pk") == InstagramPublic.mediaId(shortcode)) {
            "Browser media identity mismatch"
        }
        return InstagramPublic.media(source, product)
    }

    fun summary(raw: String): String {
        val stats = JSONObject(raw).optJSONObject("stats") ?: JSONObject()
        return listOf("responses", "parsed", "scripts", "matched").joinToString("; ") {
            "$it=" + stats.optInt(it, 0).coerceIn(0, 100000)
        }
    }
}
