package app.homedl

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class InstagramResponseTest {
    private val code = "Example"
    private val id = InstagramPublic.mediaId(code)
    @Test fun acceptsFourPhotoShortcodeResponse() {
        val product = JSONObject().put("code", code).put("media_type", 8)
            .put("carousel_media", org.json.JSONArray(List(4) {
                JSONObject("""{"media_type":1,"image_versions2":{"candidates":[{"url":"https://s.cdninstagram.com/image.jpg","width":1440,"height":1920}]}}""")
            }))
        val response = JSONObject().put("data", JSONObject().put("xdt_api__v1__media__shortcode__web_info",
            JSONObject().put("items", org.json.JSONArray().put(product))))
        val found = InstagramResponse.product(response, InstagramQuery.SHORTCODE, code, id)
        assertEquals(4, InstagramPublic.media("https://www.instagram.com/p/Example/", found).size)
    }
    @Test fun acceptsPolarisOnlyWithMatchingIdentity() {
        val response = JSONObject().put("data", JSONObject().put("xig_polaris_media",
            JSONObject().put("if_not_gated_logged_out", JSONObject().put("code", code))))
        assertEquals(code, InstagramResponse.product(response, InstagramQuery.POLARIS, code, id).getString("code"))
    }
    @Test fun handlesNullDataWithoutUnknownOrMetadataLeak() {
        val response = JSONObject("""{"data":null,"errors":[{"message":"execution error private_metadata_marker https://example.invalid/private","code":4630001}]}""")
        val failure = failure(response)
        assertEquals("IG_QUERY", failure.category)
        assertTrue(failure.summary.contains("4630001"))
        assertFalse(failure.message!!.contains("private_metadata_marker"))
        assertTrue(InstagramResponse.canTryAlternative(failure))
        assertEquals("IG_QUERY", Diagnostics.classify(failure).code)
    }
    @Test fun stopsOnRateLimitWithoutTryingAnotherQuery() {
        val failure = failure(JSONObject("""{"errors":[{"message":"Rate limit exceeded","code":1675004}],"data":null}"""))
        assertEquals("IG_RATE", failure.category)
        assertFalse(InstagramResponse.canTryAlternative(failure))
    }
    @Test fun stopsOnExplicitAccessRefusal() {
        val failure = failure(JSONObject("""{"errors":[{"message":"Login required"}],"data":null}"""))
        assertEquals("IG_ACCESS", failure.category)
        assertFalse(InstagramResponse.canTryAlternative(failure))
    }
    @Test fun rateLimitInRootMessageAlsoStops() {
        val failure = failure(JSONObject("""{"status":"fail","message":"Please wait a few minutes before you try again."}"""))
        assertEquals("IG_RATE", failure.category)
        assertFalse(InstagramResponse.canTryAlternative(failure))
    }
    @Test fun missingProductIsAnExplicitFailure() {
        assertEquals("IG_EMPTY", failure(JSONObject("""{"data":{"xig_polaris_media":null}}""")).category)
    }
    @Test fun neverAcceptsAnotherPostsMetadata() {
        val response = JSONObject("""{"data":{"xdt_api__v1__media__shortcode__web_info":{"items":[{"code":"OtherPost"}]}}}""")
        assertEquals("IG_MISMATCH", failure(response).category)
    }
    private fun failure(response: JSONObject): InstagramFailure {
        try { InstagramResponse.product(response, InstagramQuery.SHORTCODE, code, id) }
        catch (e: InstagramFailure) { return e }
        throw AssertionError("Expected a classified failure")
    }
}
