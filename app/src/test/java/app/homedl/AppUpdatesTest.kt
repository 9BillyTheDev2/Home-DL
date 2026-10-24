package app.homedl

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AppUpdatesTest {
    private fun manifest(url: String = "https://github.com/9BillyTheDev2/Home-DL/releases/download/v0.2.0/Home-DL.apk") =
        JSONObject().put("versionCode", 7).put("versionName", "0.2.0").put("url", url)
            .put("size", 60_000_000).put("sha256", "a".repeat(64))
    @Test fun acceptsOnlyOwnRelease() {
        assertEquals(7L, AppUpdates.manifest(manifest()).versionCode)
        assertTrue(AppUpdates.trusted("release-assets.githubusercontent.com"))
        assertFalse(AppUpdates.trusted("github.com.evil.invalid"))
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsAnotherRepository() {
        AppUpdates.manifest(manifest("https://github.com/other/app/releases/download/v0.2.0/Home-DL.apk"))
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingIntegrity() { AppUpdates.manifest(manifest().put("sha256", "")) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsOversizedDownload() { AppUpdates.manifest(manifest().put("size", Long.MAX_VALUE)) }
}
