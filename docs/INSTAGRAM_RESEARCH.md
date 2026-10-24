# Recherche — Instagram public sans connexion

24 octobre 2026. Recherche et test de faisabilité ; aucune nouvelle APK publiée par cette étape.

## Résultat vérifié

Une requête anonyme complète a été testée dans l’environnement de développement sur une vidéo publique figurant dans les tests amont de yt-dlp : accès public accepté, produit média renvoyé, trois variantes vidéo, aucun cookie de compte sessionid. Aucun serveur de téléchargement tiers, proxy ajouté ou compte Instagram utilisé. Des cookies techniques anonymes sont obtenus auprès d’Instagram ; « sans connexion » ne signifie pas « sans aucun cookie ».

Ce test valide la réponse de métadonnées sur une vidéo précise depuis cet environnement. Il ne valide pas l’extraction sur Android, le téléchargement des fichiers de cette vidéo, toutes les publications publiques, les photos/carrousels ni les restrictions réelles de compte, d’âge ou de région.

## Différence avec Home-DL 0.1.5

Lecture du code officiel yt-dlp 2026.10.21 : la requête GraphQL anonyme Polaris est conditionnée par _can_impersonate. L’archive Python ARM64 officielle de youtubedl-android 0.18.1 a été inspectée : Python 3.12, aucune entrée curl_cffi ni libcurl. Home-DL ne fournit pas ces composants en complément. La requête est donc omise dans cette intégration ; remplacer seulement le programme Python yt-dlp ne fournit pas la dépendance native manquante.

Le test indépendant a utilisé les requêtes anonymes sans ce composant et a obtenu un résultat. Cela démontre une possibilité technique, sans garantir un résultat identique sur le téléphone. L’erreur de restriction issue d’un chemin d’extraction ne doit pas être interprétée comme une preuve définitive qu’un compte est nécessaire pour cette publication.

## Ce que font les outils étudiés

- FastDL indique un traitement sur ses serveurs et ne demande pas le compte de l’utilisateur. Son code serveur n’a pas été audité ; aucune conclusion sur ses éventuels comptes internes ou autres mécanismes.
- parth-dl implémente une session anonyme, un contrôle d’accessibilité et une requête Polaris ; il sait lire les variantes vidéo, images et enfants de carrousel. Son code ne doit pas être importé sans vérifier sa licence et ses erreurs/répétitions.
- InstaDownload propose une extraction Android directe de données publiques de page et le traitement des photos/carrousels. Certaines parties utilisent une identité de robot de recherche et des journaux contenant les données des médias ; elles ne sont pas retenues pour Home-DL.

## Correction retenue pour la suite

Préparer un adaptateur Instagram public indépendant de l’interface : session anonyme en mémoire, requêtes bornées, contrôle d’accès, lecture des médias explicitement exposés, sélection des versions disponibles. Prévoir photos, vidéos et carrousels ; ne jamais transformer la couverture d’une vidéo en photo téléchargée à sa place. Vérifier domaine, redirections, type et taille des fichiers. Conserver yt-dlp pour les autres sites.

Arrêter sur un refus explicite, une demande de connexion, une limitation ou une vérification humaine ; pas de boucle, pas de rotation d’adresses ni de service tiers implicite. Une connexion restera facultative pour les contenus auxquels l’utilisateur a accès, après les essais anonymes.

La méthode doit être validée séparément sur Android avant d’annoncer une prise en charge fonctionnelle.

## Sources inspectées

- https://fastdl.app/en5IW
- https://github.com/yt-dlp/yt-dlp/blob/2026.10.21/yt_dlp/extractor/instagram.py
- https://github.com/yausername/youtubedl-android/tree/0.18.1/library/src/main/jniLibs/arm64-v8a
- https://github.com/parthmax2/parth-dl/blob/main/parth_dl/extractors.py
- https://github.com/Orang-Studio/InstaDownload/blob/main/InstaDownload/app/src/main/java/com/vakarux/instadownload/InstagramDownloader.kt

Les branches main des projets tiers évoluent. Aucun lien de publication fourni par l’utilisateur, cookie, jeton anonyme ou réponse contenant des données de compte n’est conservé dans ce document.
