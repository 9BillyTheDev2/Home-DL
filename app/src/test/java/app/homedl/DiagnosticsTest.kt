package app.homedl

import org.junit.Assert.*
import org.junit.Test

class DiagnosticsTest {
    @Test fun cookieHelpDoesNotBecomeAnAuthenticationFailure() {
        val error = Exception("No video formats found. Use --cookies for the authentication. See https://example.invalid/help")
        assertEquals("NO_VIDEO", Diagnostics.classify(error).code)
    }

    @Test fun forbiddenTakesPriorityOverCookieAdvice() {
        val error = Exception("Unable to download webpage: HTTP Error 403: Forbidden. Use --cookies for authentication")
        assertEquals("HTTP_403", Diagnostics.classify(error).code)
    }

    @Test fun alternativesAreNotReportedAsCertainLoginRequirement() {
        val error = Exception("Requested content is not available, rate-limit reached or login required. Use --cookies")
        assertEquals("ACCESS_UNCERTAIN", Diagnostics.classify(error).code)
    }

    @Test fun explicitLoginAndRateLimitRemainDistinct() {
        assertEquals("AUTH", Diagnostics.classify(Exception("Login required")).code)
        assertEquals("RATE_LIMIT", Diagnostics.classify(Exception("HTTP Error 429: Too Many Requests")).code)
        assertEquals("UNKNOWN", Diagnostics.classify(Exception("item id429xyz")).code)
    }

    @Test fun missingModuleIsNotHiddenByAuthenticationAdvice() {
        assertEquals("ENGINE_RUNTIME", Diagnostics.classify(Exception("ModuleNotFoundError: No module named 'sample_module'. Use --cookies")).code)
    }

    @Test fun reportsDoNotExposeCommonSecretsOrPrivateLocations() {
        val sensitive = "HTTP Error 403 at https://example.invalid/private?id=privateIdentifier\n" +
            "Cookie: sessionid=PRIVATE_SESSION\nAuthorization: Bearer PRIVATE_ACCESS\n" +
            "user@example.invalid /data/user/0/sample/private/file.txt 192.168.5.9 @sample_account\n" +
            "[Instagram] samplePostId: error"
        val report = Diagnostics.report(Stage.ANALYZE, Exception(sensitive), "test", "test-engine", "Instagram")
        for (value in listOf("privateIdentifier", "PRIVATE_SESSION", "PRIVATE_ACCESS", "user@example.invalid",
            "/data/user/0", "192.168.5.9", "@sample_account", "samplePostId")) {
            assertFalse(value, report.contains(value))
        }
        assertTrue(report.contains("HTTP_403"))
    }

    @Test fun parserErrorsOnlyExposeResponseLength() {
        val report = Diagnostics.report(Stage.ANALYZE, EngineOutputException(123), "test", "test", "autre")
        assertTrue(report.contains("ENGINE_OUTPUT"))
        assertTrue(report.contains("123"))
    }

    @Test fun diagnosticExcerptIsBounded() {
        assertTrue(Diagnostics.redact("diagnostic ".repeat(5000)).length <= 4000)
    }
}
