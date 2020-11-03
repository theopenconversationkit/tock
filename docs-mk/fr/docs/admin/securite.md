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

## Données

Les utilisateurs pouvant transmettre aux bots des données personnelles à travers leurs conversations, il est important 
de réfléchir à la nature des données manipulées dans _Tock Studio_ ou stockées par Tock, et 
de mettre en oeuvre des mécanismes de protection appropriés (anonymisation, chiffrement, 
durée de rétention, restrictions d'accès basées sur des rôles, etc.).
 
> Voir en particulier la réglementation [RGPD](https://en.wikipedia.org/wiki/General_Data_Protection_Regulation).

### Chiffrement des données

#### Chiffrement de la base

Il est recommandé de déployer vos bases de données MongoDB en [mode _chiffré_](https://docs.mongodb.com/manual/tutorial/configure-encryption/).

#### Chiffrement applicatif

Tock peut réaliser un chiffrement applicatif (facultatif) de certains champs en base de données, indépendamment du 
chiffrement de la base elle-même.

C'est le rôle de la variable d'environnement `tock_encrypt_pass`, qui permet d'indiquer un mot de passe
pour chiffrer et déchiffrer ces champs. Par défaut en environnement `prod`, Tock chiffre toutes les données utilisateurs
jugées sensibles à condition que `tock_encrypt_pass` soit défini.

> Pour plus de détails, vous pouvez vous réferrer au [code source](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/Encryptors.kt).

> Remarque : définir `tock_encrypt_pass` est requis pour utiliser les fonctions d'anonymisation d'entités NLP dans 
> les interfaces _Tock Studio_.

### Anonymisation

Il est souvent souhaitable que certaines phrases soient anonymisées que ce soit dans les _logs_ (journalisation)
 ou dans l'interface (_Tock Studio_). Par exemple, des coordonnées, numéros de cartes de fidélité, etc.
   ne devraient être lus ni par les utilisateurs de _Tock Studio_ ni par les administrateurs de la plateforme.

#### Par le framework
  
Pour anonymiser ces données, Tock met à disposition dans son framework une solution basée sur des 
_expressions régulières (RegExp)_ dont l'interface de base est [`StringObfuscator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/StringObfuscator.kt).

#### Par le modèle NLP

Tock permet également d'anonymiser dans _Tock Studio_ (vue _Inbox_ notamment.) les valeurs des entités 
reconnues par le modèle NLP.

Cette anonymisation par types d'entités se configure dans la vue _Language Understanding > Entities_. Seuls les 
utilisateurs ayant un rôle `admin` ou `techAdmin` dans _Tock Studio_ peuvent activer/désactiver cette fonctionnalité.

Dans les vues où les phrases sont affichées anonymisées (_Inbox_, _Search_ par exemple), un `admin` ou 
`techAdmin` peut décider d'afficher quand même (pour lui-même uniquement) une phrase non anonymisée grâce à l'action 
_Reveal the sentence_ (oeil ouvert).
  
> Remarque : définir `tock_encrypt_pass` est requis pour utiliser les fonctions d'anonymisation d'entités NLP dans 
> les interfaces _Tock Studio_.

### Stockage & conservation

Tock stocke automatiquement différents types de données, allant d'informations peu sensibles (configuration de Stories 
et réponses du bot, structure des intentions, statistiques de navigation tous utilisateurs confondus, etc.) à des données 
plus personnelles (détails des conversations, préférences utilisateurs, etc.).

En fonction de leur nature et leur utilisation dans le fonctionnement de Tock (modèle NLP, supervision, debug...), 
ces données ont des durées de rétention spécifiques, et configurables. **Chaque utilisateur de Tock décide 
et configure combien de temps les données stockées sont conservées, en fonction de ses besoins.**

La section [_Installation > Conservation des données_](installation.md#conservation-des-donnees) décrit les différents 
types de données conservées et comment modifier leur durée de rétention.
