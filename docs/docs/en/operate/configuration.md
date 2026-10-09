---
title: Configuration reference
description: "Reference of the configuration properties of the Tock components."
---

# Configuration reference

This page lists the configuration properties of the Tock components.

## Setting a property

Each property is read from a JVM system property or, failing that, from an environment variable of the same name.
With Docker Compose for instance:

```yaml
services:
  admin_web:
    image: tock/bot_admin:$TAG
    environment:
      - tock_env=prod
      - tock_mongo_url=mongodb://mongo:27017/?replicaSet=tock
```

or on the command line: `java -Dtock_env=prod ...`.

The value formats are:

* Durations: the unit is given by the suffix of the name (`_ms`, `_in_s`, `_in_minutes`, `_hours`, `_days`...)
* Lists: values separated by commas, e.g. `tock_users=alice@tock.ai,bob@tock.ai`
* Maps: entries separated by `|`, keys and values separated by `=`, e.g. `id1=namespace1|id2=namespace2`

A property is only read by the components that embed the corresponding module: for instance, the connector
properties are read by the bot (`bot_api` service in _Bot API_ mode, or your bot in _Integrated bot_ mode).

> This list is built from the source code with the `etc/list-doc-properties.py` script, which lists the
> properties read by the Kotlin modules and the ones missing from this page (`--check` option).

## Common properties

These properties are read by all the Tock components (`bot_admin`, `nlp_api`, `build_worker`, `bot_api`,
`kotlin_compiler`, `duckling_bot`) and by the bots developed in _Integrated bot_ mode.

### Environment

| Property | Default | Description |
|----------|---------|-------------|
| `tock_env` | `dev` | Environment. Any value other than `dev` enables the production behaviors (HTTPS cookies, CORS, mandatory encryption passphrase...) |
| `tock_default_locale` | `en` | Default locale of the platform |
| `tock_default_zone` | `UTC` | Default time zone |
| `tock_default_namespace` | `app` | Default namespace |
| `tock_namespace_open_access` | `false` | Gives every user access to all the namespaces. Disabled by default for security reasons |
| `tock_vertx_worker_pool_size` | Vert.x default | Size of the Vert.x worker thread pool |
| `tock_cache_in_memory_maximum_size` | `10000` | Maximum number of entries of the in-memory caches |
| `tock_cache_in_memory_expiration_in_ms` | `3600000` | Expiration of the in-memory cache entries, after the last access |

### MongoDB

