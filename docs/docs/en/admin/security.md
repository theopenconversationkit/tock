---
title: Security
---
[//]: # (Traduit avec Google Translate et Reverso)

# Security

## Users *Tock Studio*

### Authentication

Tock supports several authentication systems for the administration interface.
It uses the corresponding [vert.x](https://vertx.io/docs/vertx-auth-common/java/) libraries.

Here are the systems available by default (all implementations of `TockAuthProvider`):

- One model per "properties", used by default.
The code is available in the [`PropertyBasedAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/PropertyBasedAuthProvider.kt) class

- A generic [_OAuth2_](https://oauth.net/2/) model.

- A [_OAuth2_](https://oauth.net/2/) model adapted to [`Keycloak`](https://www.keycloak.org/).

- A [_OAuth2_](https://oauth.net/2/) model specific to Github, an example of which is given by [`GithubOAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/GithubOAuthProvider.kt)

It is also possible to integrate CAS authentication (SSO), in the case of an enterprise-type installation.
This model requires inheriting from a base model, but allows you to match a user profile according to your
own constraints and specificities.

Details and configuration examples are given further down this page.

If these models do not fit your needs, it is relatively easy to develop others
based on the examples above. Feel free to contribute to the project and contact us for any questions!

### Roles

Tock allows you to assign several _roles_ or authorization levels to users in the _Tock Studio_ interfaces.

Depending on the authentication system used (by properties, _0Auth_, etc.) each user is assigned
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
in other words the implementation of `TockAuthProvider` used.

### Implementation by properties

The configuration by "properties" is used by default. It does not depend on any third-party system
to work.

This mode consists of configuring users and roles by properties or environment variables.

Depending on the deployment mode used, these variables can be defined either directly on the command line,
or in a descriptor such as `docker-compose.yml`, `dockerrun.aws.json` or other.

> If no variable is defined (for example in the descriptors provided in the repository
> [`tock-docker`](https://github.com/theopenconversationkit/tock-docker)), default values ​​are used.

Here are the properties and their default values:

| Environment Variable     | Default Value          | Description                                      |
|--------------------------|------------------------|--------------------------------------------------|
| `tock_users`             | `admin@app.com`       | Identifiers (comma-separated).                  |
| `tock_passwords`         | `password`            | Passwords (comma-separated).                    |
| `tock_organizations`     | `app`                | Organizations (comma-separated).                |
| `tock_roles`             | Empty (i.e., all roles)| Roles separated by `|` (then by commas).         |


To define the identity and roles of several users, separate the values ​​by commas.

> **Warning:** each of these properties must have the same number of values ​​(and in the same order) to
> allow correlation of these values ​​(index by index, for each user)

Below is an example in Docker-Compose format:

```yaml
{ "name" : "tock_users", "value" : "alice@tock.ai,bob@tock.ai" },
{ "name" : "tock_passwords", "value" : "secret1,secret2" },
{ "name" : "tock_organizations", "value" : "tock,tock" },
{ "name" : "tock_roles", "value" : "botUser,nlpUser|botUser|admin|technicalAdmin" },
```

In this example, Alice has the `botUser` role, while Bob has all the roles.

> For more information on how this implementation works, see the class
> [`PropertyBasedAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/PropertyBasedAuthProvider.kt).

### Generic 0Auth2 Implementation

This generic implementation is to be used whenever you want to set up an OAuth2 configuration.

Here are the properties and their default values:

| Environment Variable                  | Example Value           | Description                                         | Example                            |
|----------------------------------------|-------------------------|-----------------------------------------------------|------------------------------------|
| `tock_oauth2_enabled`                  | `true`                  | Enable OAuth2 authentication                        |                                    |
| `tock_oauth2_client_id`                | `CLIENT_ID`             | Client ID to query the GitHub API                   |                                    |
| `tock_oauth2_secret_key`               | `SECRET_KEY`            | Password to query the GitHub API                    |                                    |
| `tock_oauth2_site_url`                 | `https://provider`      | OAuth2 provider URL                                 |                                    |
| `tock_oauth2_access_token_path`        | `/oauth2/token`         | Relative path to retrieve the access token          |                                    |
| `tock_oauth2_authorize_path`           | `/oauth2/authorize`     | Relative path to retrieve the authorization         |                                    |
| `tock_oauth2_userinfo_path`            | `/oauth2/userInfo`      | Relative path to retrieve user information          |                                    |
| `tock_oauth2_proxy_host`               |                         | Proxy host (leave blank if no proxy is used)        |                                    |
| `tock_oauth2_proxy_port`               |                         | Optional proxy port                                 |                                    |
| `tock_oauth2_user_role_attribute`      | `custom:roles`          | Attribute read from the token for role mapping      |                                    |
| `tock_custom_roles_mapping`            |                         | Link between OAuth profiles and Tock roles          |                                    |
| `tock_custom_namespace_mapping`        |                         | Link between OAuth profiles and Tock namespaces     | `id1=sncf|id2=sncf`                |
| `tock_bot_admin_rest_default_base_url` | `http://localhost:8080` | Redirect to Tock Studio URL if necessary            |                                    |

It is necessary to indicate in callback url `https://[host admin]/rest/callback`.

### 0Auth2 implementation for Keycloak

This OAuth2 Keycloak implementation is to be used as soon as you want to configure an OAuth2 configuration with Keycloak.

Here are the properties and their proposed values:

> Warning, by default, no adapter is activated, it is imperative to set the tock_keycloak_enabled key corresponding to our adapter to true.

| Environment variable | Example value | Description |
| ----------------------------------- | ----------------------------------- | ----------------------------------------------- |
| `tock_keycloak_enabled` | `true` | Enabling 0Auth2 authentication |
| `tock_keycloak_client_id` | `CLIENT_ID` | Client id created on Keycloak |
| `tock_keycloak_secret_key` | `SECRET_KEY` | Secret key generated by Keycloak |
| `tock_keycloak_site_url` | `https://keycloak/realms/myrealm` | Keycloak Realm Url |
| `tock_keycloak_access_token_path` | `/protocol/openid-connect/token` | Relative path to retrieve the access token |
| `tock_keycloak_authorize_path` | `/protocol/openid-connect/auth` | Relative path for the authorize |
| `tock_keycloak_userinfo_path` | `/protocol/openid-connect/userinfo` | Relative path for the userinfo |
| `tock_keycloak_proxy_host` | | proxy host (do not specify if no proxy) |
| `tock_keycloak_proxy_port` | | optional proxy port |
| `tock_custom_namespace_mapping` | `tock_namespace` | Attribute read in the token for the namespace |
| `tock_keycloak_user_role_attribute` | `tock_roles` | Attribute read in the token for the roles |

It is necessary to indicate in callback url `https://[host admin]/rest/callback`.

A basic configuration requires to define tock_keycloak_client_id and tock_keycloak_secret_key with the corresponding values ​​on Keycloak.

It is also necessary to pass the address of the keycloak Realm via tock_keycloak_site_url.

It is however not necessary to define tock_keycloak_access_token_path, tock_keycloak_authorize_path and tock_keycloak_userinfo_path since the default values ​​are suitable for Keycloak if the realm address is specified correctly.

If tock_custom_namespace_mapping is not set or the attribute is not found, the default "app" namespace is used.

### 0Auth/GitHub Implementation

This rather simplistic implementation is used as an example, as well as for the public demo platform
[https://demo.tock.ai](https://demo.tock.ai).

It consists of querying the GitHub API to verify the identity of a user from their token (`access_token`).

> Note: no other data in the GitHub profile is accessed by Tock, apart from the identifier.

In this mode, enabled by the `tock_github_oauth_enabled` property, each user automatically receives all
_Tock Studio_ roles and an organization (ie. namespace) with the same name as their identifier.

Here are the properties and their default values:

| Environment Variable                | Default Value  | Description                                       |
|-------------------------------------|----------------|---------------------------------------------------|
| `tock_github_oauth_enabled`         | `false`        | Enable OAuth/GitHub authentication.              |
| `tock_github_oauth_client_id`       | `CLIENT_ID`    | Identifier to query the GitHub API.              |
| `tock_github_oauth_secret_key`      | `SECRET_KEY`   | Password to query the GitHub API.                |
| `tock_github_api_request_timeout_ms`| `5000`         | Identity verification timeout (GitHub API).      |

> For more information on how this implementation works, see the class
> [`GithubOAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/GithubOAuthProvider.kt).

### SSO/CAS Implementation

This implementation is intended to serve as a bridge between an enterprise environment and Tock.
It is therefore partly specific to each enterprise, insofar as it is necessary to map
a user profile to Tock groups and roles.

It is composed of:

- An implementation of the CAS authentication mechanism integrated into Tock \( based on ['PAC4J'](https://www.pac4j.org/) \)
- Your externalized module which will inherit this implementation, with a (re)definition of roles/groups according to the
user profile

> CAS authentication is enterprise specific and requires the development of a dedicated external module for tock
>
> CAS module example: ['samples/tock-sample-cas-auth-provider'](https://github.com/theopenconversationkit/tock/blob/master/samples/tock-sample-cas-auth-provider/)

Here are the properties and their default values:

| Environment variable | Default value | Description |
| --------------------------------------- | ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `tock_cas_auth_enabled` | `false` | Enable PAC4J/CAS authentication. |
| `tock_cas_auth_proxy_host` | `127.0.0.1` | Proxy host (do not specify if no proxy) |
| `tock_cas_auth_proxy_port` | `3128` | Optional proxy port |
| `tock_cas_join_same_namespace_per_user` | `true` | When creating the user, if the namespace already exists and other users are already present, the new user joins the same existing namespace |

> For more information on how this implementation works, see the class
> [`CASAuthProvider`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/auth/CASAuthProvider.kt).

Additional note:

> When authentication is of type SSO the Logout button is not available

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

> Note: defining `tock_encrypt_pass` is required to use the NLP entity anonymization functions in
> the _Tock Studio_ interfaces.

### Anonymization

It is often desirable that certain sentences be anonymized whether in the _logs_ (logging)
or in the interface (_Tock Studio_). For example, contact details, loyalty card numbers, etc.
should not be read by _Tock Studio_ users or by platform administrators.

#### By the framework

To anonymize this data, Tock provides in its framework a solution based on
_regular expressions (RegExp)_ whose basic interface is [`StringObfuscator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/StringObfuscator.kt).

#### By the NLP model

Tock also allows to anonymize in _Tock Studio_ (_Inbox_ view in particular.) the values ​​of the entities
recognized by the NLP model.

This anonymization by entity type is configured in the _Language Understanding > Entities_ view. Only
users with an `admin` or `technicalAdmin` role in _Tock Studio_ can enable/disable this feature.

> For more information, see [_Roles_](../../admin/security.md#roles).

In views where sentences are displayed anonymized (_Inbox_, _Search_ for example), an `admin` or
`technicalAdmin` can decide to still display (for himself only) a non-anonymized sentence using the
_Reveal the sentence_ (eye open) action.
> Note: Setting `tock_encrypt_pass` is required to use NLP entity anonymization functions in
> _Tock Studio_ interfaces.

### Anonymization

It is often desirable that certain sentences be anonymized whether in the _logs_ (logging)
or in the interface (_Tock Studio_). For example, contact details, loyalty card numbers, etc.
should not be read by either _Tock Studio_ users or platform administrators.

#### By the framework

To anonymize this data, Tock provides in its framework a solution based on
_regular expressions (RegExp)_ whose basic interface is [`StringObfuscator`](https://github.com/theopenconversationkit/tock/blob/master/shared/src/main/kotlin/security/StringObfuscator.kt).

#### By the NLP model

Tock also allows to anonymize in _Tock Studio_ (_Inbox_ view in particular.) the values ​​of the entities
recognized by the NLP model.

This anonymization by entity types is configured in the _Language Understanding > Entities_ view. Only
users with an `admin` or `technicalAdmin` role in _Tock Studio_ can enable/disable this feature.

> For more information, see [_Roles_](../../admin/security.md#roles).

In the views where the sentences are displayed anonymized (_Inbox_, _Search_ for example), an `admin` or
`technicalAdmin` can decide to still display (for himself only) a non-anonymized sentence thanks to the
_Reveal the sentence_ action (open eye).

> Note: Setting `tock_encrypt_pass` is required to use NLP entity anonymization features in
> _Tock Studio_ interfaces.

### Storage & Retention

Tock automatically stores different types of data, ranging from non-sensitive information (Stories
configuration and bot responses, intent structure, browsing statistics for all users, etc.) to more personal data (conversation details, user preferences, etc.).

Depending on their nature and use in Tock (NLP, monitoring, debugging, etc.),

these data have specific, configurable retention periods. **Each Tock user decides and configures
how long the stored data is retained, based on their needs.**

The [_Installation > Data Retention_](../../admin/installation.md#data-retention) section describes the different
types of data retained and how to modify their retention period.