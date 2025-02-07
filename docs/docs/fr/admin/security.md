---
title: Sécurité
---

# Sécurite

## Utilisateurs *Tock Studio*

### Authentification

Tock supporte plusieurs systèmes d'authentification pour l'interface d'administration.
Il utilise les librairies [vert.x](https://vertx.io/docs/vertx-auth-common/java/) correspondantes.

Voici les systèmes disponibles par défaut (tous implémentations de `TockAuthProvider`) :

- Un modèle par "propriétés", utilisé par défaut.
  Le code est disponible dans la classe [`PropertyBasedAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/PropertyBasedAuthProvider.kt)

- Un modèle [_OAuth2_](https://oauth.net/2/) générique.

- Un modèle [_OAuth2_](https://oauth.net/2/) adapté à [`Keycloak`](https://www.keycloak.org/).

- Un modèle [_OAuth2_](https://oauth.net/2/) spécifique pour Github dont un exemple est donné par [`GithubOAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/GithubOAuthProvider.kt)

Il est également possible d'intégrer une authentification CAS (SSO), dans le cas d'une installation de type entreprise.
Ce modèle nécessite d'hériter d'un modèle de base, mais permet de faire correspondre un profil utilisateur selon vos
propres contraintes et spécificités.

Des détails et exemples de configuration sont donnés plus bas dans cette page.

Si ces modèles ne correspondent pas à votre besoin, il est relativement simple d'en développer d'autres
en se basant sur les exemples ci-dessus. N'hésitez pas à contribuer au projet et à nous contacter pour toute question!

### Roles

Tock permet d'affecter plusieurs _rôles_ ou niveaux d'habilitations aux utilisateurs dans les interfaces _Tock Studio_.
En fonction du système d'authentification utilisé (par propriétés, _0Auth_, etc.) chaque utilisateur se voit assigné
un ou plusieurs de ces rôles, lui donnant différents accès dans l'application.

Les rôles disponibles sont définis dans l'enum `TockUserRole`:

| Rôle            | Description                                                                                                                  |
|-----------------|------------------------------------------------------------------------------------------------------------------------------|
| `nlpUser`       | NLP platform user, allowed to qualify and search sentences.                                                                  |
| ~~`faqNlpUser`~~ | ~~FAQ NLP platform user, allowed to qualify and search sentences.~~<br/> (Deprecated: Use the 'nlpUser' role instead)        |
| ~~`faqBotUser`~~ | ~~A faq bot user is allowed to manage the FAQ content, and train the FAQ~~<br/> (Deprecated: Use the 'botUser' role instead) |
| `botUser`       | Bot platform user, allowed to create and modify stories, rules and answers.                                                  |
| `admin`         | Allowed to update applications and configurations/connectors, import/export intents, sentences, stories, etc..               |
| `technicalAdmin` | Allowed to access encrypted data, import/export application dumps, etc.                                                      |


La manière de configurer quel utilisateur _Tock Studio_ a quel rôle dépend du mode d'authentification,
autrement dit l'implémentation de `TockAuthProvider` utilisée.

### Implementation par proprietes

La configuration par "propriétés" est utilisée par défaut. Elle ne dépend d'aucun système tiers
pour fonctionner.

Ce mode consiste a configurer utilisateurs et rôles par des propriétés ou variables d'environnement.
Selon le mode de déploiement utilisé, ces variables peuvent être définies soit directement en ligne de commande,
soit dans un descripteur type `docker-compose.yml`, `dockerrun.aws.json` ou autre.

> Si aucune variable n'est définie (par exemple dans les descripteurs fournis dans le dépôt
> [`tock-docker`](https://github.com/theopenconversationkit/tock-docker)), des valeurs par défaut sont utilisées.

Voici les propriétés et leurs valeurs par défaut :

| Variable d'environnement | Valeur par défaut         | Description                                    |
|--------------------------|---------------------------|------------------------------------------------|
| `tock_users`             | `admin@app.com`           | Identifiants (séparés par des virgules).       |
| `tock_passwords`         | `password`                | Mots de passe (séparés par des virgules).      |
| `tock_organizations`     | `app`                     | Organisations (séparées par des virgules).     |
| `tock_roles`             | Vide (i.e., tous les rôles)| Rôles séparés par des `|` (puis par des virgules). |


Pour définir l'identité et les rôles de plusieurs utilisateurs, on sépare les valeurs par des virgules.

> **Attention :** chacune de ces propriétés doit posséder le même nombre de valeurs (et dans le même ordre) pour
> permettre de corréler ces valeurs (index par index, pour chaque utilisateur).

Ci-dessous un exemple au format Docker-Compose :

```yaml
{ "name" : "tock_users", "value" : "alice@tock.ai,bob@tock.ai" },
{ "name" : "tock_passwords", "value" : "secret1,secret2" },
{ "name" : "tock_organizations", "value" : "tock,tock" },
{ "name" : "tock_roles", "value" : "botUser,nlpUser|botUser|admin|technicalAdmin" },
```

Dans cet exemple, Alice a le rôle `botUser`, alors que Bob a tous les rôles.

> Pour en savoir plus sur le fonctionnement précis de cette implémentation, voir la classe
> [`PropertyBasedAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/PropertyBasedAuthProvider.kt).

### Implementation 0Auth2 generique

Cette implémentation générique est à utiliser dès que vous souhaitez paramétrer une configuration OAuth2.

Voici les propriétés et leurs valeurs par défaut :

| Variable d'environnement               | Exemple de valeur       | Description                                        | Exemple                     |
|----------------------------------------|-------------------------|----------------------------------------------------|-----------------------------|
| `tock_oauth2_enabled`                  | `true`                  | Activation de l'authentification OAuth2            |                             |
| `tock_oauth2_client_id`                | `CLIENT_ID`             | Identifiant pour interroger l'API GitHub           |                             |
| `tock_oauth2_secret_key`               | `SECRET_KEY`            | Mot de passe pour interroger l'API GitHub          |                             |
| `tock_oauth2_site_url`                 | `https://provider`      | URL du provider OAuth2                             |                             |
| `tock_oauth2_access_token_path`        | `/oauth2/token`         | Chemin relatif pour récupérer l'access token       |                             |
| `tock_oauth2_authorize_path`           | `/oauth2/authorize`     | Chemin relatif pour récupérer l'authorize          |                             |
| `tock_oauth2_userinfo_path`            | `/oauth2/userInfo`      | Chemin relatif pour récupérer les userinfo         |                             |
| `tock_oauth2_proxy_host`               |                         | Hôte du proxy (ne pas indiquer si pas de proxy)    |                             |
| `tock_oauth2_proxy_port`               |                         | Port optionnel du proxy                            |                             |
| `tock_oauth2_user_role_attribute`      | `custom:roles`          | Attribut lu dans le token pour le mapping du rôle  |                             |
| `tock_custom_roles_mapping`            |                         | Lien entre les profils OAuth et les rôles Tock     |                             |
| `tock_custom_namespace_mapping`        |                         | Lien entre les profils OAuth et les namespaces Tock| `id1=sncf|id2=sncf`         |
| `tock_bot_admin_rest_default_base_url` | `http://localhost:8080` | Redirection vers l'URL de Tock Studio si besoin    |                             |



Il est nécessaire d'indiquer en callback url `https://[host admin]/rest/callback`.

### Implementation 0Auth2 pour Keycloak

Cette implémentation OAuth2 Keycloak est à utiliser dès que vous souhaitez paramétrer une configuration OAuth2 avec Keycloak.

Voici les propriétés et leurs valeurs proposées :

> Attention, par défaut, aucun adapteur n'étant activé, il faut impérativement mettre la clé tock_keycloak_enabled correspondant à notre adaptateur à true.

| Variable d'environnement            | Exemple de valeur                   | Description                                     |
| ----------------------------------- | ----------------------------------- | ----------------------------------------------- |
| `tock_keycloak_enabled`             | `true`                              | Activation de l'authentification 0Auth2         |
| `tock_keycloak_client_id`           | `CLIENT_ID`                         | Client id crée sur Keycloak                     |
| `tock_keycloak_secret_key`          | `SECRET_KEY`                        | Secret key générée par Keycloak                 |
| `tock_keycloak_site_url`            | `https://keycloak/realms/myrealm`   | Url du Realm Keycloak                           |
| `tock_keycloak_access_token_path`   | `/protocol/openid-connect/token`    | Chemin relatif pour récupérer l'access token    |
| `tock_keycloak_authorize_path`      | `/protocol/openid-connect/auth`     | Chemin relatif pour l'authorize                 |
| `tock_keycloak_userinfo_path`       | `/protocol/openid-connect/userinfo` | Chemin relatif pour les userinfo                |
| `tock_keycloak_proxy_host`          |                                     | host du proxy (ne pas indiquer si pas de proxy) |
| `tock_keycloak_proxy_port`          |                                     | port optionnel du proxy                         |
| `tock_custom_namespace_mapping`     | `tock_namespace`                    | Attribut lu dans le token pour le namespace     |
| `tock_keycloak_user_role_attribute` | `tock_roles`                        | Attribut lu dans le token pour les roles        |

Il est nécessaire d'indiquer en callback url `https://[host admin]/rest/callback`.

Une configuration basique nécessite de définir tock_keycloak_client_id et tock_keycloak_secret_key avec les valeurs correspondantes sur Keycloak.

Il est également nécessaire de passer l'adresse du Realm keycloak via tock_keycloak_site_url.

Il n'est en revanche alors pas nécessaire de définir tock_keycloak_access_token_path, tock_keycloak_authorize_path et tock_keycloak_userinfo_path puisque les valeurs par défaut conviennent pour Keycloak si l'on spécifie bien l'adresse du realm.

Si tock_custom_namespace_mapping n'est pas défini ou si l'attribut n'est pas trouvé, le namespace "app" par défaut est utilisé.

### Implementation 0Auth/GitHub

Cette implémentation assez simpliste est utilisée à titre d'exemple, ainsi que pour la plateforme publique de démo
[https://demo.tock.ai](https://demo.tock.ai).

Elle consiste à interroger l'API GitHub pour vérifier l'identité d'un utilisateur à partir de son jeton (`access_token`).

> Remarque : aucune autre donnée du profil GitHub n'est accédée par Tock, à part l'identifiant.

Dans ce mode, activé par la propriété `tock_github_oauth_enabled`, chaque utilisateur reçoit automatiquement tous
les rôles _Tock Studio_ et une organisation (ie. namespace) du même nom que son identifiant.

Voici les propriétés et leurs valeurs par défaut :

| Variable d'environnement             | Valeur par défaut | Description                                      |
| ------------------------------------ | ----------------- | ------------------------------------------------ |
| `tock_github_oauth_enabled`          | `false`           | Activation de l'authentification 0Auth/GitHub.   |
| `tock_github_oauth_client_id`        | `CLIENT_ID`       | Identifiant pour interroger l'API GitHub.        |
| `tock_github_oauth_secret_key`       | `SECRET_KEY`      | Mot de passe pour interroger l'API GitHub.       |
| `tock_github_api_request_timeout_ms` | `5000`            | Timeout vérification de l'identité (API GitHub). |

> Pour en savoir plus sur le fonctionnement précis de cette implémentation, voir la classe
> [`GithubOAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/GithubOAuthProvider.kt).

### Implementation SSO/CAS

Cette implémentation a pour vocation de servir de pont entre un environnement entreprise et Tock.
Elle est donc en partie spécifique à chaque entreprise, dans la mesure ou il est nécessaire de faire correspondre
un profil utilisateur vers des groupes et rôles Tock.

Elle est composée de :

- Une implémentation du mécanisme d'authentification CAS intégrée à Tock \( basée sur ['PAC4J'](https://www.pac4j.org/) \)
- Votre module externalisé qui va hériter de cette implémentation, avec une (re)définition des rôles/groupes selon le
  profil utilisateur

> L'authentification CAS est spécifique à l'entreprise, et nécessite un module dédié externe à développer pour Tock
>
> Example de module CAS: ['samples/tock-sample-cas-auth-provider'](https://github.com/theopenconversationkit/tock/blob/master/samples/tock-sample-cas-auth-provider/)

Voici les propriétés et leurs valeurs par défaut :

| Variable d'environnement                | Valeur par défaut | Description                                                                                                                                                                |
| --------------------------------------- | ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `tock_cas_auth_enabled`                 | `false`           | Activation de l'authentification PAC4J/CAS.                                                                                                                                |
| `tock_cas_auth_proxy_host`              | `127.0.0.1`       | Host du proxy (ne pas indiquer si pas de proxy)                                                                                                                            |
| `tock_cas_auth_proxy_port`              | `3128`            | Port optionnel du proxy                                                                                                                                                    |
| `tock_cas_join_same_namespace_per_user` | `true`            | Lors de la création de l'utilisateur, si le namespace existe déjà et que d'autres utilisateurs sont déjà présent, le nouvel utilisateur rejoint le même namespace existant |

> Pour en savoir plus sur le fonctionnement précis de cette implémentation, voir la classe
> [`CASAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/CASAuthProvider.kt).

Note complémentaire:

> Lorsque l'authentification est de type SSO le bouton de Logout n'est pas disponible

## Donnees

Les utilisateurs pouvant transmettre aux bots des données personnelles à travers leurs conversations, il est important
de réfléchir à la nature des données manipulées dans _Tock Studio_ ou stockées par Tock, et
de mettre en oeuvre des mécanismes de protection appropriés (anonymisation, chiffrement,
durée de rétention, restrictions d'accès basées sur des rôles, etc.).

> Voir en particulier la réglementation [RGPD](https://en.wikipedia.org/wiki/General_Data_Protection_Regulation).

### Chiffrement des donnees

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
utilisateurs ayant un rôle `admin` ou `technicalAdmin` dans _Tock Studio_ peuvent activer/désactiver cette fonctionnalité.

> Pour en savoir plus, voir [_Rôles_](#roles).

Dans les vues où les phrases sont affichées anonymisées (_Inbox_, _Search_ par exemple), un `admin` ou
`technicalAdmin` peut décider d'afficher quand même (pour lui-même uniquement) une phrase non anonymisée grâce à l'action
_Reveal the sentence_ (oeil ouvert).

> Remarque : définir `tock_encrypt_pass` est requis pour utiliser les fonctions d'anonymisation d'entités NLP dans
> les interfaces _Tock Studio_.

### Stockage & conservation

Tock stocke automatiquement différents types de données, allant d'informations peu sensibles (configuration de Stories
et réponses du bot, structure des intentions, statistiques de navigation tous utilisateurs confondus, etc.) à des données
plus personnelles (détails des conversations, préférences utilisateurs, etc.).

En fonction de leur nature et leur utilisation dans le fonctionnement de Tock (NLP, supervision, debug...),
ces données ont des durées de rétention spécifiques, et configurables. **Chaque utilisateur de Tock décide et configure
combien de temps les données stockées sont conservées, en fonction de ses besoins.**

La section [_Installation > Conservation des données_](installation.md#conservation-des-donnees) décrit les différents
types de données conservées et comment modifier leur durée de rétention.
