# Home-DL

Prototype Android personnel pour analyser un lien et télécharger les vidéos accessibles, sans publicité ni compte Home-DL.

## État

Premier prototype compilé et ouvert sur téléphone ; analyse Instagram en échec, cause à préciser. Version 0.1.2 avec diagnostic et mise à jour moteur préparée, à compiler et tester. Ce prototype ne doit pas être présenté comme compatible avec toutes les plateformes. Photos et carrousels photo non implémentés ; une miniature n’est pas une photo téléchargeable.

## Prototype 0.1

- Android 10 minimum, processeur ARM64.
- Saisie HTTPS ou partage d’un lien vers Home-DL.
- Analyse vidéo avec yt-dlp embarqué ; aperçus et sélection.
- Meilleure qualité vidéo/audio accessible avec FFmpeg.
- Téléchargement temporaire privé puis publication dans Téléchargements/Home-DL.
- Annulation et délais bornés ; garder l’application au premier plan pour ce prototype.
- Aucun serveur Home-DL, cookie, suivi publicitaire ou historique de liens ajouté.
- Moteur stable vérifié automatiquement à l’ouverture, au plus une fois par 24 h après succès ; désactivable.
- Mise à jour APK intégrée non activée : signature et distribution à définir.

## Construire l’APK

GitHub → Actions → APK de test → Run workflow. Déclenchement manuel uniquement pour maîtriser les consommations. Attendre la réussite de compilation et du lint, puis récupérer l’artefact Home-DL-prototype-arm64. L’archive contient app-debug.apk.

La signature debug du runner est temporaire : deux compilations peuvent nécessiter une désinstallation avant réinstallation. Définir une signature durable hors du dépôt avant les livraisons régulières.

En local : JDK 17, SDK Android 35 et Gradle 8.9, puis `gradle :app:assembleDebug :app:lintDebug`. Aucun Gradle Wrapper n’est inclus à ce stade.

## Confidentialité

Aucun nom personnel, courriel, clé, cookie, média ou lien privé ne doit être committé. Les fichiers de signature et configurations locales sont ignorés. Aucun détail brut d’erreur d’extraction n’est affiché ou enregistré par le code Home-DL.

Les noms des projets tiers sont conservés pour leurs crédits. GitHub affiche séparément le compte propriétaire et l’auteur des commits ; cela ne fait pas partie des fichiers de l’application.

Voir ROADMAP.md, docs/TESTS.md et THIRD_PARTY.md.

Voir docs/UPDATES.md pour le fonctionnement et les limites des mises à jour.
