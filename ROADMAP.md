# Home-DL — Roadmap et suivi

Mis à jour le 24 octobre 2026.

**État actuel :** code 0.2.0 compilé, tests/lint réussis et téléchargement réel Instagram public confirmé sur JVM. Signature permanente configurée ; première publication signée en cours ; ne pas demander d’installation du build debug pour cette transition. Les mises à jour APK et MediaStore ne sont pas encore validés sur téléphone.

## Dernière version testée — moteur et accès (0.1.5)

Captures reçues : la vérification moteur échoue avec UnknownHostException sur api.github.com ; l’analyse Instagram renvoie plusieurs hypothèses (contenu absent, limitation, connexion). Aucun téléchargement Instagram validé. Le lien de publication présenté ne permet pas de conclure que le média est une vidéo ; photos/carrousels restent non implémentés.

Correction préparée : moteur officiel yt-dlp 2026.10.21 intégré lors de la compilation avec SHA-256 épinglé, puis installé atomiquement au premier lancement. Un moteur déjà plus récent est conservé. Les vérifications distantes passent désormais par github.com et les sommes officielles de la même release, sans dépendre de api.github.com. Taille, HTTPS et origines autorisées restent vérifiés. Erreur DNS distincte et avertissements du moteur conservés dans le diagnostic local masqué. Tests de lecture des sommes et de classement DNS/restriction ajoutés. Les douze tests unitaires, assembleDebug et lintDebug réussissent dans l’exécution 40113703258 ; APK ARM64 produite. Essai téléphone restant à effectuer.

Priorités restantes : valider une vidéo réelle avec le moteur récent ; déterminer si un accès connecté facultatif est nécessaire pour Instagram ; implémenter photos/carrousels ; signature permanente et mises à jour APK. Ne pas présenter cette correction réseau comme une résolution des restrictions Instagram.

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

Copie publique indépendante importée sur le compte de développement demandé. Avant modification du suivi, comparaison avec le dépôt source : 20 fichiers identiques par chemin et SHA, 10 commits identiques et même tête de branche. Code et historique principal conservés. Les exécutions Actions et leurs artefacts ne sont pas transférés par l’import Git. Confidentialité de l’adresse de commit activée pour le compte de destination. L’ancien dépôt a été supprimé par le propriétaire ; son ancien chemin renvoie désormais 404 via la connexion du compte d’origine. La signature permanente et la mise à jour APK restent à finaliser sur le nouveau dépôt.

## Suivi — ancien dépôt supprimé

Suppression confirmée par le propriétaire et ancien chemin inaccessible (404). Le dépôt public du compte de développement reste accessible, ainsi que la roadmap. Cette copie devient l’unique dépôt de travail. Signature permanente et mises à jour APK restent à configurer.

## Suivi — interface rétro-futuriste 0.1.3

Direction visuelle validée : inspiration terminal numérique et machine temporelle, noir profond, vert/cyan et orange, typographie monospace, cadrans source/connexion/destination et cartes médias. Interface isolée dans RetroScreen pour garder la logique d’extraction dans MainActivity. Pluie de code confinée à l’en-tête, environ 12 images/s, arrêt en arrière-plan et respect du réglage Android des animations ; interrupteur mémorisé pour la désactiver. Réglages moteur repliables, aperçu vide, actions en bas, marges système et zones tactiles de 48 dp minimum. Aucun logo ni image de film intégré. Compilation assembleDebug et lintDebug réussis, APK de test produite. Rendu apprécié sur téléphone par le propriétaire ; signature permanente et mises à jour APK restent en cours de configuration.

## Suivi — diagnostic et saisie 0.1.4

Essais utilisateur : AUTH sur une publication et UNKNOWN sur une vidéo ; aucun téléchargement validé. Correction du classement : aide générique sur les cookies ignorée, refus HTTP et erreurs moteur distingués, hypothèses multiples explicitement signalées. Bouton rouge de remise à zéro, champ de lien multiligne, état d’échec distinct. Diagnostic technique local consultable et copiable à la demande : version application/moteur, plateforme générique, étape, code et extrait borné avec masquage des liens, adresses, chemins et secrets courants. Pas de collecte distante ni de stockage persistant du diagnostic ; relecture demandée avant partage. Les erreurs JSON ne recopient pas les métadonnées des médias. Huit tests de régression et confidentialité exécutés avec succès, ainsi que assembleDebug et lintDebug ; APK de test produite. Essai sur téléphone et diagnostic réel encore attendus. Cette version améliore l’investigation ; elle ne prouve pas que les restrictions des plateformes sont résolues. Signature permanente et mises à jour APK restent à finaliser.

## Suivi — recherche sans connexion

Comparaison des méthodes existantes et test réel d’une requête Instagram publique anonyme : produit média et trois variantes vidéo obtenus sans compte ni serveur de téléchargement tiers, dans l’environnement de développement. Une dépendance absente dans l’intégration Android actuelle empêche yt-dlp d’exécuter une requête anonyme importante. Un adaptateur Instagram public dédié est la piste retenue avant toute connexion obligatoire. Voir docs/INSTAGRAM_RESEARCH.md pour les preuves, les sources et les limites. Aucun changement de moteur, aucune nouvelle APK et aucune validation téléphone par cette étape. Photos/carrousels restent à implémenter.

## En préparation — 0.2.0, Instagram public et mises à jour APK

Adaptateur Instagram anonyme intégré au code : liens de publications/reels/TV publics, session technique en mémoire, contrôle d’accès puis requête dédiée, photos/vidéos/carrousels ordonnés et meilleure variante directe par dimensions. Les couvertures vidéo ne deviennent pas des photos de substitution. Téléchargement direct CDN borné et vérification du format avant MediaStore ; URLs privées non conservées dans le dépôt. Les autres sites conservent yt-dlp. Compilation Android, lint et tests JVM réussis sur le commit aa5a45f2a82fa6f993c2f56071f1fef5f5d1c8ca (exécution 40126888285). Le même adaptateur Kotlin a analysé et téléchargé un MP4 Instagram public de 918 120 octets sans compte lors de l’essai JVM. Photos/carrousels vérifiés par tests de parsing synthétiques ; essais Android restant à effectuer. Qualité : meilleure variante progressive exposée ; pas de sélection DASH spécifique dans cet adaptateur.

Code de mise à jour APK ajouté : recherche automatique à l’ouverture (6 h entre tentatives), bouton de vérification, annonce d’une version disponible, téléchargement après choix utilisateur, contrôle SHA-256/package/version/certificat identique, puis confirmation Android. Source unique : releases publiques de ce dépôt. Interrupteur pour la recherche automatique. Le moteur garde son propre mécanisme.

Publication signée préparée dans le workflow manuel (signed-release + publish). Aucune clé créée ni aucun secret ajouté par cette étape. Il faut enregistrer une clé permanente hors des sources avant la première release, puis tester deux mises à jour successives sur appareil. Les APK de test précédentes ne partagent pas nécessairement cette signature : une transition manuelle reste nécessaire. Ne pas distribuer une nouvelle APK debug comme solution aux réinstallations répétées.

## Suivi — signature permanente autorisée

Clé RSA 3072 bits, PKCS12, certificat CN=Home-DL sans renseignement personnel. Quatre secrets Actions enregistrés sur le dépôt avec l’accord du propriétaire ; sauvegarde privée conservée hors des sources. Empreinte publique dans docs/SIGNING_CERTIFICATE.md. Gradle précise maintenant le format PKCS12. Compilation signed-release et publication de v0.2.0 à vérifier avant livraison. Aucune clé ni mot de passe ajouté au Git.
