# Home-DL — Roadmap et suivi

Mis à jour le 25 octobre 2026.

**État actuel :** 0.2.1 installée confirmée par le propriétaire. Des analyses Instagram réussissent (dont un carrousel de 20 médias) et d’autres renvoient HTTP_404. Vidéos X/TikTok fonctionnelles selon ses essais ; photos non prises en charge. Priorité au fonctionnement sur téléphone, sans serveur intermédiaire ni connexion aux comptes. 0.2.4 publiée : récupération photo dédiée X/TikTok, trace locale des étapes en échec et progression visible des mises à jour APK.

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

Clé RSA 3072 bits, PKCS12, certificat CN=Home-DL sans renseignement personnel. Quatre secrets Actions enregistrés sur le dépôt avec l’accord du propriétaire ; sauvegarde privée conservée hors des sources. Empreinte publique dans docs/SIGNING_CERTIFICATE.md. Gradle précise maintenant le format PKCS12. Compilation signed-release et publication v0.2.0 réussies (exécution 40127772969, commit 48f07ea75bdbd9e343c1ad086c7baf20978f9b96). APK publique de 65 039 980 octets, somme SHA-256 et route de mise à jour vérifiées. Signature APK v2 contrôlée avec la bibliothèque officielle apksig 8.7.3 ; certificat conforme à l’empreinte permanente. Aucun fichier de clé ni mot de passe de signature détecté dans l’APK. Aucune clé ni mot de passe ajouté au Git.

## Suivi — première release permanente livrée

v0.2.0 publiée avec Home-DL.apk et update.json dans le même dépôt. Route latest/download/update.json vérifiée après publication ; versionCode 7, versionName 0.2.0, URL et taille cohérentes, SHA-256 vérifié sur les octets réellement téléchargés. Publication et contrôles terminés ; première installation manuelle puis futur parcours de mise à jour/conservation des réglages à vérifier sur téléphone. Le test de téléchargement Instagram public est confirmé sur JVM ; photos/carrousels et MediaStore restent à valider sur appareil. Pas de mise à jour silencieuse : Android conserve la confirmation d’installation.

## Suivi — 0.2.1, carrousel Instagram et erreurs de requête

Un essai utilisateur 0.2.0 a échoué avec une réponse publique vide, classée UNKNOWN. Sur le lien transmis (non conservé dans le dépôt), les deux requêtes testées depuis l’environnement de développement rendent un carrousel de quatre photos. Cela ne reproduit pas le refus sur le téléphone ; la cause réseau/serveur précise reste inconnue. La page HTML publique testée n’expose pas ces médias : aucune extraction HTML spéculative ajoutée.

Correction des limites confirmées : requête publique par shortcode utilisée par Instaloader en première intention, requête Polaris en alternative au maximum une fois si la réponse est vide ou signale une erreur de requête. Arrêt immédiat sur limitation ou accès explicitement refusé. Vérification que les données correspondent au post demandé. Les erreurs portent désormais des codes Instagram dédiés et un résumé technique sans texte serveur, lien, auteur ni cookie.

Code Kotlin de production compilé et testé localement sur le lien exact avec le routage réseau standard de l’environnement : quatre photos téléchargées et formats vérifiés, 1440 × 1920, tailles 403184 / 383260 / 248864 / 268766 octets. Les fichiers d’image n’ont pas été conservés. Source et réponses de l’utilisateur uniquement dans les fichiers temporaires locaux, jamais dans les tests ou journaux GitHub publics. Exécution 40136132751 réussie sur df1fa4ab5b632d31fad30c732ac4a1686df33db8 : tests JVM, assembleRelease et lintRelease, vérification de signature et publication v0.2.1. La vidéo de régression publique a également été téléchargée avec le même code Kotlin (918120 octets, MP4 vérifié). Première mise à jour via l’app et confirmation du résultat sur téléphone restent à valider.

