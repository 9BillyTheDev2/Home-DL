# Validation du prototype

## Contrôles effectués

- API source du moteur Android consultée : initialisation, exécution, options et annulation.
- Revue statique : pas de compte utilisateur, cookies, clés ou données personnelles dans les fichiers ajoutés.
- Réseau, extraction, téléchargement et aperçus exécutés hors du thread d’interface.
- Aperçus limités à 2 Mio et réduits avant affichage.
- Fichiers partiels nettoyés ; publication MediaStore après copie complète uniquement.

## Contrôles restant à exécuter

Prototype 0.1 : workflow nº 2 réussi ; compilation et lint validés par le workflow. Installation et ouverture confirmées par un essai utilisateur. Une publication Instagram a échoué à l’analyse ; la cause reste inconnue, l’ancien message regroupant toutes les erreurs. Aucun lien ni média de cet essai conservé dans le dépôt.
Versions 0.1.1/0.1.2 : compilation et essai du diagnostic/mises à jour à effectuer. Aucun téléchargement réel validé.

| Cas | État |
| --- | --- |
| Instagram publication (type exact non établi) | Échec d’analyse sur 0.1 ; diagnostic à préciser |
| TikTok vidéo publique | Non testé |
| X/Twitter vidéo publique | Non testé |
| Photo seule / carrousel photo | Non implémenté |
| Publication vidéo multiple | Non testé |
| Lien partagé depuis une autre application | Non testé |
| Audio et meilleure qualité | Non testé |
| Annulation pendant analyse / transfert / copie | Non testé |
| Stockage plein, réseau coupé, refus plateforme | Non testé |
| Rotation ou fermeture de l’application | L’opération est annulée ; à vérifier |

## Procédure sur téléphone

1. Installer l’APK après réussite du workflow.
2. Analyser une publication vidéo publique et vérifier l’aperçu.
3. Télécharger, ouvrir le fichier dans Téléchargements/Home-DL et vérifier image, son et résolution.
4. Vérifier un partage Android puis une publication multiple.
5. Tester annulation et coupure réseau. Aucun fichier partiel ne doit apparaître comme réussi.
6. Consigner seulement la plateforme, le type de média, le résultat et la version, sans conserver de lien personnel dans le dépôt.

## Limites connues

Application au premier plan uniquement ; pas de service de téléchargement persistant. Pas d’extraction photo. Pas de connexion aux plateformes. Vérification moteur automatique à l’ouverture (désactivable), ainsi qu’à la demande. Signature debug éphémère. Le diagnostic classe les messages du moteur sans prouver leur cause ; aucun texte brut n’est exposé.

## Vérification 0.1.1 sur téléphone

1. Vérifier que le titre et les boutons restent visibles avec les barres système.
2. Appuyer sur « Mettre le moteur à jour » et attendre sa confirmation ; aucun lien n’est transmis dans cette opération.
3. Relancer l’analyse. En cas d’échec, relever l’étape et le code affichés (INIT, AUTH, HTTP_403, EXTRACTION, etc.), sans communiquer de cookie ou identifiant.
4. Une réussite de mise à jour ne valide pas la compatibilité de la publication.

## Vérifications 0.1.2

- Première ouverture en ligne : vérification et mise à jour moteur, puis analyse disponible.
- Réouverture avant 24 h après succès : pas de nouvelle vérification automatique.
- Échec réseau : boutons réactivés, moteur précédent conservé, pas de tentative automatique avant 6 h.
- Interrupteur désactivé : aucune vérification automatique à la prochaine ouverture ; contrôle manuel possible.
- Annulation pendant le téléchargement : aucun remplacement incomplet.
- Réponse trop grande ou somme SHA-256 absente/incorrecte : remplacement refusé.
- Fermeture pendant l’écriture : vérifier la récupération AtomicFile au prochain démarrage.
- Aucune mise à jour APK ne doit être annoncée comme disponible par cette version.

Tous ces cas restent à tester sur appareil ; la lecture du code ne vaut pas validation fonctionnelle.

## Vérifications visuelles 0.1.3

