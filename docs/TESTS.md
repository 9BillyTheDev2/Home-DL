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