See [Installation](installation.md#mongodb-database).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_mongo_url` | `mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=tock&retryWrites=true` | MongoDB connection string. A replica set is required |
| `tock_async_mongo_url` | value of `tock_mongo_url` | Connection string used by the asynchronous driver |
| `tock_bot_mongo_db` | `tock_bot` | Name of the database of the bots (dialogs, users, stories, answers...) |
| `tock_front_mongo_db` | `tock_front` | Name of the database of the NLP applications (intents, entities, sentences) |
| `tock_model_mongo_db` | `tock_model` | Name of the database of the NLP models |
| `tock_cache_mongo_db` | `tock_cache` | Name of the database of the shared cache |
| `tock_document_db_on` | `false` | Enables the compatibility mode with [Amazon DocumentDB](https://aws.amazon.com/documentdb/) |
| `tock_database_mongodb_secret_manager_provider` | | Secret manager holding the MongoDB credentials: `AWS_SECRETS_MANAGER` or `GCP_SECRET_MANAGER` |
| `tock_database_mongodb_credentials_secret_name` | `database_mongodb_credentials` | Name of the secret holding the MongoDB credentials |

### _Tock Studio_ authentication

See [_Tock Studio_ authentication](authentication.md). Without any of the `*_enabled` properties below, the users are
defined by the `tock_users`, `tock_passwords`, `tock_organizations` and `tock_roles` properties.

| Property | Default | Description |
|----------|---------|-------------|
| `tock_users` | value of `tock_user` | Logins of the users |
| `tock_passwords` | value of `tock_password` | Passwords of the users, in the same order |
| `tock_organizations` | value of `tock_default_namespace` | Namespaces of the users, in the same order |
| `tock_roles` | all roles | Roles of the users, in the same order. The roles of a user are separated by `\|` |
| `tock_user` | `admin@app.com` | Login of the default user, when `tock_users` is not set |
| `tock_password` | `password` | Password of the default user, when `tock_passwords` is not set |
| `tock_oauth2_enabled` | `false` | Enables the generic OAuth2 authentication |
| `tock_oauth2_client_id` | | OAuth2 client identifier |
| `tock_oauth2_secret_key` | | OAuth2 client secret |
| `tock_oauth2_site_url` | | URL of the OAuth2 provider |
| `tock_oauth2_access_token_path` | | Relative path of the token endpoint |
| `tock_oauth2_authorize_path` | | Relative path of the authorization endpoint |
| `tock_oauth2_userinfo_path` | | Relative path of the user info endpoint |
| `tock_oauth2_supported_grant_types` | | Supported grant types |
| `tock_oauth2_user_role_attribute` | `custom:roles` | Attribute of the user info holding the roles |
| `tock_oauth2_proxy_host` | | Host of the HTTP proxy used to reach the OAuth2 or GitHub provider |
| `tock_oauth2_proxy_port` | `0` | Port of this proxy |
| `tock_oauth2_proxy_username` | | User of this proxy |
| `tock_oauth2_proxy_password` | | Password of this proxy |
| `tock_custom_roles_mapping` | | Mapping between the roles of the OAuth2 provider and the Tock roles |
| `tock_custom_namespace_mapping` | | OAuth2: mapping between user profiles and Tock namespaces (e.g. `id1=namespace1\|id2=namespace2`). Keycloak: attribute holding the namespace (default `tock_namespace`) |
| `tock_bot_admin_rest_default_base_url` | `http://localhost:8080` | URL of _Tock Studio_, to redirect the user after the OAuth2 or Keycloak authentication |
| `tock_keycloak_enabled` | `false` | Enables the Keycloak authentication |
| `tock_keycloak_client_id` | `tock` | Keycloak client identifier |
| `tock_keycloak_secret_key` | | Keycloak client secret |
| `tock_keycloak_site_url` | `https://keycloak/realms/myrealm` | URL of the Keycloak realm |
| `tock_keycloak_access_token_path` | `/protocol/openid-connect/token` | Relative path of the token endpoint |
| `tock_keycloak_authorize_path` | `/protocol/openid-connect/auth` | Relative path of the authorization endpoint |
| `tock_keycloak_userinfo_path` | `/protocol/openid-connect/userinfo` | Relative path of the user info endpoint |
| `tock_keycloak_grant_types` | | Supported grant types |
| `tock_keycloak_user_role_attribute` | `tock_roles` | Attribute holding the roles |
| `tock_keycloak_proxy_host` | | Host of the HTTP proxy used to reach Keycloak |
| `tock_keycloak_proxy_port` | `0` | Port of this proxy |
| `tock_github_oauth_enabled` | `false` | Enables the GitHub authentication |
| `tock_github_oauth_client_id` | | Client identifier of the GitHub OAuth application |
| `tock_github_oauth_secret_key` | | Client secret of the GitHub OAuth application |
| `tock_github_api_request_timeout_ms` | `5000` | Timeout of the GitHub API calls |
| `tock_cas_auth_enabled` | `false` | Enables the CAS authentication (requires a CAS authentication module) |
| `tock_cas_join_same_namespace_per_user` | `true` | Puts the users of the same organization in the same namespace |
| `tock_cas_auth_proxy_host` | | Host of the HTTP proxy used to reach the CAS server |
| `tock_cas_auth_proxy_port` | | Port of this proxy |
| `aws_region` | `eu-west-1` | AWS region of the Application Load Balancer, for the authentication with its JWT |
| `tock_aws_public_key_request_timeout_ms` | `30000` | Timeout of the calls retrieving the AWS ALB public keys |

### Web server

The properties prefixed by `<verticle>_` apply to one HTTP server (_verticle_) of Tock. The prefix is the name of
the verticle class in lower case: `botadminverticle` (`bot_admin`), `nlpverticle` (`nlp_api`),
`botverticle` (bot and `bot_api`), `kotlincompilerverticle` (`kotlin_compiler`), `ducklingverticle` (`duckling_bot`),
`healthcheckverticle` (`build_worker`). For instance, `botverticle_port` sets the HTTP port of the bot.

| Property | Default | Description |
|----------|---------|-------------|
| `<verticle>_port` | `8080` | HTTP port |
| `<verticle>_body_limit` | `1000000` | Maximum size of a request body, in bytes |
| `<verticle>_tock_vertx_compression_supported` | `true` | Enables HTTP compression |
| `<verticle>_tock_vertx_healthcheck_path` | `<root path>/healthcheck` | Path of the healthcheck (see [Supervision](supervision.md#healthchecks)) |
| `<verticle>_tock_vertx_readinesscheck_path` | `/health/readiness` | Path of the readiness probe |
| `<verticle>_tock_vertx_livenesscheck_path` | `/health/liveness` | Path of the liveness probe |
| `tock_detailed_healthcheck_enabled` | `false` | Returns the status of each dependency in the healthcheck (see [Supervision](supervision.md#detailed-mode)) |
| `tock_https_env` | `true` | Secure (HTTPS only) session cookies. Ignored in the dev environment |
| `tock_vertx_session_expiration_timeout` | `21600000` | Expiration of the _Tock Studio_ sessions, in milliseconds (6 hours) |
| `tock_web_use_default_cors_handler` | `true` in dev, else `false` | Adds a CORS handler to the HTTP servers |
| `tock_web_use_default_cors_handler_url` | `http://localhost:4200` (`*` for the bot) | Origins allowed by this CORS handler, separated by `\|` (`*`: all) |
| `tock_web_use_default_cors_handler_with_credentials` | `true` (`false` for the bot) | Allows the credentials in the CORS requests |
| `tock_web_cookie_auth` | | Default user identification of the web connectors: `encrypted` (encrypted cookie), `basic` or `true` (plain cookie). Otherwise, the identifier sent by the client is used |
| `tock_web_cookie_auth_max_age` | `-1` | Maximum age of the authentication cookie, in seconds (`-1`: session cookie) |
| `tock_web_cookie_auth_path` | | Path of the authentication cookie |
| `tock_web_sse_keepalive_delay` | `10` | Delay between two keep-alive messages of the SSE connections, in seconds |
| `tock_file_upload_directory` | `file-uploads` | Directory of the uploaded files |

### Encryption and secrets

See [Security](security.md#data).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_encrypt_pass` | | Passphrase used to encrypt the sensitive data. Required outside the dev environment as soon as encryption is used |
| `tock_gen_ai_secret_manager_provider` | | Secret manager holding the Gen AI secrets (API keys): `AWS_SECRETS_MANAGER` or `GCP_SECRET_MANAGER` |
| `tock_gen_ai_secret_prefix` | `LOCAL/TOCK` | Prefix of the names of the Gen AI secrets |

### Logs and outgoing HTTP calls

See [Supervision](supervision.md#logging).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_logback_enabled` | `true` | Enables the default Logback configuration of Tock |
| `tock_default_log_level` | `DEBUG` in dev, else `INFO` | Default log level |
| `tock_logback_file_appender` | `false` | Also writes the logs to `log/logFile.log` |
| `tock_retrofit_log_level` | `BODY` in dev, else `NONE` | Log level of the outgoing HTTP calls: `NONE`, `BASIC`, `HEADERS` or `BODY` |
| `tock_proxy_user` | | User of the HTTP proxy used for the outgoing calls |
| `tock_proxy_password` | | Password of this proxy |
| `tock_circuit_breaker` | `false` | Enables a circuit breaker on the outgoing calls that support it (Duckling) |

## Bot

These properties are read by the bots: the `bot_api` service in _Bot API_ mode, or your bot application
in _Integrated bot_ mode.

### Bot engine

| Property | Default | Description |
|----------|---------|-------------|
| `tock_configuration_bot_default_base_url` | `http://<local IP>:<botverticle_port>` | Base URL of the bot, proposed by default in the connector configurations of _Tock Studio_ |
| `tock_bot_protected_paths` | `/admin` | Paths of the bot protected by the authentication |
| `tock_bot_protected_path` | `/admin` | Deprecated: use `tock_bot_protected_paths` |
| `tock_restricted_configuration_id` | | Only loads the given bot configurations (development) |
| `tock_bot_default_breath_ms` | `1000` | Default delay between two messages of the bot |
| `tock_bot_min_breath_ms` | `50` | Minimum delay between two messages, when a message takes longer than the default delay to prepare |
| `tock_technical_error` | `Technical error :( sorry!` | Message sent to the user on technical error |
| `tock_gen_ai_orchestrator_technical_error` | `Technical error :( sorry!` | Message sent to the user when the Gen AI orchestrator fails |
| `tock_ask_again_round` | `1` | Default number of times the bot asks a question again |
| `tock_bot_send_choice_activate` | `true` | A click on a button reactivates a disabled bot |
| `tock_bot_disabled_duration_in_minutes` | `7200` | Duration of the deactivation of the bot for a user (5 days) |
| `tock_bot_refresh_profil_duration_in_minutes` | `7200` | Delay before refreshing the user profile from the channel (5 days) |
| `tock_bot_wait_nlp_availability_in_ms` | `5000` | Time to wait for the NLP service at startup |
| `tock_cleanup_delay_seconds` | `60` | Maximum delay to send the queued messages of a coroutine story |
| `tock_bot_audio_nlp_enabled` | `true` | Sends the audio messages to the NLP (speech to text) |
| `tock_bot_audio_nlp_max_size` | `1048576` | Maximum size of the audio messages sent to the NLP, in bytes |
| `tock_bot_serve_files` | `true` | Serves the files uploaded in _Tock Studio_ (answer images...) |
| `tock_bot_serve_files_path` | `/f/` | Path of these files |
| `tock_nlp_proxy_on_bot` | `false` | Exposes the NLP API through the bot |
| `tock_nlp_proxy_on_bot_path` | `/_proxy_nlp` | Path of this NLP proxy |
| `tock_websocket_enabled` | `false` | Enables the _WebSocket_ endpoint of the _Bot API_ mode |
| `tock_timeline_persistence_synchronous_mode` | `true` | Deprecated: use [coroutine stories](../develop/coroutine-stories.md) |
| `tock_timeline_persistence_asynchronous_mode` | `false` | Deprecated: use [coroutine stories](../develop/coroutine-stories.md) |
| `tock_orchestration` | `false` | Enables the orchestration between bots |
| `tock_orchestration_lock_ttl_in_min` | `60` | Lifetime of the orchestration locks |
| `tock_gen_ai_orchestrator_server_url` | `http://localhost:8000` | URL of the Gen AI orchestrator |
| `tock_gen_ai_orchestrator_client_request_timeout_ms` | `55000` | Timeout of the calls to the Gen AI orchestrator |
| `tock_gen_ai_orchestrator_vector_store_provider` | `PGVector` | Type of the default vector store (see [Vector DB settings](../gen-ai/vector-store.md)) |
| `tock_nlp_client_request_timeout_ms` | `20000` | Timeout of the calls to the NLP API |

### Test keywords and test user

Keywords sent as a user message to act on the conversation, mostly used for the tests.

| Property | Default | Description |
|----------|---------|-------------|
| `tock_bot_delete_keyword` | `_delete_user_` | Deletes the data of the user |
| `tock_bot_enable_keyword` | `_enable_user_` | Enables the bot for the user |
| `tock_bot_disable_keyword` | `_disable_user_` | Disables the bot for the user |
| `tock_bot_test_context_keyword` | `_test_` | Starts a test context |
| `tock_bot_end_test_context_keyword` | `_end_test_` | Ends the test context |
| `tock_bot_test_first_name` | `Joe` | First name of the test user |
| `tock_bot_test_last_name` | `Hisaishi` | Last name of the test user |

### Dialog storage

See [Installation](installation.md#data-retention).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_bot_timeline_index_ttl_days` | `365` | Retention of the user timelines (profiles and preferences) |
| `tock_bot_dialog_index_ttl_days` | `7` | Retention of the dialogs |
| `tock_bot_flow_stats_index_ttl_days` | `365` | Retention of the conversation flow statistics |
| `tock_bot_alternative_index_ttl_hours` | `1` | Retention of the alternatives chosen for the multiple-choice answers |
| `tock_bot_dialog_max_validity_in_seconds` | `86400` | Maximum age of a dialog to be continued. After this delay, a new dialog starts |
| `tock_bot_max_actions_by_dialog` | `1000` | Maximum number of actions kept in a dialog |
| `tock_bot_dialog_flow_stat` | `true` | Records the conversation flow statistics |
| `tock_dialog_flow_crawl_stats` | `true` | Computes the conversation flow statistics periodically |
| `tock_bot_add_namespace_to_timeline_id` | `false` | Prefixes the user identifiers with the namespace, to separate the users of several namespaces |
| `tock_bot_encrypted_flags` | | Dialog flags whose value is encrypted |
| `tock_bot_lock_timeout_in_ms` | `5000` | Lifetime of the lock of a user, while a message is processed |
| `tock_bot_locked_attempts_wait_in_ms` | `500` | Delay between two attempts to acquire the lock |
| `tock_bot_lock_retry_jitter_in_ms` | `20` | Random delay added to this delay |
| `tock_bot_max_locked_attempts` | `10` | Number of attempts before giving up |
| `tock_bot_warn_after_locked_attempts` | `5` | Number of attempts before logging a warning |
| `mongo_user_ttl_hours` | `6` | Retention of the user locks |

### _Bot API_ mode

See [Bot API](../develop/bot-api.md).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_bot_api_timeout_in_ms` | `60000` | Timeout of the calls to the webhook of the bot |
| `tock_bot_api_connection_timeout_in_ms` | `3000` | Connection timeout of these calls |
| `tock_bot_api_webhook_check_reachability` | `true` | Checks that the webhook is reachable before calling it |
| `tock_bot_api_webhook_reachability_in_ms` | `10000` | Timeout of this check |
| `tock_api_timout_in_s` | `10` | Timeout of the answer of a bot connected with _WebSocket_ |
| `tock_api_old_webhook_behaviour` | `false` | Disables the streaming (SSE) of the webhook answers |
| `tock_bot_api_actions_history_to_client_bus` | `false` | Sends the history of the dialog actions to the bot with each request |
| `tock_websocket_host` | `localhost` | Host of the `bot_api` service, for the _WebSocket_ client |
| `tock_websocket_port` | `8080` | Port of the `bot_api` service, for the _WebSocket_ client |
| `tock_websocket_ssl` | `false` | Uses TLS for the _WebSocket_ connection |

## Connectors

### Web

See [Web](../channels/web.md).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_web_sse` | `false` | Enables the SSE (Server-Sent Events) endpoint |
| `tock_web_direct_sse` | `false` | Sends the answers directly on the SSE connection, without storing them |
| `tock_web_cors_pattern` | `.*` | Origins allowed to call the connector |
| `tock_web_enable_markdown` | `false` | Converts the Markdown of the answers to HTML |
| `allow_markdown` | | Former name of `tock_web_enable_markdown` |
| `tock_web_connector_persist_profile` | `false` | Stores the user profile sent by the client |
| `tock_web_connector_extra_headers` | | HTTP headers of the request passed to the bot |
| `tock_web_connector_use_extra_header_as_metadata_request` | `false` | Passes these headers as metadata of the request |
| `tock_web_connector_merge_stream_response` | `true` | Merges the chunks of a streamed answer into one message |
| `tock_web_connector_bridge_enabled` | `false` | Allows a request to target another web connector with its `connectorId` field |
| `tock_web_sse_message_queue_ttl_days` | `-1` | Retention of the SSE messages waiting to be delivered (`-1`: no expiration) |
| `tock_web_sse_message_queue_max_count` | `50000` | Maximum number of SSE messages waiting to be delivered |
| `tock_web_sse_message_queue_max_size_kb` | `100000` | Maximum size of these messages, in kilobytes |

### OpenAI API

See [OpenAI API](../channels/openai.md). This connector also reads `tock_web_sse` and `tock_web_cors_pattern`.

| Property | Default | Description |
|----------|---------|-------------|
| `tock_openai_connector_persist_profile` | `false` | Stores the user profile sent by the client |
| `tock_openai_support_unstreamed` | `false` | Accepts the requests without streaming |

### WhatsApp

See [WhatsApp](../channels/whatsapp.md).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_whatsappcloud_request_timeout_ms` | `30000` | Timeout of the calls to the WhatsApp API |
| `tock_whatsappcloud_request_gzip` | `false` | Compresses the requests |
| `tock_whatsapp_cloud_restricted_phone_numbers` | | Only answers these phone numbers (tests) |
| `tock_whatsapp_cloud_restricted_user_ids` | | Only answers these Business-Scoped User IDs (BSUID), in addition to the phone numbers above (tests) |
| `tock_whatsapp_sync_templates` | `false` | Synchronizes the message templates with WhatsApp |
| `tock_whatsapp_reupload_images` | `true` | Uploads the images to WhatsApp instead of sending their URL |
| `tock_whatsapp_error_on_invalid_messages` | `false` | Fails instead of logging a warning on a message that does not respect the WhatsApp limits |
| `tock_whatsapp_payload` | `whatsapp_payload` | Collection storing the button payloads |
| `tock_whatsapp_payload_ttl_days` | `10` | Retention of these payloads |

### Messenger

See [Messenger](../channels/messenger.md).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_messenger_request_timeout_ms` | `30000` | Timeout of the calls to the Messenger API |
| `tock_messenger_request_gzip` | `true` | Compresses the requests |
| `messenger_retries_on_error_limit` | `1` | Number of retries of a failed call |
| `messenger_retries_on_error_wait_in_ms` | `5000` | Delay between two retries |
| `tock_messenger_extended_profile_fields` | | Additional fields of the user profile to retrieve |
| `tock_bot_messenger_reuse_attachment` | `true` | Reuses the attachments already uploaded to Messenger |
| `tock_messenger_webhook_check_subscription` | `false` | Periodically checks the webhook subscription of the pages |
| `tock_messenger_webhook_check_period` | `600` | Period of this check, in seconds |
| `tock_messenger_webhook_url` | URL of the current subscription | Webhook URL used to renew the subscription |

### Other connectors

| Property | Default | Description |
|----------|---------|-------------|
| `tock_microsoft_request_timeout` | `5000` | Teams: timeout of the calls retrieving the Microsoft keys, in milliseconds |
| `tock_teams_request_timeout_ms` | `30000` | Teams: timeout of the calls to the Bot Framework API (formerly `tock_whatsapp_request_timeout_ms`, still read if the new property is not set) |
| `tock_whatsapp_request_timeout_ms` | `30000` | WhatsApp (deprecated _On-Premise API_ connector): timeout of the calls to the WhatsApp API |
| `tock_mattermost_request_timeout_ms` | `30000` | Mattermost: timeout of the calls to the Mattermost API |
| `tock_slack_old_api_style` | `false` | Slack: uses the former format of the Slack API |
| `tock_api_google_chat_connector_test_send_intro_message` | `false` | Google Chat: sends an introduction message in test mode |
| `tock_rest_connector_disabled` | `false` | Disables the REST connector used by the tests of _Tock Studio_ |
| `tock_rest_connector_check_nlp_stats` | `false` | REST connector: returns the NLP statistics with the answer |
| `tock_bot_rest_client_request_timeout_ms` | `100000` | Timeout of the calls of the REST connector client |
| `tock_iadvize_secret_manager_provider` | `ENV` | iAdvize: secret manager holding the credentials |
| `tock_iadvize_credentials_secret_name` | `iadvize_credentials` | iAdvize: name of the secret holding the credentials |

## _Tock Studio_

These properties are read by the `bot_admin` service.

| Property | Default | Description |
|----------|---------|-------------|
| `botadminverticle_base_href` | `/` | Base path of _Tock Studio_ (see [Installation](installation.md#making-the-administration-interface-available-in-a-subdirectory)) |
| `botadminverticle_content_path` | `/maven/dist` | Directory of the _Tock Studio_ web application |
| `botadminverticle_nlp_external_host` | `localhost:8888` | Host of the NLP API displayed in _Tock Studio_ |
| `tock_bot_api` | `false` | Displays the _Bot API_ features in _Tock Studio_ |
| `tock_bot_global_message` | | Message displayed in a banner of _Tock Studio_ |
| `tock_csv_delimiter` | `;` | Delimiter of the CSV exports |
| `tock_bot_compiler_disabled` | `false` | Disables the script compiler |
| `tock_bot_compiler_service_url` | `http://localhost:8887` | URL of the `kotlin_compiler` service |
| `tock_bot_compiler_timeout_in_ms` | `60000` | Timeout of the calls to this service |
| `tock_kotlin_compiler_classpath` | | Additional classpath of the `kotlin_compiler` service |
| `tock_bot_test_xray_url` | | URL of Xray: enables the Xray features of the test plans |

### Answers (i18n)

See [Building a multilingual bot](../studio/i18n.md).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_i18n_enabled` | `false` | Enables the multilingual answers |
| `tock_load_all_labels_of_default_namespace` | `true` | Loads all the labels of the default namespace at startup |
| `tock_i18n_stat_write_enabled` | `true` | Records the usage statistics of the labels |
| `tock_i18n_stat_refresh_in_ms` | `10000` | Period of the recording of these statistics |
| `tock_i18n_reset_value_on_default_change` | `false` | Resets the translations of a label when its default value changes |
| `tock_translator_deepl_api_key` | | DeepL API key (`tock-deepl-translate` module) |
| `tock_translator_deepl_api_url` | `https://api.deepl.com/v2/translate` | URL of the DeepL API |
| `tock_translator_deepl_target_languages` | all | Target languages supported by DeepL |
| `tock_translator_deepl_glossary_map_ids` | | DeepL glossaries, by language |

## NLP

These properties are read by the `nlp_api`, `bot_admin` and `build_worker` services.

| Property | Default | Description |
|----------|---------|-------------|
| `tock_nlp_root` | `/rest/nlp` | Root path of the NLP API |
| `nlpverticle_tock_nlp_protect_path` | `false` | Protects the NLP API with the authentication |
| `nlpverticle_tock_nlp_check_healthcheck` | `false` | Checks the entity providers in the healthcheck |
| `tock_nlp_model_refresh` | `true` | Reloads the models when they are rebuilt |
| `tock_nlp_model_fill_cache` | `false` | Loads all the models at startup |
| `tock_parser_validate_sentence_test` | `false` | In test mode, returns the validated qualification of a known sentence |
| `nlp_duckling_url` | `http://localhost:8889` | URL of the Duckling service |
| `tock_duckling_enabled` | `true` | Enables the Duckling entities (dates, numbers...) |
| `tock_duckling_request_timeout_ms` | `4000` | Timeout of the calls to Duckling |
| `tock_nlp_entity_type_url` | `http://localhost:5000/app/v1/` | URL of the REST entity provider (see [NLP and entity models](../develop/internals/nlp/models-and-entity-models.md)) |
| `tock_bot_rest_timeout_in_ms` | `10000` | Timeout of the calls to this provider |
| `rasa_model_path` | `models/` | Directory of the Rasa models |

### Data retention

See [Installation](installation.md#data-retention).

| Property | Default | Description |
|----------|---------|-------------|
| `tock_nlp_log_index_ttl_days` | `7` | Retention of the NLP request logs |
| `tock_nlp_log_stats_index_ttl_days` | `365` | Retention of the NLP statistics |
| `tock_user_log_index_ttl_days` | `365` | Retention of the logs of the _Tock Studio_ user actions |
| `tock_nlp_classified_sentences_index_ttl_days` | `-1` | Retention of the sentences not validated (`-1`: no expiration) |
| `tock_nlp_classified_sentences_index_ttl_intent_names` | | Limits this retention to the sentences of these intents |

### Model building

| Property | Default | Description |
|----------|---------|-------------|
| `tock_build_worker_mode` | `VERTICLE` | Execution mode of the `build_worker`: `VERTICLE` (service), `COMMAND_LINE`, `ON_DEMAND` or `DEV` |
| `tock_build_worker_verticle_enabled` | `true` | Deprecated: use `tock_build_worker_mode` |
| `tock_complete_model_enabled` | `true` | Periodically rebuilds the complete models |
| `tock_test_model_enabled` | `true` | Runs the model tests (see [Model quality](../studio/model-quality.md)) |
| `tock_test_model_timeframe` | `0,5` | Hours (start, end) during which the model tests run |
| `tock_cleanup_model_enabled` | `true` | Deletes the models that are no longer used |
| `tock_build_worker_on_demand_type` | `AWS_BATCH` | `ON_DEMAND` mode: type of the jobs |
| `tock_build_worker_on_demand_delay_in_minutes_between_job_rebuild_diff` | `60` | `ON_DEMAND` mode: delay between two model updates |
| `tock_build_worker_on_demand_timeframe_rebuild_diff` | `0,24` | `ON_DEMAND` mode: hours during which the models are updated |
| `tock_build_worker_on_demand_delay_in_minutes_between_job_test` | `1440` | `ON_DEMAND` mode: delay between two model tests |
| `tock_build_worker_on_demand_timeframe_test` | `0,5` | `ON_DEMAND` mode: hours during which the models are tested |
| `tock_build_worker_on_demand_delay_in_minutes_between_job_cleanup` | `720` | `ON_DEMAND` mode: delay between two cleanups |
| `tock_build_worker_on_demand_timeframe_cleanup` | `0,24` | `ON_DEMAND` mode: hours during which the cleanups run |
| `tock_worker_aws_batch_job_definition_name` | `tock-worker-job-definition` | AWS Batch: job definition |
| `tock_worker_aws_batch_job_queue_name` | `tock-worker-job-queue` | AWS Batch: job queue |
| `tock_worker_aws_batch_job_name` | `tock-worker-job` | AWS Batch: prefix of the job names |
| `tock_worker_aws_batch_attempt_duration_seconds` | `7200` | AWS Batch: maximum duration of a job |
| `tock_worker_aws_batch_vcpus` | `4` | AWS Batch: vCPUs of a job |
| `tock_worker_aws_batch_memory` | `12288` | AWS Batch: memory of a job, in MB |

### Amazon SageMaker models

| Property | Default | Description |
|----------|---------|-------------|
| `tock_sagemaker_aws_region_name` | `eu-west-3` | AWS region of the SageMaker endpoints |
| `tock_sagemaker_aws_profile_name` | `default` | AWS profile |
| `tock_sagemaker_aws_intent_endpoint_name` | `default` | Endpoint of the intent model |
| `tock_sagemaker_aws_entities_endpoint_name` | `default` | Endpoint of the entity model |
| `tock_sagemaker_aws_content_type` | `application/json` | Content type of the requests |
| `tock_sagemaker_aws_model_file_name` | `default` | Name of the model file |

## Gen AI orchestrator

The Python Gen AI orchestrator reads environment variables prefixed by `tock_gen_ai_orchestrator_`.
The vector store variables are described on the [Vector DB settings](../gen-ai/vector-store.md) page.

| Variable | Default | Description |
|----------|---------|-------------|
| `tock_gen_ai_orchestrator_application_environment` | `DEV` | `DEV` or `PROD` |
| `tock_gen_ai_orchestrator_application_logging_config_ini` | provided file | Logging configuration file |
| `tock_gen_ai_orchestrator_llm_provider_timeout` | `30` | Timeout of the LLM calls, in seconds |
| `tock_gen_ai_orchestrator_llm_provider_max_retries` | `0` | Number of retries of a failed LLM call |
| `tock_gen_ai_orchestrator_llm_rate_limits` | `true` | Enables the rate limit of the LLM calls |
| `tock_gen_ai_orchestrator_em_provider_timeout` | `4` | Timeout of the embedding calls, in seconds |
| `tock_gen_ai_orchestrator_compressor_provider_timeout` | `7` | Timeout of the document compressor calls, in seconds |
| `tock_gen_ai_orchestrator_observability_provider_timeout` | `3` | Timeout of the observability calls, in seconds |
| `tock_gen_ai_orchestrator_observability_provider_max_retries` | `0` | Number of retries of a failed observability call |
| `tock_gen_ai_orchestrator_db_pool_size` | `10` | Size of the PGVector connection pool |
| `tock_gen_ai_orchestrator_db_max_overflow` | `5` | Additional connections allowed beyond this size |
| `tock_gen_ai_orchestrator_db_pool_timeout` | `30` | Timeout to get a connection, in seconds |
| `tock_gen_ai_orchestrator_db_pool_recycle` | `3600` | Maximum lifetime of a connection, in seconds |
| `tock_gen_ai_orchestrator_vector_store_test_query` | `Any definition` | Query used to test the connection to the vector store |
| `tock_gen_ai_orchestrator_vector_store_test_max_docs_retrieved` | `4` | Number of documents retrieved by this test |
| `tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name` | | AWS profile used to call [AWS Bedrock](../gen-ai/providers/llm-embedding.md#aws-bedrock) |
| `tock_gen_ai_orchestrator_aws_bedrock_credentials_allow_default_profile` | `false` | Without profile, uses the default AWS credential chain (IAM role, IRSA, environment variables) instead of failing |
| `tock_gcp_project_id` | | GCP project of the secrets, with the GCP secret manager |

## Other modules

### Xray tests

Properties of the `tock-xray-plugin` module, which runs the Xray test plans of a bot.

| Property | Default | Description |
|----------|---------|-------------|
| `tock_bot_test_url` | | URL of _Tock Studio_ |
| `tock_bot_test_login` | | _Tock Studio_ login |
| `tock_bot_test_password` | | _Tock Studio_ password |
| `tock_bot_test_timeout_in_ms` | `3600000` | Timeout of the calls to _Tock Studio_ |
| `tock_bot_test_botId` | | Identifier of the tested bot |
| `tock_bot_test_configuration_ids` | | Identifiers of the tested configurations |
| `tock_bot_test_configuration_names` | | Names of the tested configurations |
| `tock_bot_test_locale` | value of `tock_default_locale` | Locale of the tests |
| `tock_bot_test_start_sentence` | | Sentence sent at the beginning of each test |
| `tock_bot_test_xray_login` | | Xray login |
| `tock_bot_test_xray_password` | | Xray password |
| `tock_bot_test_xray_timeout_in_ms` | `60000` | Timeout of the calls to Xray |
| `tock_bot_test_xray_test_plan_keys` | | Keys of the test plans to run |
| `tock_bot_test_xray_test_keys` | | Keys of the tests to run |
| `tock_bot_test_xray_test_plan_env` | | Environment reported in the test executions |
| `tock_bot_test_xray_test_plan_bot_url` | | Bot URL reported in the test executions |
| `tock_bot_test_jira_project` | | Jira project of the tests |
| `tock_bot_test_jira_xray_test_type_field` | | Jira field of the test type |
| `tock_bot_test_jira_xray_manual_test_field` | | Jira field of the manual test steps |
| `tock_bot_test_jira_xray_automation_type_field` | | Jira field of the automation type |
| `tock_bot_test_jira_linked_field` | | Jira field linking the tests |
| `tock_bot_test_jira_registered_bot_url` | `${botUrl}` | Bot URL recorded in Jira |
| `tock_bot_test_connector_jira_map` | | Jira components by connector |
| `tock_bot_test_precondition_key_text_chat` | | Xray precondition of the text tests |
| `tock_bot_test_precondition_key_voice_assistant` | | Xray precondition of the voice tests |
| `tock_bot_test_precondition_key_text_and_voice_assistant` | | Xray precondition of the text and voice tests |
| `tock_bot_xray_creation_keyword` | `_xray_` | Keyword creating an Xray test from the current dialog |
| `tock_bot_xray_update_keyword` | `_xray_update_` | Keyword updating an Xray test from the current dialog |

### Dialogflow

| Property | Default | Description |
|----------|---------|-------------|
| `dialogflow_project_id` | | Google Cloud project of the Dialogflow agent to import |
