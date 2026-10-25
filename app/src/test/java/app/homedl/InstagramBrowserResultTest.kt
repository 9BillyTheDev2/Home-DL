package app.homedl

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class InstagramBrowserResultTest {
    private val source = "https://www.instagram.com/p/ABC/"
    private fun response() = JSONObject("""{"product":{"code":"ABC","pk":"66","media_type":1,"image_versions2":{"candidates":[{"url":"https://test.cdninstagram.com/photo.jpg","width":1200,"height":800}]}},"stats":{"responses":2,"parsed":1}}""")
    @Test fun matchedPublicProductIsAccepted() {
        assertEquals(1, InstagramBrowserResult.media(source, response().toString()).size)
        assertEquals(MediaKind.PHOTO, InstagramBrowserResult.media(source, response().toString()).single().kind)
    }
    @Test fun differentPostAndUntrustedMediaAreRejected() {
        val data = response()
        data.getJSONObject("product").put("code", "OTHER")
        assertThrows(IllegalStateException::class.java) { InstagramBrowserResult.media(source, data.toString()) }
        val bad = response()
        bad.getJSONObject("product").getJSONObject("image_versions2").getJSONArray("candidates")
            .getJSONObject(0).put("url", "https://example.invalid/photo.jpg")
        assertThrows(IllegalStateException::class.java) { InstagramBrowserResult.media(source, bad.toString()) }
    }
    @Test fun videoCoverIsNeverReturnedAsPhoto() {
        val data = response(); data.getJSONObject("product").put("media_type", 2)
        assertThrows(IllegalStateException::class.java) { InstagramBrowserResult.media(source, data.toString()) }
    }
    @Test fun mainNavigationStaysHttpsAndOnInstagram() {
        assertTrue(InstagramBrowserResult.allowedPage(source))
        for (url in listOf("http://www.instagram.com/p/ABC/", "https://www.instagram.com.evil.invalid/", "https://user@www.instagram.com/", "file:///data/private", "https://www.instagram.com:444/"))
            assertFalse(InstagramBrowserResult.allowedPage(url))
        assertTrue(InstagramBrowserResult.accessPage("https://www.instagram.com/accounts/login/"))
        assertTrue(InstagramBrowserResult.accessPage("https://www.instagram.com/challenge/"))
        assertFalse(InstagramBrowserResult.accessPage(source))
    }
    @Test fun diagnosticContainsOnlyBoundedCounters() {
        val data = response().put("secret", "PRIVATE_SECRET")
        data.getJSONObject("stats").put("scripts", -2).put("matched", 999999)
        assertEquals("responses=2; parsed=1; scripts=0; matched=100000", InstagramBrowserResult.summary(data.toString()))
        assertFalse(InstagramBrowserResult.summary(data.toString()).contains("PRIVATE"))
        assertThrows(IllegalArgumentException::class.java) { InstagramBrowserResult.media(source, " ".repeat(240001)) }
    }
}