Après publication 0.2.1 : route de mise à jour de l’app accessible, versionCode 8, APK de 65 048 088 octets, SHA-256 dab50262f2d38059c628a850adb02df29f7c21d78596d76b24c2dc9842210061 vérifié. Signature v2 validée avec apksig 8.7.3, certificat permanent identique à celui de 0.2.0. Aucun lien utilisateur dans le code, les tests, les notes de release ou les journaux Actions. Les réponses temporaires de la publication ont été supprimées.


## Suivi — 0.2.2, traces locales

Trace par opération, uniquement en mémoire, bornée à 64 événements, effacée au début de l’opération suivante et à la fermeture du processus. Étapes Instagram (référence, session, accès, shortcode, Polaris, parsing), moteur pour X/TikTok, téléchargement, durée relative, codes HTTP, type de réponse limité à des catégories fixes, tailles et nombre/type de médias. Aucun URL, cookie, jeton, corps de réponse ou en-tête brut dans la trace. Rapport disponible après analyse réussie comme après échec pour comparaison ; copie volontaire uniquement. Les diagnostics précédents ne peuvent pas être enrichis rétroactivement.

Cinq tests nouveaux : phase exacte d’un HTTP_404, exclusion des contenus privés, taille maximale/réinitialisation, isolation entre threads, rapport de succès/vide. Suite locale ciblée de 29 tests réussie ; compilation Android et publication réussies (exécution 40178715899). Aucun nouveau mode d’extraction, serveur ou connexion sociale ajouté. Prochaines étapes : comparer les rapports sur téléphone, récupération des photos X/TikTok, progression visuelle analyse/téléchargement.

Validation 0.2.2 : commit a290189f240321f84d0679290ed225c4065b2344, testDebugUnitTest (42 tests dans les sources), assembleRelease et lintRelease réussis. Le code Kotlin hors Android a reproduit le refus du lien fourni : session HTTP 200, contrôle d’accès HTTP 404 (JSON, 51 octets annoncés), trace conforme et sans URL. APK publiée de 65 059 300 octets, SHA-256 613541d424b4fd81677cffb578c5b6cd3dd9db219b63c1134130b3de0d895f71 ; route latest version 9/0.2.2 et signature v2/certificat permanent vérifiés sur le fichier réellement téléchargé. Rapport sur téléphone restant à valider.


## Suivi — 0.2.3, progression de la mise à jour APK

Le propriétaire ne voit aucune réaction après le téléchargement demandé. Limites confirmées dans le code : pourcentage affiché dans le bloc d’état éloigné de la carte de mise à jour, aucune barre, callback dépendant du Content-Length. Cause du téléchargement non abouti sur téléphone non établie. Ajout d’une barre horizontale et d’un statut immédiat à 0 % dans la carte de mise à jour, progression fondée sur la taille du manifeste déjà validé, notifications limitées aux changements de pourcentage, phase de vérification distincte à 100 %, erreur ou annulation visible dans cette même carte et bouton de nouvelle tentative. Les contrôles d’intégrité et de signature restent obligatoires. Compilation/tests/lint et publication réussis (exécution 40185675685). Aucun changement de récupération des médias.

Validation 0.2.3 : testDebugUnitTest, assembleRelease et lintRelease réussis sur bd7698a06ac22ab39b14fa88acacd3979ce784d4. APK de 65 061 668 octets réellement téléchargée, SHA-256 e10b5e8a20bd870df3e79db5dee5149513998ea3c1d4036d821b01f139cabf91 conforme au manifeste latest 10/0.2.3. Signature v2 et certificat permanent vérifiés par apksig. Affichage et téléchargement intégré sur téléphone à valider.


## Suivi — 0.2.4, photos publiques X et TikTok

