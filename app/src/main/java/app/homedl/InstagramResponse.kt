package app.homedl

import org.json.JSONObject

enum class InstagramQuery(val document: String, val friendlyName: String) {
    SHORTCODE("27128499623469141", "PolarisPostRootQuery"),
    POLARIS("27130156389949648", "PolarisLoggedOutDesktopWWWPostRootContentQuery")
}

class InstagramFailure(val category: String, val summary: String) :
    IllegalStateException("Instagram " + category + "; " + summary)

/** Only fixed structural flags and short numeric error codes enter a diagnostic. */
object InstagramResponse {
    fun product(response: JSONObject, query: InstagramQuery, shortcode: String, id: String): JSONObject {
        val data = response.optJSONObject("data")
        val product = when (query) {
            InstagramQuery.SHORTCODE -> data?.optJSONObject("xdt_api__v1__media__shortcode__web_info")
                ?.optJSONArray("items")?.optJSONObject(0)
            InstagramQuery.POLARIS -> data?.optJSONObject("xig_polaris_media")
                ?.optJSONObject("if_not_gated_logged_out")
        }
        val errors = response.optJSONArray("errors")
        val count = errors?.length() ?: 0
        val codes = (0 until minOf(count, 5)).mapNotNull { errors?.optJSONObject(it)?.optString("code") }
            .filter { Regex("[0-9]{1,10}").matches(it) }.joinToString(",").ifBlank { "none" }
        val summary = "query=" + query.name + "; data=" + (data != null) + "; product=" + (product != null) +
            "; errors=" + count + "; codes=" + codes
        if (product != null) {
            val returnedCode = product.optString("code").takeUnless { it == "null" }.orEmpty()
            val returnedId = product.optString("pk").ifBlank { product.optString("id").substringBefore('_') }
            if ((returnedCode.isNotBlank() && returnedCode != shortcode) ||
                (returnedCode.isBlank() && returnedId != id)) throw InstagramFailure("IG_MISMATCH", summary)
            return product
        }
        // Server text is inspected in memory, never included in the exception or copied report.
        val descriptions = ((0 until minOf(count, 10)).joinToString(" ") {
            val item = errors?.optJSONObject(it)
            listOf("message", "summary", "description").joinToString(" ") { key -> item?.optString(key).orEmpty() }
        } + " " + listOf("message", "feedback_title", "feedback_message")
            .joinToString(" ") { response.optString(it) }).lowercase(java.util.Locale.ROOT)
        val category = when {
            listOf("rate limit", "too many requests", "please wait", "try again later")
                .any { it in descriptions } || codes.split(',').contains("1675004") -> "IG_RATE"
            response.optBoolean("login_required") || response.optString("checkpoint_url").let { it.isNotBlank() && it != "null" } ||
                listOf("login required", "log in", "checkpoint", "challenge required", "private content",
                    "not available to everyone", "certain audiences").any { it in descriptions } -> "IG_ACCESS"
            count > 0 || response.optString("status") == "fail" -> "IG_QUERY"
            else -> "IG_EMPTY"
        }
        throw InstagramFailure(category, summary)
    }

    fun canTryAlternative(failure: InstagramFailure) = failure.category in setOf("IG_QUERY", "IG_EMPTY")
}
