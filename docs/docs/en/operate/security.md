---
title: Security
description: "Secure a Tock platform: Tock Studio users and roles, data encryption, anonymization, retention and deletion of user data."
---
# Security

## Users *Tock Studio*

### Authentication

_Tock Studio_ supports several authentication systems: users and roles defined by properties (the default),
OAuth2 (generic, Keycloak, GitHub) or CAS (SSO). They are described on the [_Tock Studio_ authentication](authentication.md) page.

### Roles

Tock allows you to assign several _roles_ or authorization levels to users in the _Tock Studio_ interfaces.

Depending on the authentication system used (by properties, _OAuth_, etc.) each user is assigned
one or more of these roles, giving them different accesses in the application.

The available roles are defined in the `TockUserRole` enum:

| Role | Description |
|-----------------|------------------------------------------------------------------------------------------------------------------------------|
| `nlpUser` | NLP platform user, allowed to qualify and search sentences. |
| ~~`faqNlpUser`~~ | ~~FAQ NLP platform user, allowed to qualify and search sentences.~~<br/> (Deprecated: Use the 'nlpUser' role instead) |
| ~~`faqBotUser`~~ | ~~A faq bot user is allowed to manage the FAQ content, and train the FAQ~~<br/> (Deprecated: Use the 'botUser' role instead) |
| `botUser` | Bot platform user, allowed to create and modify stories, rules and answers. |
| `admin` | Allowed to update applications and configurations/connectors, import/export intents, sentences, stories, etc. |
| `technicalAdmin` | Allowed to access encrypted data, import/export application dumps, etc. |


How to configure which _Tock Studio_ user has which role depends on the authentication mode,
in other words the implementation of `TockAuthProvider` used (see [_Tock Studio_ authentication](authentication.md)).

## Data

Since users can transmit personal data to bots through their conversations, it is important
to think about the nature of the data handled in _Tock Studio_ or stored by Tock, and
to implement appropriate protection mechanisms (anonymization, encryption,
retention period, role-based access restrictions, etc.).

> See in particular the [GDPR](https://en.wikipedia.org/wiki/General_Data_Protection_Regulation) regulation.

### Data encryption

#### Database encryption

It is recommended to deploy your MongoDB databases in [encrypted_ mode](https://docs.mongodb.com/manual/tutorial/configure-encryption/).

#### Application encryption

Tock can perform an application encryption (optional) of some fields in the database, independently of the
encryption of the database itself.

This is the role of the environment variable `tock_encrypt_pass`, which allows to indicate a password
to encrypt and decrypt these fields. By default in the `prod` environment, Tock encrypts all user data
deemed sensitive provided that `tock_encrypt_pass` is defined.

> For more details, you can refer to the [source code](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/Encryptors.kt).


### Anonymization

It is often desirable that certain sentences be anonymized whether in the _logs_ (logging)
or in the interface (_Tock Studio_). For example, contact details, loyalty card numbers, etc.
should not be read by _Tock Studio_ users or by platform administrators.

#### By the framework

To anonymize this data, Tock provides in its framework a solution based on
_regular expressions (RegExp)_ whose basic interface is [`StringObfuscator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/StringObfuscator.kt).

#### By the NLP model

Tock also allows to anonymize in _Tock Studio_ (_Inbox_ view in particular.) the values of the entities
recognized by the NLP model.

This anonymization by entity type is configured in the _Language Understanding > Entities_ view. Only
users with an `admin` or `technicalAdmin` role in _Tock Studio_ can enable/disable this feature.

> For more information, see [_Roles_](security.md#roles).

In views where sentences are displayed anonymized (_Inbox_, _Search_ for example), an `admin` or
`technicalAdmin` can decide to still display (for themselves only) a non-anonymized sentence using the
_Reveal the sentence_ (eye open) action.

> Note: Setting `tock_encrypt_pass` is required to use NLP entity anonymization functions in
> _Tock Studio_ interfaces.

### Storage & Retention

Tock automatically stores different types of data, ranging from non-sensitive information (Stories
configuration and bot responses, intent structure, browsing statistics for all users, etc.) to more personal data (conversation details, user preferences, etc.).

Depending on their nature and use in Tock (NLP, monitoring, debugging, etc.),
these data have specific, configurable retention periods. **Each Tock user decides and configures
how long the stored data is retained, based on their needs.**

The [_Installation > Data Retention_](installation.md#data-retention) section describes the different
types of data retained and how to modify their retention period.

### Deleting and migrating user data

Tock can delete all the data related to a user, for instance to meet a request under the GDPR:
when a user sends the delete keyword (`_delete_user_` by default, configurable with the `tock_bot_delete_keyword` property),
their data is deleted.

Deletion (and migration of the data of a user ID to another ID) goes through the `UserDataRedactor` service, which
calls every `UserDataRedactionProvider` available:

| Provider            | Data |
|---------------------|------|
| `user_timeline`     | User timeline (profile, preferences) and dialogs |
| `web_sse_messages`  | Messages queued by the [Web connector](../channels/web.md) (SSE) |
| `bot_orchestration` | Bot orchestration data |

If your bot stores other personal data, implement `ai.tock.shared.service.UserDataRedactionProvider` and register it
in a `META-INF/services/ai.tock.shared.service.UserDataRedactionProvider` file, so that this data is also deleted.

## Secrets

API keys and passwords (Gen AI providers, vector stores, MongoDB, iAdvize...) can be read from
[AWS Secrets Manager](https://aws.amazon.com/secrets-manager/) or [GCP Secret Manager](https://cloud.google.com/secret-manager)
instead of being stored in the Tock database or in environment variables:

| Environment variable | Description |
|----------------------|-------------|
| `tock_gen_ai_secret_manager_provider` | Secret manager for the Gen AI API keys: `AWS_SECRETS_MANAGER` or `GCP_SECRET_MANAGER` (see [Gen AI providers](../gen-ai/providers/llm-embedding.md#api-keys-and-secret-managers)) |
| `tock_gen_ai_secret_prefix` | Prefix of the Gen AI secret names (default: `LOCAL/TOCK`) |
| `tock_database_mongodb_secret_manager_provider` | Secret manager for the MongoDB credentials |
| `tock_database_mongodb_credentials_secret_name` | Name of the secret holding the MongoDB credentials |
