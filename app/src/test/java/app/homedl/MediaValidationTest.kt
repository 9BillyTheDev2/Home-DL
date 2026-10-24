package app.homedl

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class MediaValidationTest {
    @Test fun detectsPngAndPreservesActualExtension() {
        val directory = java.nio.file.Files.createTempDirectory("homedl-image").toFile()
        try {
            val file = File(directory, "media.jpg")
            file.writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 13, 10, 26, 10) + ByteArray(24))
            val result = MediaValidation.check(file, MediaKind.PHOTO)
            assertEquals("png", result.extension)
            assertTrue(result.isFile)
        } finally { directory.deleteRecursively() }
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsHtmlSavedAsMedia() {
        val file = File.createTempFile("homedl-invalid", ".mp4")
        try {
            file.writeText("<html>Access denied; not a video</html>")
            MediaValidation.check(file, MediaKind.VIDEO)
        } finally { file.delete() }
    }
}
