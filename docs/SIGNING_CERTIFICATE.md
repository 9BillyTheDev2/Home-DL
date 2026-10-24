# Signature permanente Home-DL

Créée le 24 octobre 2026 avec l’accord du propriétaire.

- Sujet public : CN=Home-DL.
- Clé RSA 3072 bits, SHA256withRSA, format PKCS12.
- SHA-256 du certificat DER : `33ecd7c3ae950e93ad2ea5484254b9bf6f9ebbddfcdf13ec6b0789c9ab919a5a`.

Cette empreinte est publique et permet de comparer le certificat annoncé par apksigner. Elle ne permet pas de signer une application. Les mises à jour doivent conserver le même certificat, un code de version supérieur et le package app.homedl. Le contrôle Android et la confirmation d’installation restent actifs.

La clé privée et ses mots de passe sont dans les secrets de compilation, avec une sauvegarde privée hors du dépôt. Ils ne doivent jamais être ajoutés aux sources, journaux ou assets de release. Ne pas régénérer une clé pour une mise à jour ordinaire.