Compilation assembleDebug et lintDebug réussis sur GitHub Actions pour le thème rétro-futuriste. Artefact APK ARM64 produit. Contrastes calculés : texte principal 16,4:1 ; texte secondaire 8,7:1 ; bouton vert 15,6:1 ; bouton cyan 13,5:1 ; orange sur panneau 10,1:1. XML du thème et manifeste parsés sans erreur. Aucune nouvelle dépendance graphique ou police distante.

À vérifier sur téléphone : marges système, clavier et défilement sur petit écran ; grandes polices ; ouverture des réglages ; activation/désactivation de la pluie de code ; arrêt de l’animation en arrière-plan ; états des boutons pendant analyse/annulation ; lisibilité des cartes médias réelles. Aucun rendu Android sur appareil ou émulateur n’a été observé pour cette version. Le thème n’atteste pas la réussite de l’extraction Instagram ni de la signature permanente.

## Vérifications 0.1.4

Tests automatisés ajoutés : aide cookies sans faux AUTH ; priorité HTTP 403 ; accès ambigu ; distinction AUTH/429 ; erreur de dépendance moteur ; masquage de secrets/adresses/liens/chemins ; réponse JSON sans métadonnées ; longueur bornée. Workflow : testDebugUnitTest, assembleDebug et lintDebug.

À vérifier sur téléphone : croix rouge efface le lien, les résultats et le diagnostic ; croix désactivée pendant une opération ; lien sur plusieurs lignes ; affichage des détails après échec ; copie volontaire du diagnostic ; nouvelle analyse efface le diagnostic précédent. Le masquage automatique ne garantit pas l’absence de tout renseignement dans une erreur inconnue : le texte est présenté pour relecture avant copie. Aucun journal n’est transmis automatiquement.

Résultat de validation 0.1.4 : testDebugUnitTest, assembleDebug et lintDebug réussis ; APK ARM64 produite. Les essais UI sur le téléphone et l’analyse du prochain diagnostic utilisateur restent à effectuer. Aucun succès de téléchargement sur plateforme n’est déduit de ces tests.


## Vérifications 0.1.5 — moteur

- Actif officiel yt-dlp 2026.10.21 récupéré et SHA-256 vérifié ; lancement --version confirmé sur Python local.
- Redirection officielle github.com/releases/latest et somme SHA2-256SUMS accessibles dans l’environnement de développement. Le chemin de mise à jour téléphone ne dépend plus de api.github.com.
- Téléchargement complet du trailer public W3C https://media.w3.org/2010/05/sintel/trailer.mp4 : 4 372 373 octets ; ffprobe confirme vidéo + audio, durée 52,208 s. Essai du moteur sur ordinateur, sans validation Android/MediaStore. Le test local utilise les autorités système pour le proxy de cet environnement ; aucun contrôle TLS désactivé, aucun changement TLS ajouté dans l’APK.
- Analyse du lien Instagram fourni effectuée hors téléphone avec le moteur récent : réponse de restriction de public. Ce résultat n’établit ni la cause du refus sur le téléphone ni qu’une connexion résoudra le problème. Le lien n’est pas conservé dans le dépôt.
- Tests ajoutés : lecture exacte des sommes (exclusion .exe/.tar.gz, absence/doublon refusés), distinction DNS/restriction de public.

À vérifier sur téléphone : diagnostic moteur 2026.10.21 même si la vérification réseau échoue ; contrôle manuel sans api.github.com ; vidéo publique directe avec aperçu, sélection et copie dans Téléchargements/Home-DL ; vidéo publique de chaque plateforme séparément. Photos/carrousels et mises à jour APK non implémentés.

Résultat GitHub Actions : exécution 40113703258 réussie sur le commit 207ef46410d3c452c3be3621d99737e949aebc6e ; vérification de l’actif, douze tests unitaires, assembleDebug et lintDebug terminés sans échec. Artefact APK ARM64 produit. Aucun succès Instagram/TikTok/X ou Android/MediaStore n’en est déduit.


## Vérifications 0.2.0

