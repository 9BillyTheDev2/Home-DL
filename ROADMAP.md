# Home-DL — Roadmap et suivi

Mis à jour le 24 octobre 2026.

## Objectif validé

APK Android personnelle, autonome sur téléphone, sans publicité, sans serveur personnel et sans compte Home-DL. Coller un lien ou le partager vers l’application, afficher les médias accessibles avec aperçu, sélectionner les éléments et télécharger la meilleure qualité accessible. Photos, vidéos et publications mixtes sont dans le périmètre. Instagram, TikTok et X/Twitter sont prioritaires ; les autres sites seront ajoutés selon les essais.

Aucun filigrane ajouté par Home-DL. Privilégier un fichier sans filigrane lorsque la source le propose ; aucune promesse de retrait des textes ou logos déjà incrustés. Les restrictions techniques des plateformes restent applicables. Première version sans connexion aux plateformes ; un accès bloqué doit produire un message clair, sans boucle de tentatives.

## État réel

- [x] Nom, fonctionnement autonome et périmètre validés.
- [x] Dépôt privé créé par le propriétaire ; accès et README vérifiés.
- [x] Références identifiées : Seal, YTDLnis, yt-dlp Android, gallery-dl.
- [ ] Audit du code et des licences des composants retenus.
- [ ] Choix technique confirmé par compilation et extraction réelle.
- [x] Code initial du prototype vidéo Android préparé (non validé sur appareil).
- [x] Première APK compilée, installée et ouverte sur téléphone ; extraction non validée.

Prototype 0.1 : compilation GitHub réussie au deuxième lancement, installation et ouverture confirmées sur téléphone. Premier essai Instagram en échec ; nature exacte du média et cause technique non établies. Aucun téléchargement réel validé. Voir docs/TESTS.md.

## Lot 1 — Faisabilité technique

1. Examiner les intégrations Android de https://github.com/JunkFood02/Seal et https://github.com/deniscerri/ytdlnis ainsi que https://github.com/yausername/youtubedl-android.
2. Vérifier séparément les photos et carrousels avec https://github.com/mikf/gallery-dl ; ne pas considérer sa disponibilité sur ordinateur comme une validation Android.
3. Retenir des versions explicites, consigner les licences et les éventuelles adaptations nécessaires.
4. Préparer un projet Android Kotlin minimal et une compilation manuelle GitHub Actions. Garder les dépenses désactivées et limiter les exécutions.
5. Tester des liens publics : vidéo, photo seule, carrousel et publication mixte lorsque la plateforme le permet. Consigner les échecs et les demandes de connexion.

Critère de sortie : APK installable et extraction réelle documentée. Une miniature seule ne compte pas comme une photo téléchargée. Chaque plateforme et chaque type de média obtient un statut distinct : validé, échec ou non testé.

## Lot 2 — Parcours utilisateur

- Champ de lien, bouton Analyser et réception du partage Android.
- Grille des médias : aperçu, photo/vidéo, dimensions et qualité lorsqu’elles sont connues.
- Sélection individuelle ou globale, sans charger les fichiers entiers pour afficher les aperçus.
- États explicites : analyse, résultat partiel, aucun média, connexion requise, échec réseau, site non pris en charge.
- Délais bornés et annulation ; pas de tentatives infinies.

## Lot 3 — Téléchargement et stockage

- Meilleure qualité accessible ; conserver l’audio des vidéos.
- Téléchargements hors du thread d’interface, progression et annulation.
- Enregistrement via les mécanismes Android adaptés ; fichiers visibles dans les applications de médias compatibles.
- Nettoyer les fichiers temporaires ; ne pas annoncer un téléchargement réussi avant finalisation.
- Prévoir le comportement écran éteint, manque de stockage et coupure réseau.

## Lot 4 — Livraison

- Tester installation, permissions, aperçus, photos, carrousels, vidéos avec audio et annulation sur le téléphone.
- Fournir une APK de test clairement identifiée et une matrice de compatibilité réelle.
- Définir la signature durable avant les mises à jour distribuées ; ne jamais committer une clé de signature ni des cookies.
- Élargir ensuite les sites et étudier une connexion facultative uniquement si nécessaire et validée par le propriétaire.

## Architecture cible à confirmer au Lot 1

- Interface Kotlin : saisie, grille et états de progression.
- Analyse de lien : validation HTTP(S), plateforme et résolution du lien partagé.
- MediaExtractor : renvoie une liste normalisée de médias, indépendamment de l’interface.
- Adaptateurs vidéo et galerie : versions isolées, erreurs traduites pour l’utilisateur.
- DownloadManager interne : sélection de qualité, exécution, annulation et fichiers temporaires.
- MediaStoreWriter : finalisation du fichier dans le stockage Android.

