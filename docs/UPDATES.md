# Mises à jour Home-DL

## Moteur yt-dlp — implémenté, à tester

La version 0.1.2 vérifie la version stable à l’ouverture de l’application :
- au maximum une vérification réussie par période de 24 heures ;
- après échec, attendre au moins 6 heures avant un nouvel essai automatique ;
- un interrupteur permet de désactiver cette vérification ;
- le bouton manuel permet de vérifier sans attendre.

La vérification et le téléchargement passent par les serveurs officiels GitHub du projet yt-dlp. Aucun lien de publication, cookie ou identifiant utilisateur n’est transmis lors de cette opération. Les serveurs contactés voient nécessairement l’adresse IP de connexion. Aucun compte GitHub n’est demandé.

Taille bornée, HTTPS, hôtes autorisés, délais de connexion/lecture et somme SHA-256 publiée dans les métadonnées GitHub vérifiés avant installation. La somme sert à vérifier l’intégrité du téléchargement ; elle n’est pas une signature indépendante de GitHub. Le remplacement utilise AtomicFile pour conserver une sauvegarde pendant l’écriture. En cas d’erreur de téléchargement ou de vérification, le moteur précédent reste disponible. Une incompatibilité fonctionnelle de la nouvelle version nécessite encore une intervention : pas de restauration automatique après échec d’extraction.

Seul le programme Python yt-dlp est actualisé. L’interface, Python, FFmpeg et les autres bibliothèques natives nécessitent une nouvelle APK. Une mise à jour moteur ne garantit pas la compatibilité d’un site.

L’opération se déroule au premier plan et partage la file d’exécution avec les téléchargements ; aucune extraction ne se déroule pendant un remplacement. Un premier téléchargement moteur peut retarder l’accès aux boutons. Les lectures réseau ont des délais de 10 secondes et l’opération vérifie une limite globale de 90 secondes entre ses étapes ; une lecture déjà bloquée peut prendre quelques secondes supplémentaires. Une annulation empêche le remplacement tant que celui-ci n’est pas finalisé.

## Application APK — non activé

Le dépôt source est privé. Sans compte utilisateur ou jeton dans l’application, une APK publiée uniquement dans ce dépôt privé n’est pas accessible à un système de mise à jour intégré.

Solution proposée à valider : dépôt public séparé de distribution, contenant seulement les APK signées et les informations de version. Le code peut rester privé, sous réserve des obligations des composants en cas de distribution. Ne pas y placer de données personnelles. Ne pas rendre le dépôt actuel public sans instruction explicite.

Avant activation :
1. Définir la destination de distribution et obtenir l’accord du propriétaire si elle est publique.
2. Créer une clé de signature permanente et la sauvegarder hors du dépôt.
3. Configurer les secrets de compilation ; ne jamais mettre la clé ou son mot de passe dans les sources.
4. Signer toutes les versions avec cette clé ; incrémenter versionCode.
5. Implémenter la détection de version et l’installeur Android avec confirmation système.
6. Vérifier l’identité du package, la version et le certificat de signature avant de proposer l’installation.
7. Tester une mise à jour qui conserve les données et le rejet d’une APK signée avec une autre clé.

Aucune clé générée, aucun dépôt public créé et aucun mécanisme APK activé à cette étape. Les APK de test gardent leur signature temporaire et peuvent nécessiter une désinstallation. Une notification de mise à jour APK ne doit pas être annoncée comme opérationnelle.

## Compilation signée préparée

Le workflow propose `debug` (par défaut) et `signed-release`. La seconde option échoue explicitement si la signature permanente n’est pas configurée ; elle ne publie rien.

Secrets attendus : `HOME_DL_KEYSTORE_BASE64`, `HOME_DL_STORE_PASSWORD`, `HOME_DL_KEY_ALIAS`, `HOME_DL_KEY_PASSWORD`. La clé doit être créée et sauvegardée avant de renseigner ces secrets. Ne jamais transmettre les mots de passe dans la conversation ou dans un fichier versionné. Le fichier temporaire de signature est supprimé à la fin de l’étape. Seules les APK, et non la clé, entrent dans l’artefact.

Ce branchement est préparé mais non testé, aucun secret n’étant configuré. Le choix `debug` reste utilisable pour les essais actuels.
