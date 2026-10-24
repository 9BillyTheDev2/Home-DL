# Composants et références

Home-DL : prototype original, 24 octobre 2026, GPL-3.0-only (voir LICENSE).

## Dépendances intégrées

- youtubedl-android library et ffmpeg 0.18.1 : https://github.com/yausername/youtubedl-android ; GPLv3. Exécution de yt-dlp et FFmpeg sur Android. API consultée : YoutubeDLRequest, YoutubeDL.execute, destroyProcessById et FFmpeg.init.
- Les composants embarqués (notamment Python, yt-dlp, FFmpeg et leurs bibliothèques) conservent leurs licences propres. Avant diffusion au-delà du test personnel : compléter l’inventaire des binaires de la version Maven retenue et joindre leurs notices/sources correspondantes.

## Références étudiées

- Seal : https://github.com/JunkFood02/Seal ; référence de parcours Android. Aucun code d’interface, nom ou visuel repris.
- YTDLnis : https://github.com/deniscerri/ytdlnis ; référence fonctionnelle. Aucun code repris.
- gallery-dl : https://github.com/mikf/gallery-dl ; candidat pour les images. Non intégré ; compatibilité Android non validée.

La documentation et l’API de youtubedl-android ont été consultées. Ce relevé ne constitue pas un audit complet de toutes les dépendances transitives. La compilation et les tests sur appareil restent nécessaires.
