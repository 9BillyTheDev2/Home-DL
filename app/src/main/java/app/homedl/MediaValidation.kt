package app.homedl

import java.io.File

object MediaValidation {
    fun check(file: File, kind: MediaKind): File {
        val header = ByteArray(32)
        val count = file.inputStream().use { it.read(header) }
        require(count >= 12) { "Incomplete media file" }
        val jpeg = header[0] == 0xff.toByte() && header[1] == 0xd8.toByte() && header[2] == 0xff.toByte()
        val png = header.take(8).toByteArray().contentEquals(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 13, 10, 26, 10))
        val webp = String(header, 0, 4, Charsets.US_ASCII) == "RIFF" && String(header, 8, 4, Charsets.US_ASCII) == "WEBP"
        val mp4 = String(header, 4, 4, Charsets.US_ASCII) == "ftyp"
        require(if (kind == MediaKind.PHOTO) jpeg || png || webp else mp4) { "Unexpected media format; no file saved" }
        if (kind == MediaKind.PHOTO && !jpeg) {
            val correct = File(file.parentFile, if (png) "media.png" else "media.webp")
            require(file.renameTo(correct)) { "Unable to rename image" }
            return correct
        }
        return file
    }
}
