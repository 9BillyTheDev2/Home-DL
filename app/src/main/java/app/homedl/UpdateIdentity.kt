package app.homedl

object UpdateIdentity {
    fun requireCompatible(packageName: String, currentVersion: Long, currentSigners: Set<String>,
                          candidatePackage: String, candidateVersion: Long, candidateSigners: Set<String>,
                          expectedVersion: Long? = null) {
        require(candidatePackage == packageName) { "Wrong update package" }
        require(candidateVersion > currentVersion && (expectedVersion == null || candidateVersion == expectedVersion)) {
            "Wrong update version"
        }
        require(currentSigners.isNotEmpty() && currentSigners == candidateSigners) {
            "Application signature mismatch; update refused. A test build cannot update to a different signature."
        }
    }
}
