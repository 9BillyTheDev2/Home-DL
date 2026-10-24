package app.homedl

import java.net.URI
import org.json.JSONObject

data class Media(val source: String, val index: Int?, val title: String, val preview: String?, val quality: String)

object Links {
    fun parse(text: String): String {
        val match = Regex("https://[^\\s<>]+").find(text)?.value
            ?: throw IllegalArgumentException("Collez un lien HTTPS.")
        val uri = URI(match)
        require(uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null) {
            "Ce lien n’est pas valide."
        }
        return uri.toASCIIString()
    }
}

object MediaParser {
    fun parse(source: String, output: String): List<Media> {
        val root = JSONObject(output)
        val entries = root.optJSONArray("entries")
        val results = mutableListOf<Media>()
        fun add(item: JSONObject, index: Int?) {
            // A thumbnail is never treated as a downloadable photo.
            if (!item.has("formats") && !item.has("url")) return
            val height = item.optInt("height", 0)
            results += Media(source, index, item.optString("title", "Vidéo"),
                item.optString("thumbnail").takeIf { it.startsWith("https://") },
                if (height > 0) "${height}p disponible" else "Meilleure qualité accessible")
        }
        if (entries == null) add(root, null)
        else {
            require(entries.length() <= 50) { "Publication trop volumineuse : maximum 50 éléments pour ce prototype." }
            for (i in 0 until entries.length()) entries.optJSONObject(i)?.let { add(it, i + 1) }
        }
        return results
    }
}