Méthodes photo dédiées, exécutées seulement si le moteur vidéo retourne NO_VIDEO ou UNSUPPORTED ; arrêt conservé sur autres erreurs, refus HTTP ou annulation. X : métadonnées publiques de syndication, identité du post vérifiée, images top-level uniquement, variante CDN originale et aperçu réduit. Refus des collections mixtes dans ce mode de secours plutôt que de substituer une couverture vidéo. TikTok : résolution du lien court, normalisation photo vers route publique vidéo comme gallery-dl, lecture du bloc imagePost et contrôle de l’identité. URL signée CDN gardée seulement en mémoire. Transferts limités aux CDN de chaque plateforme, HTTPS, tailles/délais bornés et format JPEG/PNG/WebP vérifié avant MediaStore. Aucun compte, serveur intermédiaire ou traitement d’image ajouté.

Essai réel du code Kotlin depuis l’environnement de développement, avec son routage standard : une photo X 1168 × 784 (163004 octets) et deux photos TikTok 1080 × 1920 (502891 octets) et 608 × 1080 (202450 octets), tous JPEG vérifiés puis supprimés. Aucun lien ou contenu utilisateur dans le dépôt. Les vidéos gardent leur parcours moteur existant ; non retestées sur appareil par cette étape. 11 tests des adaptateurs ajoutés (identité, ordre, dimensions, absence de substitution de couverture, origines CDN et données absentes/refusées), plus classement diagnostic photo. Suite locale de 41 tests réussie ; compilation Android et publication réussies (exécution 40192618900).

Validation 0.2.4 : tests JVM (54 méthodes dans les sources), assembleRelease, lintRelease et publication réussis sur 518240df9e24ac18b920b03cbd94221f82d33248. Manifeste latest 11/0.2.4 et APK de 65 077 036 octets réellement téléchargée, SHA-256 688241d4324c9c9e209b0fdd1b26342337f3ec1a2b7a1d0cea0fa7a7b48dc43e conforme. Signature v2 et certificat permanent vérifiés. Les trois photos citées valident les adaptateurs Kotlin hors appareil, sans démontrer la compatibilité universelle. Aperçus, sélection et MediaStore à valider sur téléphone.


## Suivi — 0.2.5, progression fixe et diagnostics

Zone de progression hors défilement, au-dessus des boutons : analyse avec étape réelle et durée (indéterminée tant que le volume total n’est pas connu), téléchargement avec pourcentage du fichier courant si sa taille est connue, sinon animation ; enregistrement distinct ; vérification moteur, recherche de version et téléchargement APK visibles. Arrêt de l’animation en fin, erreur ou annulation. Notifications des transferts directs limitées aux changements de pourcentage. Aucun pourcentage d’analyse inventé.

Instagram : le 404 de la route auxiliaire de contrôle ne suffit plus à arrêter l’analyse ; les deux requêtes publiques peuvent établir un diagnostic plus précis. Un refus explicite ou une limitation reste bloquant. Sur les deux nouveaux liens fournis, les essais JVM préalables reproduisent le 404 puis des réponses sans média aux deux requêtes : cette version ne prétend pas les rendre téléchargeables. X : réponse vide, identité différente et absence de photos différenciées ; support de la forme publique photos quand mediaDetails est absent, sans utiliser de couverture vidéo. Le précédent lien photo X donne encore un JPEG de 163004 octets dans notre essai ; le nouveau lien en échec est nécessaire pour reproduire sa régression. Liens et réponses utilisateur exclus du dépôt.

44 tests JVM locaux ciblés réussis, dont observateur des étapes, réponse X vide et variante photos. Tests Android/compilation/lint et affichage sur téléphone restent à vérifier avant de conclure.


Validation 0.2.5 : exécution 40213805737 réussie sur 52f2733177cb079ef0c951e632dde92dd803a301 ; testDebugUnitTest, assembleRelease, lintRelease et publication. APK réellement téléchargée : 65086212 octets ; SHA-256 b410f88bdac573d5d14382e44f0ab8291cd97a19f0408f3f189032595dff239f conforme au manifeste latest 12/0.2.5. Signature v2 et certificat permanent vérifiés par apksig. Le nouveau code reproduit sur JVM la vidéo Instagram en échec : accès auxiliaire HTTP 404, requête shortcode HTTP 200 sans produit (codes 1675030), puis Polaris HTTP 200 sans produit ; diagnostic IG_EMPTY. Cette publication reste non récupérée. Le code de progression Android compile mais n’a pas été exécuté sur appareil dans cet environnement. Le propriétaire avait confirmé les photos X/TikTok en 0.2.4 puis signalé un nouvel échec X, encore non reproductible faute du lien concerné.


