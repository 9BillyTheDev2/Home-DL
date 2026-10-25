package app.homedl

import org.junit.Assert.*
import org.junit.Test

class AnalysisTraceTest {
    @Test fun successfulAnalysesCanBeComparedWithFailures() {
        AnalysisTrace.start()
        try {
            AnalysisTrace.step(AnalysisTrace.Step.MEDIA_PARSE) { 1 }
            val report = Diagnostics.analysisSuccess("test", "test", "X", 1)
            assertTrue(report.contains("Code : OK"))
            assertTrue(report.contains("MEDIA_PARSE OK"))
            assertTrue(Diagnostics.analysisSuccess("test", "test", "X", 0).contains("Code : EMPTY"))
        } finally { AnalysisTrace.clear() }
    }
    @Test fun httpFailureNamesTheRequestPhaseWithoutCopyingPrivateData() {
        AnalysisTrace.start()
        try {
            try {
                AnalysisTrace.step(AnalysisTrace.Step.INSTAGRAM_ACCESS) {
                    AnalysisTrace.http(404, "text/html; secret=PRIVATE_VALUE", 123)
                    throw HttpStatusException(404)
                }
            } catch (expected: HttpStatusException) {
                val report = Diagnostics.report(Stage.ANALYZE, expected, "test", "test", "Instagram")
                assertTrue(report.contains("INSTAGRAM_ACCESS FAIL HTTP_404"))
                assertTrue(report.contains("status=404 type=HTML announcedBytes=123"))
                assertFalse(report.contains("PRIVATE_VALUE"))
                assertEquals("HTTP_404", Diagnostics.classify(expected).code)
            }
        } finally { AnalysisTrace.clear() }
    }

    @Test fun traceNeverCopiesExceptionMessages() {
        AnalysisTrace.start()
        try {
            try { AnalysisTrace.step(AnalysisTrace.Step.ENGINE_ANALYZE) {
                throw IllegalStateException("Cookie: PRIVATE_COOKIE https://private.invalid/secret")
            } } catch (_: IllegalStateException) { }
            assertTrue(AnalysisTrace.snapshot().contains("ENGINE_ANALYZE FAIL ERROR"))
            assertFalse(AnalysisTrace.snapshot().contains("PRIVATE"))
        } finally { AnalysisTrace.clear() }
    }

    @Test fun traceIsBoundedAndResetBetweenOperations() {
        AnalysisTrace.start()
        repeat(100) { AnalysisTrace.bytes(it.toLong()) }
        assertEquals(64, AnalysisTrace.snapshot().lines().size)
        AnalysisTrace.start()
        assertEquals("", AnalysisTrace.snapshot())
        AnalysisTrace.clear()
        AnalysisTrace.bytes(123)
        assertEquals("", AnalysisTrace.snapshot())
    }

    @Test fun concurrentOperationsDoNotShareTheirTrace() {
        AnalysisTrace.start()
        try {
            AnalysisTrace.bytes(111)
            var other = ""
            val thread = Thread {
                AnalysisTrace.start()
                AnalysisTrace.bytes(222)
                other = AnalysisTrace.snapshot()
                AnalysisTrace.clear()
            }
            thread.start(); thread.join()
            assertTrue(other.contains("bytes=222"))
            assertFalse(other.contains("bytes=111"))
            assertFalse(AnalysisTrace.snapshot().contains("bytes=222"))
        } finally { AnalysisTrace.clear() }
    }
}