Les médias et les liens restent traités sur le téléphone. GitHub sert au code, au suivi et à la compilation, pas de relais de téléchargement.

## Suivi — prototype 0.1

Structure Kotlin, analyse vidéo, aperçus, sélection, téléchargement temporaire, copie MediaStore et annulation préparés. Workflow de compilation manuel ajouté, sans déclenchement automatique. Photos/carrousels restent à implémenter ; aucun test réel ne doit être marqué réussi avant vérification. Données personnelles retirées des fichiers courants.

## Suivi — première compilation

Première exécution échouée pendant la préparation du SDK, avant compilation du code. Cause confirmée dans les journaux : le paquet SDK `tools` demandé par défaut est introuvable. Workflow corrigé pour demander explicitement `platform-tools`, `platforms;android-35` et `build-tools;35.0.0`. Nouvelle compilation manuelle nécessaire sur main ; ne pas relancer l’ancien commit. Aucun test de compilation Kotlin ou de téléchargement n’est encore validé.

## Suivi — diagnostic 0.1.1

Premier essai sur téléphone : application ouverte, analyse Instagram échouée avec un message trop général. Aucune conclusion possible sur une session obligatoire ou sur le type photo/vidéo. Version 0.1.1 : messages fixes par étape et catégorie d’erreur, sans exposer de lien, chemin ou erreur brute ; mise à jour manuelle du moteur yt-dlp ; marges en dp et prise en compte des barres système. Compilation de cette version et nouveau test à effectuer. La mise à jour utilise GitHub pour le moteur uniquement et n’envoie pas le lien saisi. Les mises à jour moteur ne peuvent pas être interrompues immédiatement par l’API amont ; le délai empêche d’annoncer une réussite tardive mais n’arrête pas nécessairement ce transfert.

## Suivi — mises à jour 0.1.2

Code de mise à jour automatique du moteur préparé : vérification à l’ouverture, 24 h entre succès, 6 h après échec, interrupteur et contrôle manuel ; HTTPS, origine, taille et SHA-256 vérifiés, remplacement atomique. Aucun lien saisi transmis lors de cette vérification. Compilation et tests appareil de cette version restent à exécuter. Le moteur existant est conservé si la vérification ou le téléchargement échoue.

Mises à jour APK non activées : signature permanente et destination de distribution encore à définir. Le propriétaire a ensuite validé un dépôt unique public pour le code et les APK ; voir docs/UPDATES.md. Le changement de visibilité reste à effectuer. Aucun secret créé ou ajouté.

Compilation signée préparée dans Gradle et dans le workflow manuel : option debug conservée, option signed-release conditionnée à quatre secrets. Aucune signature permanente configurée et aucune publication exécutée.

## Suivi — préparation du dépôt public unique

Choix validé : conserver Home-DL comme dépôt unique et le rendre public. Audit des sept commits et des trente-cinq versions de fichiers ; mention personnelle ancienne remplacée dans l’historique principal en conservant les étapes du projet. Voir docs/PRIVACY_REVIEW.md pour le périmètre et les limites des objets GitHub conservés hors branche. Le compte propriétaire reste visible. Changement de visibilité non disponible via la connexion utilisée ; à effectuer dans Settings. Signature permanente, publication de release et installation des mises à jour APK restent à finaliser.

## Suivi — visibilité publique confirmée

Le propriétaire a changé la visibilité ; vérification des métadonnées GitHub : dépôt public. Distribution prévue dans ce même dépôt. Signature permanente non configurée, aucune release APK publiée par cette étape et mise à jour APK intégrée non implémentée. La compilation signée est préparée mais les quatre secrets requis restent à renseigner via les paramètres GitHub, opération non disponible dans la connexion utilisée.

## Suivi — changement de compte propriétaire

Copie publique indépendante importée sur le compte de développement demandé. Avant modification du suivi, comparaison avec le dépôt source : 20 fichiers identiques par chemin et SHA, 10 commits identiques et même tête de branche. Code et historique principal conservés. Les exécutions Actions et leurs artefacts ne sont pas transférés par l’import Git. Confidentialité de l’adresse de commit activée pour le compte de destination. L’ancien dépôt n’est pas supprimé : sa suppression exige l’accès au compte propriétaire d’origine. La signature permanente et la mise à jour APK restent à finaliser sur le nouveau dépôt.
