package app.homedl

/** Official release pinned in scripts/prepare_engine.py; no user data. */
object EngineManifest {
    const val VERSION = "2026.10.21"
    const val SHA256 = "b7af77f686e95f7845ce8d09d21957718febb8b48b30555adb9dd2292ad7bf14"

    fun checksum(text: String): String = text.lineSequence().mapNotNull { line ->
        Regex("^([0-9a-fA-F]{64})[ \\t]+\\*?yt-dlp$").matchEntire(line.trim())?.groupValues?.get(1)
    }.singleOrNull() ?: error("engine checksum unavailable")
}
