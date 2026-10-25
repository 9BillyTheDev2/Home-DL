package app.homedl

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class XPublicPhotosTest {
    private val source = "https://x.com/example/status/1234567890123456789"
    private fun response() = JSONObject("""{"id_str":"1234567890123456789","mediaDetails":[
        {"type":"photo","media_url_https":"https://pbs.twimg.com/media/SampleA.jpg","original_info":{"width":2048,"height":1536}},
        {"type":"photo","media_url_https":"https://pbs.twimg.com/media/SampleB.png"}]}""")

    @Test fun photosAreOrderedAndUseOriginalFilesWithSmallPreviews() {
        val media = XPublicPhotos.parse(source, response())
        assertEquals(2, media.size)
        assertEquals("Photo 1", media[0].title)
        assertEquals("2048 × 1536", media[0].quality)
        assertEquals("https://pbs.twimg.com/media/SampleA?format=jpg&name=orig", media[0].directUrl)
        assertTrue(media.all { it.kind == MediaKind.PHOTO && it.preview!!.endsWith("name=small") })
    }

    @Test fun quoteOrWrongPostIsNeverUsed() {
        assertThrows(IllegalStateException::class.java) {
            XPublicPhotos.parse(source, response().put("id_str", "999"))
        }
    }

    @Test fun videoCoversAreNeverReturnedAsPhotos() {
        val data = response()
        data.getJSONArray("mediaDetails").getJSONObject(0).put("type", "video")
        assertThrows(IllegalStateException::class.java) { XPublicPhotos.parse(source, data) }
    }

    @Test fun untrustedAndNonMediaUrlsAreRejected() {
        for (url in listOf("https://pbs.twimg.com.attacker.invalid/media/Sample.jpg",
            "https://user@pbs.twimg.com/media/Sample.jpg", "http://pbs.twimg.com/media/Sample.jpg",
            "https://pbs.twimg.com/profile_images/Sample.jpg", "https://pbs.twimg.com:444/media/Sample.jpg")) {
            assertThrows(IllegalArgumentException::class.java) { XPublicPhotos.imageUrl(url, "orig") }
        }
    }

    @Test fun supportedStatusRoutesKeepThePostId() {
        for (url in listOf(source, source + "/photo/1?s=20", "https://twitter.com/i/web/status/1234567890123456789"))
            assertEquals("1234567890123456789", XPublicPhotos.reference(url))
        assertThrows(IllegalArgumentException::class.java) {
            XPublicPhotos.reference("https://x.com.attacker.invalid/example/status/123")
        }
    }
}