Exécution 40126888285 réussie sur aa5a45f2a82fa6f993c2f56071f1fef5f5d1c8ca : testDebugUnitTest, assembleDebug, lintDebug et probePublicInstagram. 29 tests JVM au total dans les sources : diagnostics, sommes, liens Instagram/hostnames, sélection de variantes et ordre du carrousel, refus d’une couverture vidéo seule, formats réels, manifeste d’APK et identité de signature/version/package. La sélection photo/carrousel utilise des réponses synthétiques, pas une validation réseau de ces formats.

Essai réseau du code Kotlin de production : session anonyme, analyse d’une vidéo publique issue des fixtures yt-dlp, puis téléchargement CDN d’un MP4 de 918 120 octets et validation du format. Aucun compte ni service de téléchargement tiers ; aucun lien fourni par l’utilisateur dans le test public. Ce test est distinct du téléchargement W3C réalisé précédemment avec yt-dlp.

La commande probePublicInstagram signale explicitement PUBLIC_INSTAGRAM_PASS ou PUBLIC_INSTAGRAM_UNCONFIRMED ; son état Gradle ne suffit pas à conclure, car l’échec d’un service externe ne doit pas rendre une compilation non déterministe. Les journaux de cette exécution contiennent PUBLIC_INSTAGRAM_PASS.

Restent à vérifier sur Android : session anonyme et réseau du téléphone, vrais photos/carrousels/publications mixtes, aperçu et sélection, enregistrement MediaStore, audio/qualité, annulation. Pour l’APK : installer une première version signée durablement, publier une version supérieure de même certificat, vérifier détection/téléchargement/permission d’installation/confirmation/conservation des réglages, puis refus d’un autre certificat et d’une version ancienne. Les tests JVM vérifient la politique de confiance ; ils ne remplacent pas ce parcours système. Aucun secret de signature configuré, aucune release publiée pendant cette étape.

## Première compilation permanente — 0.2.0

Exécution 40127772969 réussie : testDebugUnitTest, assembleRelease et lintRelease, préparation du manifeste et vérification apksigner, publication de la release. Après publication : route de vérification utilisée par l’app accessible, version 7/0.2.0, APK réellement téléchargée et taille exacte 65 039 980 octets, SHA-256 conforme au manifeste et aux métadonnées GitHub. Vérification indépendante avec apksig officiel 8.7.3 : signature v2 valide et certificat conforme à l’empreinte permanente. Inspection ZIP : absence de fichiers de clé privée et de mot de passe de compilation dans les entrées APK inspectées. Aucune installation ni mise à jour Android n’est inférée de ces tests.

## Préparation 0.2.1

Échec utilisateur observé sur 0.2.0 : produit public absent dans la réponse reçue par l’app, sans détail serveur conservé. Ce refus n’a pas été reproduit depuis l’environnement de développement. Test du lien exact avec le code Kotlin de production de la correction : analyse et téléchargement de quatre photos, 1440 × 1920, formats vérifiés ; tailles 403184, 383260, 248864 et 268766 octets. Une tentative Java directe a d’abord échoué à cause de l’absence de prise en compte du proxy standard de l’environnement ; le test a ensuite utilisé ce proxy déjà configuré, sans modifier le code de l’app, désactiver TLS ou ajouter de réseau alternatif. Les images de l’essai ont été supprimées.

Nouveaux tests : enveloppe shortcode et carrousel de quatre photos, enveloppe Polaris, erreurs data:null, résumé sans métadonnées, refus de public ou de limitation sans alternative, erreur de post différent. Fixtures synthétiques ; aucun shortcode, lien ou réponse du propriétaire dans le dépôt ou les journaux Actions. Compilation Android et publication confirmées ci-dessous.

Résultat 0.2.1 : exécution 40136132751 réussie sur df1fa4ab5b632d31fad30c732ac4a1686df33db8, 37 tests JVM dans les sources, assembleRelease et lintRelease réussis. La vidéo publique de régression a été analysée et téléchargée avec le code Kotlin compilé localement (918120 octets). Release v0.2.1 publiée ; manifeste latest version 8/0.2.1 et URL vérifiés, APK réellement téléchargée (65048088 octets), SHA-256 conforme aux métadonnées de release et au manifeste. Signature v2 et empreinte de certificat permanent vérifiées avec apksig 8.7.3. Le parcours Android de mise à jour et l’analyse depuis le téléphone restent à confirmer. Les réponses Instagram temporaires ont été supprimées ; aucun média utilisateur conservé.


