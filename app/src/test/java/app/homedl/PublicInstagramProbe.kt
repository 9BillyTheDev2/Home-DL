package app.homedl

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/** Manual network smoke test using a public fixture from yt-dlp's own tests, never a user's link. */
object PublicInstagramProbe {
    @JvmStatic fun main(args: Array<String>) {
        val directory = java.nio.file.Files.createTempDirectory("home-dl-public-probe").toFile()
        try {
            val token = AtomicBoolean(false)
            val media = InstagramPublic.analyze("https://www.instagram.com/reel/Chunk8-jurw/", token)
            val first = media.first { it.kind == MediaKind.VIDEO }
            val file = File(directory, "probe.mp4")
            file.outputStream().use { output ->
                HttpsTransfer.stream(first.directUrl!!, 30L * 1024 * 1024, token,
                    System.nanoTime() + 90_000_000_000L, InstagramPublic::cdn, output,
                    mapOf("Referer" to "https://www.instagram.com/"))
            }
            MediaValidation.check(file, MediaKind.VIDEO)
            println("PUBLIC_INSTAGRAM_PASS: Kotlin adapter returned media and downloaded an MP4; bytes=" + file.length())
            println("This JVM check does not validate Android networking, MediaStore or all public posts.")
        } catch (e: Exception) {
            println("PUBLIC_INSTAGRAM_UNCONFIRMED: " + e.javaClass.simpleName)
            println("External access or extraction failed. No platform success is inferred from this build.")
        } finally { directory.deleteRecursively() }
    }
}
