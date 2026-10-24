# Validation du prototype

## Contrôles effectués

- API source du moteur Android consultée : initialisation, exécution, options et annulation.
- Revue statique : pas de compte utilisateur, cookies, clés ou données personnelles dans les fichiers ajoutés.
- Réseau, extraction, téléchargement et aperçus exécutés hors du thread d’interface.
- Aperçus limités à 2 Mio et réduits avant affichage.
- Fichiers partiels nettoyés ; publication MediaStore après copie complète uniquement.

## Contrôles restant à exécuter

Compilation et lint Android : non exécutés dans l’environnement de préparation (SDK et Gradle absents).
Installation sur appareil : non testée.
Extraction et téléchargement réels : non testés.

| Cas | État |
| --- | --- |
| Instagram vidéo publique | Non testé |
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

Application au premier plan uniquement ; pas de service de téléchargement persistant. Pas d’extraction photo. Pas de connexion aux plateformes. Pas de mise à jour automatique du moteur. Signature debug éphémère. L’identification précise de chaque erreur plateforme reste à améliorer.
