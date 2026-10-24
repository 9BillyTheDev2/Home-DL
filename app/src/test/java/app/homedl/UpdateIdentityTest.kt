package app.homedl

import org.junit.Test

class UpdateIdentityTest {
    @Test fun acceptsNewerOwnSignedPackage() {
        UpdateIdentity.requireCompatible("app.homedl", 7, setOf("same-cert"), "app.homedl", 8, setOf("same-cert"), 8)
    }
    @Test(expected = IllegalArgumentException::class)
    fun refusesAnotherSigningKey() {
        UpdateIdentity.requireCompatible("app.homedl", 7, setOf("original"), "app.homedl", 8, setOf("other"), 8)
    }
    @Test(expected = IllegalArgumentException::class)
    fun refusesAnotherPackage() {
        UpdateIdentity.requireCompatible("app.homedl", 7, setOf("same"), "other.app", 8, setOf("same"), 8)
    }
    @Test(expected = IllegalArgumentException::class)
    fun refusesReplayOrDowngrade() {
        UpdateIdentity.requireCompatible("app.homedl", 7, setOf("same"), "app.homedl", 7, setOf("same"))
    }
    @Test(expected = IllegalArgumentException::class)
    fun refusesMissingCertificate() {
        UpdateIdentity.requireCompatible("app.homedl", 7, emptySet(), "app.homedl", 8, emptySet(), 8)
    }
}
