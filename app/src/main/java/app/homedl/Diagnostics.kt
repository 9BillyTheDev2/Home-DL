package app.homedl

import java.util.Locale

enum class Stage { INIT, ANALYZE, DOWNLOAD, UPDATE }

/** Only fixed messages leave this boundary: never expose URLs, cookies, paths or raw errors. */
object Diagnostics {
    fun describe(stage: Stage, error: Throwable): String {
        val text = generateSequence(error) { it.cause }.take(5)
            .joinToString(" ") { it.message.orEmpty() }.lowercase(Locale.ROOT)
        val detail = when {
            "no space left" in text || "enospc" in text -> "SPACE • Espace de stockage insuffisant."
            "permission denied" in text || "eacces" in text -> "PERMISSION • Accès à un fichier refusé."
            "unrecognized arguments" in text || "no such option" in text ->
                "ENGINE_OPTIONS • Le moteur ne reconnaît pas une option. Mettez-le à jour."
            "429" in text || "too many requests" in text || "rate-limit" in text ->
                "RATE_LIMIT • La plateforme limite les requêtes. Réessayez plus tard."
            "login required" in text || "login_required" in text || "log in" in text ||
                "sign in" in text || "authentication" in text || "cookies" in text ->
                "AUTH • Le moteur signale une session requise ou une restriction d’accès."
            "403" in text || "forbidden" in text ->
                "HTTP_403 • La plateforme refuse la requête. La cause précise reste à vérifier."
            "404" in text || "not found" in text ->
                "NOT_FOUND • Une ressource demandée est introuvable."
            "timed out" in text || "timeout" in text ->
                "NETWORK_TIMEOUT • Le réseau ou la plateforme ne répond pas à temps."
            "unable to resolve host" in text || "name or service not known" in text ||
                "network is unreachable" in text || "connection refused" in text ->
                "NETWORK • Connexion au service impossible."
            "ssl" in text || "certificate" in text ->
                "TLS • Échec de connexion sécurisée."
            "unsupported url" in text ->
                "UNSUPPORTED • Ce lien n’est pas pris en charge par le moteur."
            "no video" in text || "no formats" in text || "requested format" in text ->
                "NO_VIDEO • Aucune vidéo exploitable. Une publication photo n’est pas encore prise en charge."
            "unable to extract" in text || "failed to extract" in text ->
                "EXTRACTION • Le moteur ne parvient pas à lire cette publication. Essayez sa mise à jour."
            else -> "UNKNOWN • Cause non identifiée. Ce code ne permet pas de conclure à une connexion obligatoire."
        }
        val prefix = when (stage) {
            Stage.INIT -> "Initialisation du moteur"
            Stage.ANALYZE -> "Analyse"
            Stage.DOWNLOAD -> "Téléchargement ou enregistrement"
            Stage.UPDATE -> "Mise à jour"
        }
        return "$prefix — $detail"
    }
}