## Investigation Instagram — comparaison des extracteurs, 25 octobre 2026

Lecture directe des sources actuelles d’Instaloader (instaloadercontext.py) et Cobalt (api/src/processing/services/instagram.js), ainsi que de la modification Cobalt #1577. Instaloader utilise le même doc_id et la route graphql/query. La variante locale de Home-DL utilisant cette route à la place de api/graphql a été testée sur les deux publications en échec : même résultat sans produit, codes 1675030 sur SHORTCODE, absence de produit sur POLARIS. Pas d’amélioration démontrée, donc aucun changement de route publié.

Essais supplémentaires hors Android, sans compte, avec cookies anonymes éphémères et routage standard de l’environnement : pages embed/captioned HTTP 200 (178568 octets chacune), aucune structure photo/vidéo exploitable identifiée par le parseur de contexte testé ; pages de publication HTTP 200 avec paramètres LSD/SiteData ; bulk-route-definitions HTTP 200 (225 octets) sans identité média/propriétaire ni indication de route privée. La requête de restriction dépendant de ces identifiants n’a pas été exécutée. Ces absences ne prouvent ni suppression ni géoblocage. Aucun corps de réponse ni lien utilisateur dans le dépôt.

Cobalt #1577 ne fournit pas une nouvelle méthode : il réordonne les appels GraphQL et embed. Le signalement ouvert Instaloader #2738 décrit une défaillance partielle de la même requête sur certaines publications ; cela ressemble au symptôme observé sans établir sa cause précise. Cobalt #1587 décrit également des échecs des chemins mobile/embed et le risque de renvoyer une couverture à la place des vrais médias. Aucune image de couverture proposée comme résultat de substitution. Aucun média récupéré lors de ces nouveaux essais, aucune nouvelle APK publiée.

Sources : https://github.com/instaloader/instaloader/issues/2738 ; https://github.com/imputnet/cobalt/issues/1587 ; https://github.com/imputnet/cobalt/pull/1577 ; https://github.com/instaloader/instaloader/blob/master/instaloader/instaloadercontext.py ; https://github.com/imputnet/cobalt/blob/main/api/src/processing/services/instagram.js .


## Suivi — 0.2.6, navigateur Instagram local expérimental

Ajout d’un bouton de secours après IG_EMPTY/IG_QUERY : ouverture visible de la publication dans un WebView Android, JavaScript exécuté sur le téléphone, sans connexion de compte ni service de téléchargement tiers. Processus et répertoire WebView séparés, cookies anonymes et stockage Web effacés avant la page et à la fermeture ; origines HTTPS Instagram/CDN uniquement, accès fichiers et contenus locaux désactivés, aucun pont addJavascriptInterface, aucune permission Web accordée. Arrêt sur navigation vers connexion/challenge/consentement, annulation, erreur ou délai de 45 secondes. Aucune résolution automatique de vérification humaine.

Observation bornée des réponses JSON reçues par fetch/XHR et des scripts JSON de la page. Injection document-start si le WebView installé le permet, lecture des scripts après chargement sinon. Aucun changement des requêtes Instagram ni import de cookies de navigateur. Seuls les objets dont l’identité correspond au post sont conservés ; métadonnées limitées aux médias, contrôle natif de l’identité et des CDN, refus des couvertures vidéo et des carrousels incomplets. Les médias acceptés rejoignent la sélection et le téléchargement local existants. Barre de chargement de page distincte de l’analyse indéterminée. Diagnostic BROWSER_* avec compteurs/étapes fixes, sans HTML, cookies, auteur ou URL.

