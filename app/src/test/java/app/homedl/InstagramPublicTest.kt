package app.homedl

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class InstagramPublicTest {
    private val source = "https://www.instagram.com/p/Example/"
    @Test fun normalizesPostAndIgnoresShareTracking() {
        assertEquals("Example", InstagramPublic.reference(source + "?img_index=3&tracking=ignored"))
        assertEquals("Example", InstagramPublic.reference("https://instagram.com/reel/Example/"))
        assertEquals("64", InstagramPublic.mediaId("BA"))
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsLookalikeHost() { InstagramPublic.reference("https://www.instagram.com.evil.invalid/p/Example/") }
    @Test fun restrictsMediaOrigins() {
        assertTrue(InstagramPublic.cdn("scontent.cdninstagram.com"))
        assertFalse(InstagramPublic.cdn("cdninstagram.com.evil.invalid"))
        assertFalse(InstagramPublic.cdn("evilcdninstagram.com"))
    }
    @Test fun keepsCarouselOrderAndSelectsLargestActualMedia() {
        val product = JSONObject("""{"carousel_media":[
          {"media_type":1,"image_versions2":{"candidates":[
            {"url":"https://s.cdninstagram.com/small.jpg","width":320,"height":320},
            {"url":"https://s.cdninstagram.com/large.jpg","width":1080,"height":1350}]}},
          {"media_type":2,"video_versions":[
            {"url":"https://s.cdninstagram.com/small.mp4","width":320,"height":320},
            {"url":"https://s.cdninstagram.com/large.mp4","width":1080,"height":1920}],
           "image_versions2":{"candidates":[{"url":"https://s.cdninstagram.com/cover.jpg","width":320,"height":320}]}}
        ]}""")
        val media = InstagramPublic.media(source, product)
        assertEquals(listOf(MediaKind.PHOTO, MediaKind.VIDEO), media.map { it.kind })
        assertTrue(media[0].directUrl!!.endsWith("large.jpg"))
        assertTrue(media[1].directUrl!!.endsWith("large.mp4"))
        assertTrue(media[1].preview!!.endsWith("cover.jpg"))
    }
    @Test(expected = IllegalStateException::class)
    fun neverDownloadsVideoCoverAsPhoto() {
        InstagramPublic.media(source, JSONObject("""{"media_type":2,"image_versions2":{"candidates":[{"url":"https://s.cdninstagram.com/cover.jpg"}]}}"""))
    }
    @Test(expected = IllegalStateException::class)
    fun rejectsNonCdnPayload() {
        InstagramPublic.media(source, JSONObject("""{"media_type":1,"image_versions2":{"candidates":[{"url":"https://evil.invalid/file.jpg"}]}}"""))
    }
}
