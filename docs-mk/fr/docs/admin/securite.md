# Sécurité

## Authentification

Tock supporte plusieurs systèmes d'authentification pour l'interface d'administration. 
Il utilise les librairies [vert.x](https://vertx.io/docs/vertx-auth-common/java/) correspondantes. 

Voici les systèmes disponibles par défaut :  

- Un modèle par "propriétés", utilisé par défaut.
Le code est disponible dans la classe [`PropertyBasedAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/PropertyBasedAuthProvider.kt#L61)

- Un modèle [_OAuth2_](https://oauth.net/2/) dont un exemple est donné par [`GithubOAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/GithubOAuthProvider.kt)

- Un modèle basé sur des jetons [_JWT_](https://jwt.io/), dont une implémentation pour AWS est disponible dans [`AWSJWTAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/AWSJWTAuthProvider.kt)    

Si ces modèles ne correspondent pas à votre besoin, il est relativement simple d'en développer d'autres
en se basant sur les exemples ci-dessus. N'hésitez pas à contribuer au projet et à nous contacter pour toute question!

## Chiffrage et anonymisation

### Chiffrage

Il est recommandé de déployer vos bases de données MongoDB en [mode _chiffré_](https://docs.mongodb.com/manual/tutorial/configure-encryption/).

Vous pouvez cependant ajouter un chiffrage applicatif (facultatif) de certains champs en base de données.

C'est le rôle de la propriété `tock_encrypt_pass` qui permet d'indiquer un mot de passe
pour chiffrer et déchiffrer ces champs. Par défaut, Tock chiffre toutes les données utilisateurs
jugées sensibles à condition que ce mot de passe soit spécifié. 
Pour plus de détails, vous pouvez vous réferrer au [code source](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/Encryptors.kt).

### Anonymisation

Il est souvent souhaitable que certaines phrases soient anonymisées que ce soit dans les _logs_ (journalisation)
 ou dans l'interface (_Tock Studio_). Par exemple, des coordonnées, numéros de cartes de fidélité, etc.
   ne devraient être lus ni par les utilisateurs de _Tock Studio_ ni par les administrateurs de la plateforme.
  
Pour anonymiser ces données, Tock met à disposition dans son framework une solution basée sur des 
_expressions régulières (RegExp)_ dont l'interface de base est [`StringObfuscator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/StringObfuscator.kt).