Validation préparatoire : 49 tests JVM locaux ciblés réussis ; 7 scénarios JavaScript synthétiques et un test de capture fetch préservant la réponse du site réussis. Tests JavaScript ajoutés au workflow Android. L’exécution réelle du WebView sur téléphone n’est pas disponible dans l’environnement : cette version teste une nouvelle méthode et ne prétend pas résoudre les deux liens précédemment en échec. Aucun média récupéré via ce nouveau mode avant validation appareil. Compilation Android, lint, publication et vérification APK à consigner après exécution.


Validation 0.2.6 : première compilation sans publication réussie (40223782526, c6756d3), puis révision finale e6b00c66f912adcb5656a7e8ef54be7aace1f285 validée par 40224038004 : 8 scénarios JS synthétiques et observation fetch préservant la réponse d’origine, testDebugUnitTest, assembleRelease et lintRelease réussis. 49 tests JVM ciblés locaux ; suite complète Android de 62 méthodes dans les sources. Artefact 12610213981 téléchargé et ZIP SHA-256 250c7e97ba920fdd9174b8fc2a02ee9fea86f0c06ef90e06174c4044a6ae3825 vérifié. APK 65216706 octets, SHA-256 406ef599f005548ad0d94ebba37b90c169b6875e4b75aedf0a34cdc3aa664242 conforme au manifeste 13/0.2.6 ; signature v2/certificat permanent validés par apksig. Publication désactivée ; aucune release v0.2.6 ni modification de latest. Le contrôle automatique a refusé la publication publique de cette version expérimentale sans accord spécifique ; seuls construction et tests ont continué. Le mode WebView reste à éprouver sur appareil, sans réussite revendiquée sur les deux publications en échec.


Publication 0.2.6 autorisée explicitement par le propriétaire après présentation de la version expérimentale. L’envoi navigateur initial a expiré et sa session a été réinitialisée ; vérification des releases : aucune v0.2.6 ni brouillon créé par cet essai. Publication achevée via la chaîne existante, exécution 40234001184 sur 0937505fc9b81080021abd2cf370a5375a02d101, build et publish réussis. Release latest v0.2.6 et manifeste 13/0.2.6 vérifiés. APK publique réellement téléchargée : 65216706 octets, SHA-256 e9dc5feba32cafd3bc8f951790866e1c6faa7b9f67f61782c46ede206642ae9d conforme au manifeste et aux métadonnées GitHub ; signature v2/certificat permanent validés avec apksig. Cette empreinte concerne l’APK publiée par cette exécution, distincte de l’artefact de compilation précédent. Code fonctionnel inchangé depuis e6b00c6. Notes publiques et instructions du mode navigateur expérimental ajoutées. Validation réelle WebView sur téléphone toujours attendue ; aucune réussite revendiquée sur les publications en échec.


## Suivi — 0.2.7, arrêt prématuré du navigateur

Deux rapports téléphone en 0.2.6 montrent BROWSER_DOCUMENT_START ENABLED, BROWSER_ANONYMOUS BEGIN puis BROWSER_PAGE BEGIN et IG_BROWSER_NAVIGATION avant BROWSER_PAGE LOADED. Le code utilisait cette même erreur pour une navigation hors de l’origine autorisée et pour un événement de téléchargement détaché ; ces traces ne permettent donc pas d’identifier le déclencheur. Aucune conclusion sur une restriction Instagram ou géographique.

Correction : les cibles non autorisées (dont intent/instagram) restent bloquées, mais seule leur navigation est annulée, sans fermer l’inspection de la page originale ni ouvrir une application externe. Une demande de connexion/challenge reste arrêtée. Les téléchargements détachés ne sont pas acceptés sans identité et ne ferment plus le navigateur. Trace à catégories fixes BROWSER_NAVIGATION IGNORED scheme/origin et BROWSER_DOWNLOAD IGNORED, sans URL, identifiant ni paramètre. Début de l’inspection à onPageCommitVisible, sans attendre la fin de tous les chargements. Limite 45 secondes inchangée.

