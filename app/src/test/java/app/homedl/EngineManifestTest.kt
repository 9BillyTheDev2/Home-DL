package app.homedl

import org.junit.Assert.assertEquals
import org.junit.Test

class EngineManifestTest {
    @Test fun distinguishesDnsAndAudienceRestriction() {
        assertEquals("DNS", Diagnostics.classify(Exception("UnknownHostException: Unable to resolve host api.github.com")).code)
        assertEquals("AUDIENCE_RESTRICTED", Diagnostics.classify(Exception("This content isn't available to everyone: It can't be seen by certain audiences.")).code)
    }
    @Test fun selectsExactZipappNotExecutableOrArchive() {
        val hash = "a".repeat(64)
        assertEquals(hash, EngineManifest.checksum("b".repeat(64) + "  yt-dlp.exe\n" + hash + "  yt-dlp\n" + "c".repeat(64) + "  yt-dlp.tar.gz\n"))
        assertEquals(hash, EngineManifest.checksum(hash + " *yt-dlp\r\n"))
    }
    @Test(expected = IllegalStateException::class)
    fun missingChecksumFailsClosed() { EngineManifest.checksum("bad yt-dlp") }
    @Test(expected = IllegalStateException::class)
    fun ambiguousChecksumFailsClosed() {
        val line = "a".repeat(64) + "  yt-dlp\n"
        EngineManifest.checksum(line + line)
    }
}