## Préparation 0.2.2 — diagnostic des analyses

Cinq tests AnalysisTrace couvrent la phase du 404, la confidentialité (messages d’exception et paramètres de Content-Type exclus), la limite de 64 lignes, le nettoyage, l’isolation entre threads et les analyses réussies/vides. Les tests sont synthétiques, sans lien utilisateur ni requête réseau. 29 tests locaux ciblés réussis après le dernier changement ; compilation Android réussie (exécution 40178715899). Les traces n’ajoutent pas de récupération de photos X/TikTok et ne démontrent pas que les refus Instagram sont corrigés.

Résultat 0.2.2 : build et publication réussis sur a290189f240321f84d0679290ed225c4065b2344, testDebugUnitTest, assembleRelease et lintRelease. La trace réelle JVM du lien en échec indique INSTAGRAM_SESSION OK (HTTP 200) puis INSTAGRAM_ACCESS FAIL HTTP_404 (JSON, 51 octets annoncés). Aucun corps de réponse ni URL recopié. Manifeste latest vérifié 9/0.2.2 ; APK réellement téléchargée (65059300 octets), SHA-256 613541d424b4fd81677cffb578c5b6cd3dd9db219b63c1134130b3de0d895f71 conforme. Signature APK v2 et empreinte permanente vérifiées par apksig. Installation et rapport sur Android restent à tester par le propriétaire.


## Préparation 0.2.3 — affichage des mises à jour

Revue du parcours : confirmation du téléchargement, statut 0 % et barre dans la carte de mise à jour ; progression calculée sur la taille connue du manifeste indépendamment du Content-Length HTTP ; vérification de l’APK annoncée avant le bouton d’installation ; échec/annulation visible dans la carte. Pas de test d’interface Android disponible localement. Compilation Android, suite JVM et lint réussis (exécution 40185675685). La cause réseau ou installation sur le téléphone n’est pas déduite de ce défaut d’affichage.

Résultat 0.2.3 : testDebugUnitTest, assembleRelease, lintRelease et publication réussis sur bd7698a06ac22ab39b14fa88acacd3979ce784d4. Manifeste latest 10/0.2.3 et APK réellement téléchargée : 65061668 octets, SHA-256 e10b5e8a20bd870df3e79db5dee5149513998ea3c1d4036d821b01f139cabf91 conforme. Signature v2 et certificat permanent validés par apksig. Parcours de mise à jour sur appareil encore non confirmé.


## Préparation 0.2.4 — récupération photo

Fixtures synthétiques X/TikTok : liens et normalisation, ordre, qualité annoncée, variante originale X, aperçu, post différent, couverture vidéo refusée, réponse vide/refusée, URL CDN malveillante et JSON sans fuite de données. 41 tests locaux ciblés réussis. Essai réel des adaptateurs Kotlin de production (sans Android) : X 1 JPEG 163004 octets ; TikTok 2 JPEG 502891 et 202450 octets. Transferts faits avec les primitives HTTPS et de validation de format de Home-DL, via le proxy standard déjà configuré dans l’environnement Java de test ; aucun proxy ajouté à l’app. Fichiers supprimés après vérification. Compilation Android, tests JVM et publication réussis (exécution 40192618900) ; parcours Android sur appareil à confirmer. MediaStore, affichage des aperçus sur appareil, collections mixtes et toutes les publications de ces plateformes ne sont pas validés par ces trois fichiers.

Résultat 0.2.4 : testDebugUnitTest (54 méthodes dans les sources), assembleRelease et lintRelease réussis sur 518240df9e24ac18b920b03cbd94221f82d33248. Release publiée. APK réellement téléchargée, 65077036 octets, SHA-256 688241d4324c9c9e209b0fdd1b26342337f3ec1a2b7a1d0cea0fa7a7b48dc43e conforme au manifeste latest 11/0.2.4 et aux métadonnées GitHub ; signature v2 et certificat permanent validés par apksig. Les essais JVM utilisent le code source de production des adaptateurs et des transferts ; ils ne remplacent pas les essais MediaStore et UI sur Android.


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