Tests ciblés de politique de navigation : page Instagram autorisée, accès explicite arrêté, liens d’application et origine externe ignorés, exclusion des secrets dans les traces. Aucun succès de récupération revendiqué avant essai sur téléphone. Compilation Android/publication à consigner après vérification.


Validation 0.2.7 : exécution 40235097810 réussie sur 08120c6ce0531f434888f25c2431b65b8911ea76 ; tests JS (8 scénarios + capture fetch), testDebugUnitTest, assembleRelease, lintRelease et publication. Six tests locaux du résultat/navigation réussis ; suite Android de 63 méthodes dans les sources. Manifeste latest 14/0.2.7 vérifié. APK publique téléchargée : 65218790 octets ; SHA-256 f10009f06d3043ba92e9057eb95a43801ffadbeae87f6ba04edba449794f701d conforme. Une copie locale initiale a été constatée tronquée ; un nouveau téléchargement complet a passé les contrôles taille/SHA puis la vérification apksig dans le même processus. Signature v2 et certificat permanent validés. Notes de release décrivant précisément le comportement et les limites publiées. Le déclencheur exact de la navigation des deux essais 0.2.6 reste inconnu ; les catégories ajoutées permettront de le distinguer. Aucun succès Instagram supplémentaire revendiqué avant essai appareil.


## Suivi — retrait du navigateur local, 26 octobre 2026

À la demande du propriétaire : retrait complet du secours WebView Instagram (bouton, activité/processus séparé, capture JavaScript, retour de résultat, dépendance AndroidX WebKit et tests dédiés). Le parcours direct et les diagnostics existants restent disponibles ; aucune nouvelle réussite Instagram revendiquée. Les entrées précédentes décrivent l’historique, pas les fonctions actuelles. Réglages et mises à jour inchangés, en attendant les autres demandes d’interface. Compilation et validation Android à effectuer avant livraison d’une nouvelle APK.


## 0.2.8 — paramètres et aperçus

Les mises à jour de l’application et du moteur sont regroupées dans Paramètres. Vérifications automatiques conservées, mise à jour moteur sans réinstallation, confirmation Android toujours obligatoire pour l’APK. Le panneau s’ouvre si une version APK est disponible ou déjà téléchargée. Retrait de l’interrupteur d’animation : pluie de code active, suspendue en arrière-plan et respectant les animations système. Retrait complet du navigateur local inclus.

Aperçus : transferts HTTPS bornés à 8 Mio, redirections contrôlées sur CDN autorisés, Referer de plateforme sans cookie, réduction avant décodage et repli sur l’image originale pour une photo uniquement. État de chargement, absence d’aperçu et bouton Réessayer visibles. Anciennes requêtes invalidées après nouvelle analyse, effacement ou fermeture. Les erreurs d’aperçu ne bloquent pas la sélection. Ces défauts du code ne prouvent pas la cause des captures manquantes signalées. Trois tests de politique ajoutés : CDN/HTTPS, repli photo sans substitution vidéo, dimensions et en-têtes. Compilation, tests Android/lint et essai appareil à consigner ; aucun nouveau succès d’extraction Instagram revendiqué.


Validation 0.2.8 : exécution 40297008560 réussie sur 4ac55f63dfff8915ce6e0359bf75ea6049535b00, testDebugUnitTest, assembleRelease, lintRelease et vérification apksigner, puis publication. Manifeste latest 15/0.2.8 vérifié ; APK publique téléchargée (65090384 octets), SHA-256 2a74413d99f240b42301f0e0cea6720bfc5979d75536be4d1df37564ca96fa4c conforme. Archive ZIP intacte et asset navigateur absent. Aucun émulateur/appareil Android disponible dans l’environnement : affichage des paramètres, reprise d’aperçu et installation restent à vérifier sur téléphone. Aucun nouveau succès Instagram revendiqué.
