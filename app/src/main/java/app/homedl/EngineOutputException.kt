package app.homedl

/** Keeps only invalid response length, never private media metadata. */
class EngineOutputException(val characterCount: Int) :
    Exception("Invalid engine JSON response (" + characterCount + " characters)")
