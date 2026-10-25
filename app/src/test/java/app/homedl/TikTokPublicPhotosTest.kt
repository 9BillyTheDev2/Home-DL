package app.homedl

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class TikTokPublicPhotosTest {
    private val source = "https://www.tiktok.com/@example/photo/12345"
    private fun response(id: String = "12345"): JSONObject = JSONObject("""{"__DEFAULT_SCOPE__":{
        "webapp.video-detail":{"statusCode":0,"itemInfo":{"itemStruct":{"id":"$id","imagePost":{"images":[
        {"imageWidth":1080,"imageHeight":1920,"imageURL":{"urlList":["https://p16.tiktokcdn-eu.com/photo-a.jpg"]}},
        {"imageWidth":608,"imageHeight":1080,"imageURL":{"urlList":["https://p16.tiktokcdn-us.com/photo-b.jpg"]}}
        ]}}}}}}""")
    private fun html(data: JSONObject) = "<script type='application/json' id='__UNIVERSAL_DATA_FOR_REHYDRATION__'>" + data + "</script>"

    @Test fun photoAndVideoRoutesResolveToTheSameCanonicalPost() {
        assertEquals("https://www.tiktok.com/@example/video/12345" to "12345",
            TikTokPublicPhotos.canonical(source + "?tracking=discarded"))
        assertEquals(TikTokPublicPhotos.canonical(source), TikTokPublicPhotos.canonical(source.replace("/photo/", "/video/")))
    }

    @Test fun orderedImagesAreRealPhotosWithTheirDimensions() {
        val images = TikTokPublicPhotos.parse(source, "12345", html(response()))
        assertEquals(2, images.size)
        assertEquals("1080 × 1920", images[0].quality)
        assertEquals("608 × 1080", images[1].quality)
        assertTrue(images.all { it.kind == MediaKind.PHOTO && it.preview == it.directUrl })
    }

    @Test fun wrongPostAndVideoCoverAreRefused() {
        assertThrows(IllegalStateException::class.java) { TikTokPublicPhotos.parse(source, "12345", html(response("999"))) }
        val data = response()
        val item = data.getJSONObject("__DEFAULT_SCOPE__").getJSONObject("webapp.video-detail")
            .getJSONObject("itemInfo").getJSONObject("itemStruct")
        item.remove("imagePost")
        item.put("video", JSONObject().put("cover", "https://p16.tiktokcdn-eu.com/cover.jpg"))
        assertThrows(IllegalStateException::class.java) { TikTokPublicPhotos.parse(source, "12345", html(data)) }
    }

    @Test fun absentDataAndRestrictedResponsesDoNotBecomePhotos() {
        assertThrows(IllegalStateException::class.java) { TikTokPublicPhotos.parse(source, "12345", "<html>empty</html>") }
        val data = response()
        data.getJSONObject("__DEFAULT_SCOPE__").getJSONObject("webapp.video-detail").put("statusCode", 10216)
        assertThrows(IllegalStateException::class.java) { TikTokPublicPhotos.parse(source, "12345", html(data)) }
    }

    @Test fun cdnValidationRejectsOtherSitesAndUnsafeSchemes() {
        assertTrue(TikTokPublicPhotos.trustedImage("https://p16.tiktokcdn-eu.com/image.jpg?signature=sample"))
        for (url in listOf("https://p16.tiktokcdn-eu.com.attacker.invalid/photo.jpg",
            "http://p16.tiktokcdn-eu.com/photo.jpg", "https://user@p16.tiktokcdn-eu.com/photo.jpg",
            "https://p16.tiktokcdn-eu.com:444/photo.jpg", "file:///private/photo.jpg"))
            assertFalse(TikTokPublicPhotos.trustedImage(url))
        assertThrows(IllegalArgumentException::class.java) {
            TikTokPublicPhotos.canonical("https://tiktok.com.attacker.invalid/@example/photo/12345")
        }
    }

    @Test fun malformedJsonDoesNotCopyPageMetadata() {
        try {
            TikTokPublicPhotos.parse(source, "12345", "<script id='__UNIVERSAL_DATA_FOR_REHYDRATION__'>{PRIVATE</script>")
            fail("Expected parser failure")
        } catch (error: EngineOutputException) {
            assertFalse(error.message.orEmpty().contains("PRIVATE"))
        }
    }
}
