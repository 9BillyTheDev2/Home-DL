package app.homedl

import org.junit.Assert.*
import org.junit.Test

class PreviewPolicyTest {
    private fun photo(preview: String?, direct: String?) =
        Media("https://www.instagram.com/p/test/", null, "Photo", preview, "", direct, MediaKind.PHOTO)

    @Test fun validatesCdnBoundariesAndHttps() {
        assertTrue(PreviewPolicy.valid("https://scontent.cdninstagram.com/image.jpg"))
        assertFalse(PreviewPolicy.valid("https://cdninstagram.com.evil.example/image.jpg"))
        assertFalse(PreviewPolicy.valid("https://127.0.0.1/image.jpg"))
        assertFalse(PreviewPolicy.valid("http://pbs.twimg.com/media/image.jpg"))
        assertFalse(PreviewPolicy.valid("https://user@pbs.twimg.com/media/image.jpg"))
        assertFalse(PreviewPolicy.valid("https://pbs.twimg.com:8443/media/image.jpg"))
    }

    @Test fun fallsBackToPhotoOriginalButNeverVideoFile() {
        val image = "https://pbs.twimg.com/media/test.jpg"
        assertEquals(listOf(image), PreviewPolicy.candidates(photo(null, image)))
        assertEquals(listOf(image), PreviewPolicy.candidates(photo(image, image)))
        val video = Media("https://x.com/test/status/1", null, "Video", null, "",
            "https://video.twimg.com/test.mp4", MediaKind.VIDEO)
        assertTrue(PreviewPolicy.candidates(video).isEmpty())
    }

    @Test fun boundedDecodeAndPlatformReferer() {
        assertEquals(16, PreviewPolicy.sample(4000, 6000))
        assertEquals(1, PreviewPolicy.sample(640, 480))
        assertEquals("https://www.instagram.com/",
            PreviewPolicy.headers("https://www.instagram.com/p/test/")["Referer"])
        assertFalse(PreviewPolicy.headers("https://x.com/test/status/1").containsKey("Cookie"))
        assertTrue(runCatching { PreviewPolicy.sample(0, 100) }.isFailure)
        assertTrue(runCatching { PreviewPolicy.sample(100001, 100) }.isFailure)
    }
}
