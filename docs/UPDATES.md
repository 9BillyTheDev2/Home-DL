# Mises à jour Home-DL

## Application — v0.2.0 signée et publiée

Le parcours intégré recherche la version publiée au lancement (au maximum une tentative par 6 h, désactivable), affiche sa disponibilité et propose de télécharger l’APK. Le bouton Installer ouvre ensuite la confirmation Android ; Home-DL ne dispose pas de privilège permettant une installation silencieuse. La source d’installation Home-DL doit être autorisée dans les réglages Android lors du premier usage. Une recherche et un téléchargement s’effectuent au premier plan.

Source : https://github.com/9BillyTheDev2/Home-DL/releases/latest/download/update.json puis l’APK de la release concernée. Aucun compte GitHub, lien de publication ou cookie Instagram transmis. Ces requêtes exposent l’adresse IP au serveur GitHub, comme tout téléchargement. Aucun secret incorporé à l’app.

Contrôles : HTTPS et hôtes GitHub autorisés à chaque redirection, manifeste 32 Kio maximum, APK 150 Mio maximum et taille exacte, SHA-256, package app.homedl, code de version supérieur correspondant au manifeste, certificat de signature identique à l’app installée. Un APK d’une autre signature est refusé. Installation par FileProvider limité au dossier privé de mise à jour. L’installeur Android conserve son contrôle et sa confirmation.

## Signature et première transition — restant à configurer

Les APK 0.1.x sont des builds debug avec signature temporaire. Le chemin intégré ne peut pas les transformer en une application signée par une autre clé. Une première installation manuelle de la version permanente reste nécessaire ; si Android refuse la signature différente, il faudra désinstaller la version de test, ce qui effacera ses réglages (les médias enregistrés dans Téléchargements restent des fichiers partagés). Ne rien désinstaller tant que la release permanente n’est pas prête.

La clé permanente doit être sauvegardée en privé et enregistrée dans les secrets de compilation. Elle ne doit jamais apparaître dans le code, l’historique Git, un journal, un artefact public ou une conversation. Les quatre secrets attendus restent HOME_DL_KEYSTORE_BASE64, HOME_DL_STORE_PASSWORD, HOME_DL_KEY_ALIAS, HOME_DL_KEY_PASSWORD. Les quatre secrets ont été enregistrés avec l’accord du propriétaire le 24 octobre 2026 ; une sauvegarde privée a été conservée hors des sources. Gradle utilise explicitement le format PKCS12. La première compilation signée et sa publication ont réussi dans l’exécution 40127772969.

## Livraison

Le workflow manuel conserve debug pour les tests. signed-release nécessite les quatre secrets, exécute les tests et lint, signe, contrôle l’APK avec apksigner et prépare Home-DL.apk + update.json. Avec publish activé, un second job publie la release vVERSION avec uniquement les deux fichiers publics. La permission contents:write est limitée à ce job. Sans publish, aucune release n’est publiée. Une version existante n’est pas remplacée silencieusement : utiliser un nouveau numéro. Le keystore temporaire est supprimé en fin de compilation.

Signature configurée et sauvegardée ; première release permanente publiée. Il reste à vérifier sur appareil la conservation des données entre deux versions et le refus d’une signature incorrecte. Le code et une compilation seuls ne valident pas le parcours Android.

## Moteur yt-dlp

Le moteur officiel 2026.10.21 est intégré avec SHA-256 épinglé (scripts/prepare_engine.py avant Gradle). Une version locale plus récente est conservée. Vérification stable officielle github.com, sans dépendance à api.github.com : 24 h entre succès, 6 h après échec ; commande manuelle et interrupteur. Somme SHA2-256SUMS, HTTPS, taille et remplacement atomique contrôlés. Un échec conserve le moteur existant. Pas de restauration automatique après une incompatibilité fonctionnelle. Interface, Python et FFmpeg nécessitent une mise à jour APK. Aucun lien de média envoyé pour vérifier les mises à jour.

## Validation du code

Compilation, lint et tests JVM réussis dans l’exécution 40126888285. Les contrôles de package/version/certificat sont couverts par tests. La vérification Android d’APK et le parcours complet entre deux versions signées restent à essayer après configuration des secrets. État lors de la validation du code : les secrets étaient absents. Ils ont ensuite été configurés après accord explicite ; voir docs/SIGNING_CERTIFICATE.md. La publication et les contrôles de l’APK sont confirmés ci-dessous.

## Livraison vérifiée

https://github.com/9BillyTheDev2/Home-DL/releases/tag/v0.2.0 — Home-DL.apk et update.json. Route utilisée par l’app accessible : https://github.com/9BillyTheDev2/Home-DL/releases/latest/download/update.json. APK 65 039 980 octets ; SHA-256 5a5d234363dea5523bdf7aba59ddc4c5e9aa575d4ca248107c9dcbe321e73c24. Signature v2 vérifiée avec apksig 8.7.3 ; empreinte du certificat identique à docs/SIGNING_CERTIFICATE.md. Aucun fichier de clé privée ni mot de passe de compilation dans l’APK. Le téléchargement et l’identité du binaire sont vérifiés hors téléphone ; installation Android et première mise à jour à essayer sur le téléphone.
