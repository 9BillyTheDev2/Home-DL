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
