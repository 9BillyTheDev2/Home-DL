package app.homedl

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.ffmpeg.FFmpeg
import java.io.File

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

    fun analyze(url: String, id: String): List<Media> {
        val req = request(url).addOption("--dump-single-json")
            .addOption("--skip-download").addOption("--no-flat-playlist")
        val output = YoutubeDL.execute(req, id, null).out
        return try { MediaParser.parse(url, output) }
        catch (_: org.json.JSONException) { throw EngineOutputException(output.length) }
    }

    fun download(media: Media, directory: File, id: String, progress: (Float) -> Unit): File {
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
