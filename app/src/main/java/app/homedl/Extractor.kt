package app.homedl

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.ffmpeg.FFmpeg
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class Extractor(private val context: Context) {
    fun init() {
        val updates = EngineUpdates(context)
        updates.recover()
        YoutubeDL.init(context)
        updates.installBundled()
        FFmpeg.init(context)
    }

    private fun request(url: String): YoutubeDLRequest =
        YoutubeDLRequest(url).addOption("--ignore-config")
            .addOption("--socket-timeout", 15).addOption("--retries", 1)
            .addOption("--extractor-retries", 1)
            .addOption("--playlist-end", 50)

    fun analyze(url: String, id: String, token: AtomicBoolean): List<Media> {
        if (Links.platform(url) == "Instagram") return InstagramPublic.analyze(url, token)
        val req = request(url).addOption("--dump-single-json")
            .addOption("--skip-download").addOption("--no-flat-playlist")
        val output = AnalysisTrace.step(AnalysisTrace.Step.ENGINE_ANALYZE) { YoutubeDL.execute(req, id, null).out }
        AnalysisTrace.bytes(output.toByteArray(Charsets.UTF_8).size.toLong())
        return try { AnalysisTrace.step(AnalysisTrace.Step.MEDIA_PARSE) { MediaParser.parse(url, output) }
            .also { AnalysisTrace.media(it) } }
        catch (_: org.json.JSONException) { throw EngineOutputException(output.length) }
    }

    fun download(media: Media, directory: File, id: String, token: AtomicBoolean, progress: (Float) -> Unit): File {
        if (media.directUrl != null) {
            val target = File(directory, if (media.kind == MediaKind.PHOTO) "media.jpg" else "media.mp4")
            target.outputStream().use { out ->
                HttpsTransfer.stream(media.directUrl, 1024L * 1024 * 1024, token,
                    System.nanoTime() + 1_800_000_000_000L, InstagramPublic::cdn, out,
                    mapOf("Referer" to "https://www.instagram.com/")) { total, size ->
                    if (size > 0) progress(total * 100f / size)
                }
            }
            return MediaValidation.check(target, media.kind)
        }
        val req = request(media.source)
            .addOption("-f", "bv*+ba/b")
            .addOption("--no-mtime")
            .addOption("-o", File(directory, "media.%(ext)s").absolutePath)
        if (media.index != null) req.addOption("--playlist-items", media.index)
        else req.addOption("--no-playlist")
        YoutubeDL.execute(req, id) { percent, _, _ -> progress(percent) }
        val files = directory.listFiles()?.filter { it.isFile && it.extension !in setOf("part", "ytdl", "json") }.orEmpty()
        require(files.size == 1 && files.first().length() > 0) { "Fichier final introuvable." }
        return files.first()
    }
    fun cancel(id: String) { YoutubeDL.destroyProcessById(id) }
}
