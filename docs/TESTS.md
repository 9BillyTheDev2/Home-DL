# Validation du prototype

## Contrôles effectués

- API source du moteur Android consultée : initialisation, exécution, options et annulation.
- Revue statique : pas de compte utilisateur, cookies, clés ou données personnelles dans les fichiers ajoutés.
- Réseau, extraction, téléchargement et aperçus exécutés hors du thread d’interface.
- Aperçus limités à 2 Mio et réduits avant affichage.
- Fichiers partiels nettoyés ; publication MediaStore après copie complète uniquement.

## Contrôles restant à exécuter

Prototype 0.1 : workflow nº 2 réussi ; compilation et lint validés par le workflow. Installation et ouverture confirmées par un essai utilisateur. Une publication Instagram a échoué à l’analyse ; la cause reste inconnue, l’ancien message regroupant toutes les erreurs. Aucun lien ni média de cet essai conservé dans le dépôt.
Version 0.1.1 : compilation et essai du diagnostic à effectuer. Aucun téléchargement réel validé.

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

Application au premier plan uniquement ; pas de service de téléchargement persistant. Pas d’extraction photo. Pas de connexion aux plateformes. Mise à jour du moteur à la demande uniquement. Signature debug éphémère. Le diagnostic classe les messages du moteur sans prouver leur cause ; aucun texte brut n’est exposé.

## Vérification 0.1.1 sur téléphone

1. Vérifier que le titre et les boutons restent visibles avec les barres système.
2. Appuyer sur « Mettre le moteur à jour » et attendre sa confirmation ; aucun lien n’est transmis dans cette opération.
3. Relancer l’analyse. En cas d’échec, relever l’étape et le code affichés (INIT, AUTH, HTTP_403, EXTRACTION, etc.), sans communiquer de cookie ou identifiant.
4. Une réussite de mise à jour ne valide pas la compatibilité de la publication.
