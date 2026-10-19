---
title: Sécurité
description: "Sécuriser une plateforme Tock : utilisateurs et rôles de Tock Studio, chiffrement, anonymisation, rétention et suppression des données."
---

# Sécurité

## Utilisateurs *Tock Studio*

### Authentification

_Tock Studio_ prend en charge plusieurs systèmes d'authentification : utilisateurs et rôles définis par propriétés (par défaut),
OAuth2 (générique, Keycloak, GitHub) ou CAS (SSO). Ils sont décrits dans la page [Authentification _Tock Studio_](authentication.md).

### Rôles

Tock permet d'affecter plusieurs _rôles_ ou niveaux d'habilitations aux utilisateurs dans les interfaces _Tock Studio_.
En fonction du système d'authentification utilisé (par propriétés, _OAuth_, etc.) chaque utilisateur se voit assigné
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
autrement dit l'implémentation de `TockAuthProvider` utilisée (voir [Authentification _Tock Studio_](authentication.md)).

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

### Supprimer et migrer les données d'un utilisateur

Tock peut supprimer toutes les données liées à un utilisateur, par exemple pour répondre à une demande au titre du RGPD :
quand un utilisateur envoie le mot-clé de suppression (`_delete_user_` par défaut, configurable avec la propriété
`tock_bot_delete_keyword`), ses données sont supprimées.

La suppression (et la migration des données d'un identifiant utilisateur vers un autre) passe par le service
`UserDataRedactor`, qui appelle chaque `UserDataRedactionProvider` disponible :

| Fournisseur         | Données |
|---------------------|---------|
| `user_timeline`     | Timeline de l'utilisateur (profil, préférences) et dialogues |
| `web_sse_messages`  | Messages en file du [connecteur Web](../channels/web.md) (SSE) |
| `bot_orchestration` | Données d'orchestration entre bots |

Si votre bot stocke d'autres données personnelles, implémentez `ai.tock.shared.service.UserDataRedactionProvider` et
déclarez-le dans un fichier `META-INF/services/ai.tock.shared.service.UserDataRedactionProvider`, pour que ces données
soient aussi supprimées.

## Secrets

Les clés d'API et les mots de passe (fournisseurs Gen AI, bases vectorielles, MongoDB, iAdvize...) peuvent être lus
depuis [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/) ou [GCP Secret Manager](https://cloud.google.com/secret-manager)
plutôt que d'être stockés dans la base Tock ou dans des variables d'environnement :

| Variable d'environnement | Description |
|--------------------------|-------------|
| `tock_gen_ai_secret_manager_provider` | Gestionnaire de secrets pour les clés d'API Gen AI : `AWS_SECRETS_MANAGER` ou `GCP_SECRET_MANAGER` (voir [fournisseurs Gen AI](../gen-ai/providers/llm-embedding.md#cles-dapi-et-gestionnaires-de-secrets)) |
| `tock_gen_ai_secret_prefix` | Préfixe des noms de secrets Gen AI (par défaut : `LOCAL/TOCK`) |
| `tock_database_mongodb_secret_manager_provider` | Gestionnaire de secrets pour les identifiants MongoDB |
| `tock_database_mongodb_credentials_secret_name` | Nom du secret contenant les identifiants MongoDB |
