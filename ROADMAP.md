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
- [ ] APK compilée et testée sur téléphone.

Moteur vidéo référencé dans le prototype Kotlin. Compilation, téléchargements réels et installation sur appareil non validés. Aucune APK produite à ce stade. Voir docs/TESTS.md.

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
