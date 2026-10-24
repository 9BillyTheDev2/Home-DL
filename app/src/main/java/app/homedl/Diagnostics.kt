package app.homedl

import java.util.Locale

enum class Stage { INIT, ANALYZE, DOWNLOAD, UPDATE }
data class FailureInfo(val code: String, val message: String)

object Diagnostics {
    private fun messages(error: Throwable) = generateSequence(error) { it.cause }.take(5).toList()

    fun classify(error: Throwable): FailureInfo {
        val text = messages(error).joinToString("\n") { it.message.orEmpty() }.lowercase(Locale.ROOT)
        fun result(code: String, message: String) = FailureInfo(code, message)
        val causeText = text.substringBefore("use --cookies").substringBefore("use --username")
            .substringBefore("see https://").substringBefore("see  https://")
        val auth = listOf("login required", "login_required", "login is required", "must be logged in",
            "please log in", "you need to log in", "authentication required",
            "sign in to confirm", "private video", "private account").any { it in causeText }
        val rate = Regex("""\b429\b""").containsMatchIn(causeText) ||
            listOf("too many requests", "rate-limit", "rate limit").any { it in causeText }
        return when {
            "no space left" in text || "enospc" in text -> result("SPACE", "Espace de stockage insuffisant.")
            "permission denied" in text || "eacces" in text -> result("PERMISSION", "Accès à un fichier refusé.")
            "no module named" in text || "cannot import name" in text || "modulenotfounderror" in text ->
                result("ENGINE_RUNTIME", "Une dépendance du moteur manque ou est incompatible.")
            "unrecognized arguments" in text || "no such option" in text ->
                result("ENGINE_OPTIONS", "Le moteur ne reconnaît pas une option.")
            error is EngineOutputException -> result("ENGINE_OUTPUT", "Réponse du moteur illisible. Ouvrez le diagnostic.")
            Regex("""http(?: error)?[: ]+403\b""").containsMatchIn(causeText) || "forbidden" in causeText ->
                result("HTTP_403", "La plateforme refuse la requête ; la raison n’est pas établie.")
            Regex("""http(?: error)?[: ]+401\b""").containsMatchIn(causeText) ->
                result("HTTP_401", "Le service refuse une requête non authentifiée.")
            "unsupported url" in causeText -> result("UNSUPPORTED", "Ce type de lien n’est pas pris en charge.")
            "not available to everyone" in causeText || "can't be seen by certain audiences" in causeText ->
                result("AUDIENCE_RESTRICTED", "Instagram indique une restriction de public pour cette publication. Son accès ne peut pas être confirmé sans session autorisée.")
            auth && (rate || " or " in causeText || "not available" in causeText) ->
                result("ACCESS_UNCERTAIN", "Le moteur évoque plusieurs causes : accès restreint, connexion ou limitation.")
            rate -> result("RATE_LIMIT", "La plateforme limite les requêtes. Réessayez plus tard.")
            auth -> result("AUTH", "Le moteur indique explicitement une session ou un accès privé requis.")
            listOf("unable to resolve host", "name or service not known", "eai_nodata", "temporary failure in name resolution")
                .any { it in text } -> result("DNS", "Le téléphone ne trouve pas l’adresse du service. Essayez en Wi-Fi puis en données mobiles ; vérifiez un éventuel DNS privé ou VPN.")
            "timed out" in text || "timeout" in text -> result("NETWORK_TIMEOUT", "Le réseau ou le service ne répond pas à temps.")
            listOf("unable to resolve host", "name or service not known", "network is unreachable", "connection refused")
                .any { it in text } -> result("NETWORK", "Connexion au service impossible.")
            "ssl" in text || "certificate" in text -> result("TLS", "Échec de connexion sécurisée.")
            "no such file or directory" in text || "exec format error" in text ->
                result("ENGINE_FILE", "Un fichier du moteur est absent ou incompatible.")
            Regex("""http(?: error)?[: ]+404\b""").containsMatchIn(causeText) ->
                result("HTTP_404", "Une ressource du service est introuvable.")
            listOf("no video", "no formats", "requested format", "no media found").any { it in causeText } ->
                result("NO_VIDEO", "Aucune vidéo exploitable. Les photos restent à prendre en charge.")
            "unable to extract" in causeText || "failed to extract" in causeText ->
                result("EXTRACTION", "Le moteur n’arrive pas à lire cette publication.")
            else -> result("UNKNOWN", "Cause non identifiée. Ouvrez le diagnostic pour lire l’erreur technique.")
        }
    }

    fun describe(stage: Stage, error: Throwable): String {
        val info = classify(error)
        val prefix = when (stage) {
            Stage.INIT -> "Initialisation du moteur"
            Stage.ANALYZE -> "Analyse"
            Stage.DOWNLOAD -> "Téléchargement ou enregistrement"
            Stage.UPDATE -> "Mise à jour"
        }
        return prefix + " — " + info.code + " • " + info.message
    }

    fun report(stage: Stage, error: Throwable, appVersion: String, engineVersion: String, platform: String): String {
        val errors = messages(error)
        // Parser exceptions can embed entire private media metadata: never copy their messages.
        val excerpt = if (errors.any { it is EngineOutputException || it.javaClass.name == "org.json.JSONException" })
            "Réponse JSON illisible ; métadonnées non recopiées. Taille : " +
                (errors.filterIsInstance<EngineOutputException>().firstOrNull()?.characterCount?.toString() ?: "inconnue")
        else redact(errors.joinToString("\n") { it.javaClass.simpleName + ": " + it.message.orEmpty() })
        return "HOME-DL / DIAGNOSTIC\nApplication : " + appVersion +
            "\nMoteur : " + engineVersion +
            "\nPlateforme : " + platform + "\nÉtape : " + stage.name +
            "\nCode : " + classify(error).code + "\n\n" + excerpt +
            "\n\nDiagnostic local. Relisez ce texte avant de le partager."
    }

    /** Bounded, masked local excerpt. No automatic remote collection. */
    fun redact(raw: String): String {
        var text = raw.take(16000)
        text = text.replace(Regex("""(?i)https?://[^\s<>"']+"""), "[lien masqué]")
        text = text.replace(Regex("""(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b"""), "[adresse masquée]")
        text = text.replace(Regex("""(?im)\b(authorization|cookie|set-cookie|password|passwd|access_token|refresh_token|token|sessionid|csrftoken)\s*[:=][^\r\n]*"""), "[secret masqué]")
        text = text.replace(Regex("""(?i)\bbearer\s+[a-z0-9._~+/=-]+"""), "[secret masqué]")
        text = text.replace(Regex("""\b(?:\d{1,3}\.){3}\d{1,3}\b"""), "[adresse réseau masquée]")
        text = text.replace(Regex("""[A-Za-z]:\\[^\s"']+|(?<![A-Za-z0-9])/(?:[^\s"']+)"""), "[chemin masqué]")
        text = text.replace(Regex("""(?i)\[(instagram|tiktok|twitter|x)\]\s*[^:\s]+:"""), "[extracteur]:")
        text = text.replace(Regex("""(?<!\w)@[A-Za-z0-9_.-]+"""), "[compte masqué]")
        text = text.replace(Regex("""\b[A-Za-z0-9_-]{32,}\b"""), "[identifiant masqué]")
        return text.take(4000)
    }
}