## Retrait du navigateur local — 26 octobre 2026

Suppression du code WebView et de ses tests/assets dédiés, des points d’entrée MainActivity/RetroScreen, de l’activité du manifeste, de la dépendance WebKit et de l’étape CI JavaScript devenue sans objet. Vérifier l’absence de proposition navigateur après une analyse Instagram en échec ; conserver diagnostic, progression, analyse directe et téléchargement. Compilation Android et tests existants à exécuter ; aucune nouvelle APK publiée à cette étape. Les descriptions navigateur précédentes sont historiques.


## 0.2.8 — paramètres et aperçus

Les mises à jour de l’application et du moteur sont regroupées dans Paramètres. Vérifications automatiques conservées, mise à jour moteur sans réinstallation, confirmation Android toujours obligatoire pour l’APK. Le panneau s’ouvre si une version APK est disponible ou déjà téléchargée. Retrait de l’interrupteur d’animation : pluie de code active, suspendue en arrière-plan et respectant les animations système. Retrait complet du navigateur local inclus.

Aperçus : transferts HTTPS bornés à 8 Mio, redirections contrôlées sur CDN autorisés, Referer de plateforme sans cookie, réduction avant décodage et repli sur l’image originale pour une photo uniquement. État de chargement, absence d’aperçu et bouton Réessayer visibles. Anciennes requêtes invalidées après nouvelle analyse, effacement ou fermeture. Les erreurs d’aperçu ne bloquent pas la sélection. Ces défauts du code ne prouvent pas la cause des captures manquantes signalées. Trois tests de politique ajoutés : CDN/HTTPS, repli photo sans substitution vidéo, dimensions et en-têtes. Compilation, tests Android/lint et essai appareil à consigner ; aucun nouveau succès d’extraction Instagram revendiqué.


Validation 0.2.8 : exécution 40297008560 réussie sur 4ac55f63dfff8915ce6e0359bf75ea6049535b00, testDebugUnitTest, assembleRelease, lintRelease et vérification apksigner, puis publication. Manifeste latest 15/0.2.8 vérifié ; APK publique téléchargée (65090384 octets), SHA-256 2a74413d99f240b42301f0e0cea6720bfc5979d75536be4d1df37564ca96fa4c conforme. Archive ZIP intacte et asset navigateur absent. Aucun émulateur/appareil Android disponible dans l’environnement : affichage des paramètres, reprise d’aperçu et installation restent à vérifier sur téléphone. Aucun nouveau succès Instagram revendiqué.


## 0.2.9 — état explicite des interrupteurs

Les deux options automatiques affichent Activé/Désactivé, synchronisé avec isChecked à l’ouverture et après chaque changement. Couleurs de curseur et de piste différenciées selon l’état (vert/cyan actif, gris inactif), état vocal explicite et indication d’indisponibilité conservée pendant une opération. Aucun changement de préférence ni d’automatisation. Libellé Paramètres corrigé également après ouverture/fermeture. Les captures utilisateur confirmaient des couleurs identiques entre états ; validation visuelle sur appareil restant nécessaire. Pas de test unitaire ajouté pour cette modification d’apparence réversible ; suite existante, compilation et lint à exécuter.


Validation 0.2.9 : exécution 40299833602 sur d32fc56ccc5d6d2361239a83aabeabc1f09812fa, tests JVM, assembleRelease, lintRelease et contrôle de signature réussis ; release publiée. Manifeste latest 16/0.2.9 vérifié ; APK téléchargée, 65091072 octets, SHA-256 45cb1d2fd67529fe2a532503b151f71dac40cfdd4709482d84cce1daa7c86d17 conforme et ZIP intact. Affichage réel Activé/Désactivé et contrastes des interrupteurs à confirmer sur téléphone ; pas de test d’interface Android exécuté dans l’environnement.
