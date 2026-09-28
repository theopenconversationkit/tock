---
title: Référence de configuration
---

# Référence de configuration

Cette page liste les propriétés de configuration des composants Tock.

## Définir une propriété

Chaque propriété est lue dans une propriété système de la JVM ou, à défaut, dans une variable d'environnement du même nom.
Avec Docker Compose par exemple :

```yaml
services:
  admin_web:
    image: tock/bot_admin:$TAG
    environment:
      - tock_env=prod
      - tock_mongo_url=mongodb://mongo:27017/?replicaSet=tock
```

ou en ligne de commande : `java -Dtock_env=prod ...`.

Les formats de valeurs sont :

* Durées : l'unité est donnée par le suffixe du nom (`_ms`, `_in_s`, `_in_minutes`, `_hours`, `_days`...)
* Listes : valeurs séparées par des virgules, par exemple `tock_users=alice@tock.ai,bob@tock.ai`
* Maps : entrées séparées par `|`, clés et valeurs séparées par `=`, par exemple `id1=namespace1|id2=namespace2`

Une propriété n'est lue que par les composants qui embarquent le module correspondant : par exemple, les propriétés
des connecteurs sont lues par le bot (service `bot_api` en mode _Bot API_, ou votre bot en mode _Bot intégré_).

> Cette liste est construite à partir du code source avec le script `etc/list-doc-properties.py`, qui liste les
> propriétés lues par les modules Kotlin et celles qui manquent dans cette page (option `--check`).

## Propriétés communes

Ces propriétés sont lues par tous les composants Tock (`bot_admin`, `nlp_api`, `build_worker`, `bot_api`,
`kotlin_compiler`, `duckling_bot`) et par les bots développés en mode _Bot intégré_.

### Environnement

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_env` | `dev` | Environnement. Toute autre valeur que `dev` active les comportements de production (cookies HTTPS, CORS, phrase de chiffrement obligatoire...) |
| `tock_default_locale` | `en` | Locale par défaut de la plateforme |
| `tock_default_zone` | `UTC` | Fuseau horaire par défaut |
| `tock_default_namespace` | `app` | Namespace par défaut |
| `tock_namespace_open_access` | `false` | Donne à chaque utilisateur l'accès à tous les namespaces. Désactivé par défaut pour des raisons de sécurité |
| `tock_vertx_worker_pool_size` | défaut de Vert.x | Taille du pool de threads _worker_ de Vert.x |
| `tock_cache_in_memory_maximum_size` | `10000` | Nombre maximal d'entrées des caches en mémoire |
| `tock_cache_in_memory_expiration_in_ms` | `3600000` | Expiration des entrées des caches en mémoire, après le dernier accès |

### MongoDB

Voir [Installation](installation.md#base-de-donnees-mongodb).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_mongo_url` | `mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=tock&retryWrites=true` | Chaîne de connexion MongoDB. Un _replica set_ est obligatoire |
| `tock_async_mongo_url` | valeur de `tock_mongo_url` | Chaîne de connexion du driver asynchrone |
| `tock_bot_mongo_db` | `tock_bot` | Nom de la base des bots (dialogues, utilisateurs, stories, réponses...) |
| `tock_front_mongo_db` | `tock_front` | Nom de la base des applications NLP (intentions, entités, phrases) |
| `tock_model_mongo_db` | `tock_model` | Nom de la base des modèles NLP |
| `tock_cache_mongo_db` | `tock_cache` | Nom de la base du cache partagé |
| `tock_document_db_on` | `false` | Active le mode de compatibilité avec [Amazon DocumentDB](https://aws.amazon.com/fr/documentdb/) |
| `tock_database_mongodb_secret_manager_provider` | | Gestionnaire de secrets contenant les identifiants MongoDB : `AWS_SECRETS_MANAGER` ou `GCP_SECRET_MANAGER` |
| `tock_database_mongodb_credentials_secret_name` | `database_mongodb_credentials` | Nom du secret contenant les identifiants MongoDB |

### Authentification _Tock Studio_

Voir [Sécurité](security.md#authentification). Sans aucune des propriétés `*_enabled` ci-dessous, les utilisateurs
sont définis par les propriétés `tock_users`, `tock_passwords`, `tock_organizations` et `tock_roles`.

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_users` | valeur de `tock_user` | Identifiants des utilisateurs |
| `tock_passwords` | valeur de `tock_password` | Mots de passe des utilisateurs, dans le même ordre |
| `tock_organizations` | valeur de `tock_default_namespace` | Namespaces des utilisateurs, dans le même ordre |
| `tock_roles` | tous les rôles | Rôles des utilisateurs, dans le même ordre. Les rôles d'un utilisateur sont séparés par `\|` |
| `tock_user` | `admin@app.com` | Identifiant de l'utilisateur par défaut, quand `tock_users` n'est pas défini |
| `tock_password` | `password` | Mot de passe de l'utilisateur par défaut, quand `tock_passwords` n'est pas défini |
| `tock_oauth2_enabled` | `false` | Active l'authentification OAuth2 générique |
| `tock_oauth2_client_id` | | Identifiant du client OAuth2 |
| `tock_oauth2_secret_key` | | Secret du client OAuth2 |
| `tock_oauth2_site_url` | | URL du fournisseur OAuth2 |
| `tock_oauth2_access_token_path` | | Chemin relatif de l'endpoint de token |
| `tock_oauth2_authorize_path` | | Chemin relatif de l'endpoint d'autorisation |
| `tock_oauth2_userinfo_path` | | Chemin relatif de l'endpoint d'informations utilisateur |
| `tock_oauth2_supported_grant_types` | | Types de _grant_ supportés |
| `tock_oauth2_user_role_attribute` | `custom:roles` | Attribut des informations utilisateur contenant les rôles |
| `tock_oauth2_proxy_host` | | Hôte du proxy HTTP utilisé pour joindre le fournisseur OAuth2 ou GitHub |
| `tock_oauth2_proxy_port` | `0` | Port de ce proxy |
| `tock_oauth2_proxy_username` | | Utilisateur de ce proxy |
| `tock_oauth2_proxy_password` | | Mot de passe de ce proxy |
| `tock_custom_roles_mapping` | | Correspondance entre les rôles du fournisseur OAuth2 et les rôles Tock |
| `tock_custom_namespace_mapping` | | OAuth2 : correspondance entre profils utilisateurs et namespaces Tock (par exemple `id1=namespace1\|id2=namespace2`). Keycloak : attribut contenant le namespace (`tock_namespace` par défaut) |
| `tock_bot_admin_rest_default_base_url` | `http://localhost:8080` | URL de _Tock Studio_, pour rediriger l'utilisateur après l'authentification OAuth2 ou Keycloak |
| `tock_keycloak_enabled` | `false` | Active l'authentification Keycloak |
| `tock_keycloak_client_id` | `tock` | Identifiant du client Keycloak |
| `tock_keycloak_secret_key` | | Secret du client Keycloak |
| `tock_keycloak_site_url` | `https://keycloak/realms/myrealm` | URL du _realm_ Keycloak |
| `tock_keycloak_access_token_path` | `/protocol/openid-connect/token` | Chemin relatif de l'endpoint de token |
| `tock_keycloak_authorize_path` | `/protocol/openid-connect/auth` | Chemin relatif de l'endpoint d'autorisation |
| `tock_keycloak_userinfo_path` | `/protocol/openid-connect/userinfo` | Chemin relatif de l'endpoint d'informations utilisateur |
| `tock_keycloak_grant_types` | | Types de _grant_ supportés |
| `tock_keycloak_user_role_attribute` | `tock_roles` | Attribut contenant les rôles |
| `tock_keycloak_proxy_host` | | Hôte du proxy HTTP utilisé pour joindre Keycloak |
| `tock_keycloak_proxy_port` | `0` | Port de ce proxy |
| `tock_github_oauth_enabled` | `false` | Active l'authentification GitHub |
| `tock_github_oauth_client_id` | | Identifiant client de l'application OAuth GitHub |
| `tock_github_oauth_secret_key` | | Secret client de l'application OAuth GitHub |
| `tock_github_api_request_timeout_ms` | `5000` | Délai maximal des appels à l'API GitHub |
| `tock_cas_auth_enabled` | `false` | Active l'authentification CAS (nécessite un module d'authentification CAS) |
| `tock_cas_join_same_namespace_per_user` | `true` | Place les utilisateurs d'une même organisation dans le même namespace |
| `tock_cas_auth_proxy_host` | | Hôte du proxy HTTP utilisé pour joindre le serveur CAS |
| `tock_cas_auth_proxy_port` | | Port de ce proxy |
| `aws_region` | `eu-west-1` | Région AWS de l'Application Load Balancer, pour l'authentification par son JWT |
| `tock_aws_public_key_request_timeout_ms` | `30000` | Délai maximal des appels récupérant les clés publiques de l'ALB AWS |

### Serveur web

Les propriétés préfixées par `<verticle>_` s'appliquent à un serveur HTTP (_verticle_) de Tock. Le préfixe est le nom
de la classe du verticle en minuscules : `botadminverticle` (`bot_admin`), `nlpverticle` (`nlp_api`),
`botverticle` (bot et `bot_api`), `kotlincompilerverticle` (`kotlin_compiler`), `ducklingverticle` (`duckling_bot`),
`healthcheckverticle` (`build_worker`). Par exemple, `botverticle_port` définit le port HTTP du bot.

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `<verticle>_port` | `8080` | Port HTTP |
| `<verticle>_body_limit` | `1000000` | Taille maximale du corps d'une requête, en octets |
| `<verticle>_tock_vertx_compression_supported` | `true` | Active la compression HTTP |
| `<verticle>_tock_vertx_healthcheck_path` | `<chemin racine>/healthcheck` | Chemin du _healthcheck_ (voir [Supervision](supervision.md#lignes-de-vie-healthchecks)) |
| `<verticle>_tock_vertx_readinesscheck_path` | `/health/readiness` | Chemin de la sonde de _readiness_ |
| `<verticle>_tock_vertx_livenesscheck_path` | `/health/liveness` | Chemin de la sonde de _liveness_ |
| `tock_detailed_healthcheck_enabled` | `false` | Renvoie l'état de chaque dépendance dans le _healthcheck_ (voir [Supervision](supervision.md#mode-detaille)) |
| `tock_https_env` | `true` | Cookies de session sécurisés (HTTPS uniquement). Ignoré en environnement de dev |
| `tock_vertx_session_expiration_timeout` | `21600000` | Expiration des sessions _Tock Studio_, en millisecondes (6 heures) |
| `tock_web_use_default_cors_handler` | `true` en dev, sinon `false` | Ajoute un gestionnaire CORS aux serveurs HTTP |
| `tock_web_use_default_cors_handler_url` | `http://localhost:4200` (`*` pour le bot) | Origines autorisées par ce gestionnaire CORS, séparées par `\|` (`*` : toutes) |
| `tock_web_use_default_cors_handler_with_credentials` | `true` (`false` pour le bot) | Autorise les identifiants dans les requêtes CORS |
| `tock_web_cookie_auth` | | Identification par défaut des utilisateurs des connecteurs web : `encrypted` (cookie chiffré), `basic` ou `true` (cookie simple). Sinon, l'identifiant envoyé par le client est utilisé |
| `tock_web_cookie_auth_max_age` | `-1` | Durée de vie maximale du cookie d'authentification, en secondes (`-1` : cookie de session) |
| `tock_web_cookie_auth_path` | | Chemin du cookie d'authentification |
| `tock_web_sse_keepalive_delay` | `10` | Délai entre deux messages de maintien des connexions SSE, en secondes |
| `tock_file_upload_directory` | `file-uploads` | Répertoire des fichiers téléversés |

### Chiffrement et secrets

Voir [Sécurité](security.md#donnees).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_encrypt_pass` | | Phrase de passe utilisée pour chiffrer les données sensibles. Obligatoire hors environnement de dev dès que le chiffrement est utilisé |
| `tock_gen_ai_secret_manager_provider` | | Gestionnaire de secrets contenant les secrets Gen AI (clés d'API) : `AWS_SECRETS_MANAGER` ou `GCP_SECRET_MANAGER` |
| `tock_gen_ai_secret_prefix` | `LOCAL/TOCK` | Préfixe des noms des secrets Gen AI |

### Logs et appels HTTP sortants

Voir [Supervision](supervision.md#journalisation-logs).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_logback_enabled` | `true` | Active la configuration Logback par défaut de Tock |
| `tock_default_log_level` | `DEBUG` en dev, sinon `INFO` | Niveau de log par défaut |
| `tock_logback_file_appender` | `false` | Écrit aussi les logs dans `log/logFile.log` |
| `tock_retrofit_log_level` | `BODY` en dev, sinon `NONE` | Niveau de log des appels HTTP sortants : `NONE`, `BASIC`, `HEADERS` ou `BODY` |
| `tock_proxy_user` | | Utilisateur du proxy HTTP utilisé pour les appels sortants |
| `tock_proxy_password` | | Mot de passe de ce proxy |
| `tock_circuit_breaker` | `false` | Active un _circuit breaker_ sur les appels sortants qui le permettent (Duckling) |

## Bot

Ces propriétés sont lues par les bots : le service `bot_api` en mode _Bot API_, ou votre application de bot
en mode _Bot intégré_.

### Moteur du bot

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_configuration_bot_default_base_url` | `http://<IP locale>:<botverticle_port>` | URL de base du bot, proposée par défaut dans les configurations de connecteurs de _Tock Studio_ |
| `tock_bot_protected_paths` | `/admin` | Chemins du bot protégés par l'authentification |
| `tock_bot_protected_path` | `/admin` | Obsolète : utiliser `tock_bot_protected_paths` |
| `tock_restricted_configuration_id` | | Ne charge que les configurations de bot indiquées (développement) |
| `tock_bot_default_breath_ms` | `1000` | Délai par défaut entre deux messages du bot |
| `tock_bot_min_breath_ms` | `50` | Délai minimal entre deux messages, quand un message met plus longtemps que le délai par défaut à être préparé |
| `tock_technical_error` | `Technical error :( sorry!` | Message envoyé à l'utilisateur en cas d'erreur technique |
| `tock_gen_ai_orchestrator_technical_error` | `Technical error :( sorry!` | Message envoyé à l'utilisateur quand l'orchestrateur Gen AI échoue |
| `tock_ask_again_round` | `1` | Nombre de fois par défaut où le bot repose une question |
| `tock_bot_send_choice_activate` | `true` | Un clic sur un bouton réactive un bot désactivé |
| `tock_bot_disabled_duration_in_minutes` | `7200` | Durée de désactivation du bot pour un utilisateur (5 jours) |
| `tock_bot_refresh_profil_duration_in_minutes` | `7200` | Délai avant de rafraîchir le profil utilisateur depuis le canal (5 jours) |
| `tock_bot_wait_nlp_availability_in_ms` | `5000` | Temps d'attente du service NLP au démarrage |
| `tock_cleanup_delay_seconds` | `60` | Délai maximal pour envoyer les messages en attente d'une story coroutine |
| `tock_bot_audio_nlp_enabled` | `true` | Envoie les messages audio au NLP (_speech to text_) |
| `tock_bot_audio_nlp_max_size` | `1048576` | Taille maximale des messages audio envoyés au NLP, en octets |
| `tock_bot_serve_files` | `true` | Sert les fichiers téléversés dans _Tock Studio_ (images des réponses...) |
| `tock_bot_serve_files_path` | `/f/` | Chemin de ces fichiers |
| `tock_nlp_proxy_on_bot` | `false` | Expose l'API NLP à travers le bot |
| `tock_nlp_proxy_on_bot_path` | `/_proxy_nlp` | Chemin de ce proxy NLP |
| `tock_websocket_enabled` | `false` | Active l'endpoint _WebSocket_ du mode _Bot API_ |
| `tock_timeline_persistence_synchronous_mode` | `true` | Obsolète : utiliser les [stories coroutines](../develop/coroutine-stories.md) |
| `tock_timeline_persistence_asynchronous_mode` | `false` | Obsolète : utiliser les [stories coroutines](../develop/coroutine-stories.md) |
| `tock_orchestration` | `false` | Active l'orchestration entre bots |
| `tock_orchestration_lock_ttl_in_min` | `60` | Durée de vie des verrous d'orchestration |
| `tock_gen_ai_orchestrator_server_url` | `http://localhost:8000` | URL de l'orchestrateur Gen AI |
| `tock_gen_ai_orchestrator_client_request_timeout_ms` | `55000` | Délai maximal des appels à l'orchestrateur Gen AI |
| `tock_gen_ai_orchestrator_vector_store_provider` | `PGVector` | Type de la base vectorielle par défaut (voir [Vector DB settings](../gen-ai/vector-store.md)) |
| `tock_nlp_client_request_timeout_ms` | `20000` | Délai maximal des appels à l'API NLP |

### Mots-clés et utilisateur de test

Mots-clés envoyés comme message utilisateur pour agir sur la conversation, surtout utilisés pour les tests.

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_bot_delete_keyword` | `_delete_user_` | Supprime les données de l'utilisateur |
| `tock_bot_enable_keyword` | `_enable_user_` | Active le bot pour l'utilisateur |
| `tock_bot_disable_keyword` | `_disable_user_` | Désactive le bot pour l'utilisateur |
| `tock_bot_test_context_keyword` | `_test_` | Démarre un contexte de test |
| `tock_bot_end_test_context_keyword` | `_end_test_` | Termine le contexte de test |
| `tock_bot_test_first_name` | `Joe` | Prénom de l'utilisateur de test |
| `tock_bot_test_last_name` | `Hisaishi` | Nom de l'utilisateur de test |

### Stockage des dialogues

Voir [Installation](installation.md#conservation-des-donnees).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_bot_timeline_index_ttl_days` | `365` | Conservation des _timelines_ utilisateurs (profils et préférences) |
| `tock_bot_dialog_index_ttl_days` | `7` | Conservation des dialogues |
| `tock_bot_flow_stats_index_ttl_days` | `365` | Conservation des statistiques de flux de conversation |
| `tock_bot_alternative_index_ttl_hours` | `1` | Conservation des alternatives choisies pour les réponses à choix multiples |
| `tock_bot_dialog_max_validity_in_seconds` | `86400` | Âge maximal d'un dialogue pour être poursuivi. Au-delà, un nouveau dialogue commence |
| `tock_bot_max_actions_by_dialog` | `1000` | Nombre maximal d'actions conservées dans un dialogue |
| `tock_bot_dialog_flow_stat` | `true` | Enregistre les statistiques de flux de conversation |
| `tock_dialog_flow_crawl_stats` | `true` | Calcule périodiquement les statistiques de flux de conversation |
| `tock_bot_add_namespace_to_timeline_id` | `false` | Préfixe les identifiants utilisateurs par le namespace, pour séparer les utilisateurs de plusieurs namespaces |
| `tock_bot_encrypted_flags` | | _Flags_ de dialogue dont la valeur est chiffrée |
| `tock_bot_lock_timeout_in_ms` | `5000` | Durée de vie du verrou d'un utilisateur, pendant le traitement d'un message |
| `tock_bot_locked_attempts_wait_in_ms` | `500` | Délai entre deux tentatives d'acquisition du verrou |
| `tock_bot_lock_retry_jitter_in_ms` | `20` | Délai aléatoire ajouté à ce délai |
| `tock_bot_max_locked_attempts` | `10` | Nombre de tentatives avant abandon |
| `tock_bot_warn_after_locked_attempts` | `5` | Nombre de tentatives avant de logger un avertissement |
| `mongo_user_ttl_hours` | `6` | Conservation des verrous utilisateurs |

### Mode _Bot API_

Voir [Bot API](../develop/bot-api.md).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_bot_api_timeout_in_ms` | `60000` | Délai maximal des appels au webhook du bot |
| `tock_bot_api_connection_timeout_in_ms` | `3000` | Délai maximal de connexion de ces appels |
| `tock_bot_api_webhook_check_reachability` | `true` | Vérifie que le webhook est joignable avant de l'appeler |
| `tock_bot_api_webhook_reachability_in_ms` | `10000` | Délai maximal de cette vérification |
| `tock_api_timout_in_s` | `10` | Délai maximal de la réponse d'un bot connecté en _WebSocket_ |
| `tock_api_old_webhook_behaviour` | `false` | Désactive le _streaming_ (SSE) des réponses du webhook |
| `tock_bot_api_actions_history_to_client_bus` | `false` | Envoie l'historique des actions du dialogue au bot à chaque requête |
| `tock_websocket_host` | `localhost` | Hôte du service `bot_api`, pour le client _WebSocket_ |
| `tock_websocket_port` | `8080` | Port du service `bot_api`, pour le client _WebSocket_ |
| `tock_websocket_ssl` | `false` | Utilise TLS pour la connexion _WebSocket_ |

## Connecteurs

### Web

Voir [Web](../channels/web.md).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_web_sse` | `false` | Active l'endpoint SSE (_Server-Sent Events_) |
| `tock_web_direct_sse` | `false` | Envoie les réponses directement sur la connexion SSE, sans les stocker |
| `tock_web_cors_pattern` | `.*` | Origines autorisées à appeler le connecteur |
| `tock_web_enable_markdown` | `false` | Convertit le Markdown des réponses en HTML |
| `allow_markdown` | | Ancien nom de `tock_web_enable_markdown` |
| `tock_web_connector_persist_profile` | `false` | Stocke le profil utilisateur envoyé par le client |
| `tock_web_connector_extra_headers` | | En-têtes HTTP de la requête transmis au bot |
| `tock_web_connector_use_extra_header_as_metadata_request` | `false` | Transmet ces en-têtes comme métadonnées de la requête |
| `tock_web_connector_merge_stream_response` | `true` | Fusionne les morceaux d'une réponse en _streaming_ en un seul message |
| `tock_web_connector_bridge_enabled` | `false` | Permet à une requête de cibler un autre connecteur web avec son champ `connectorId` |
| `tock_web_sse_message_queue_ttl_days` | `-1` | Conservation des messages SSE en attente de livraison (`-1` : pas d'expiration) |
| `tock_web_sse_message_queue_max_count` | `50000` | Nombre maximal de messages SSE en attente de livraison |
| `tock_web_sse_message_queue_max_size_kb` | `100000` | Taille maximale de ces messages, en kilo-octets |

### API OpenAI

Voir [API OpenAI](../channels/openai.md). Ce connecteur lit aussi `tock_web_sse` et `tock_web_cors_pattern`.

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_openai_connector_persist_profile` | `false` | Stocke le profil utilisateur envoyé par le client |
| `tock_openai_support_unstreamed` | `false` | Accepte les requêtes sans _streaming_ |

### WhatsApp

Voir [WhatsApp](../channels/whatsapp.md).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_whatsappcloud_request_timeout_ms` | `30000` | Délai maximal des appels à l'API WhatsApp |
| `tock_whatsappcloud_request_gzip` | `false` | Compresse les requêtes |
| `tock_whatsapp_cloud_restricted_phone_numbers` | | Ne répond qu'à ces numéros de téléphone (tests) |
| `tock_whatsapp_sync_templates` | `false` | Synchronise les modèles de messages avec WhatsApp |
| `tock_whatsapp_reupload_images` | `true` | Téléverse les images vers WhatsApp au lieu d'envoyer leur URL |
| `tock_whatsapp_error_on_invalid_messages` | `false` | Échoue au lieu de logger un avertissement sur un message qui ne respecte pas les limites de WhatsApp |
| `tock_whatsapp_persistent_cache` | `false` | Stocke les identifiants utilisateurs hachés dans MongoDB. Nécessite `tock_encrypt_pass` |
| `tock_whatsapp_memory_timeout_in_minutes` | `60` | Expiration du cache en mémoire de ces identifiants |
| `tock_whatsapp_payload` | `whatsapp_payload` | Collection stockant les _payloads_ des boutons |
| `tock_whatsapp_payload_ttl_days` | `10` | Conservation de ces _payloads_ |

### Messenger

Voir [Messenger](../channels/messenger.md).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_messenger_request_timeout_ms` | `30000` | Délai maximal des appels à l'API Messenger |
| `tock_messenger_request_gzip` | `true` | Compresse les requêtes |
| `messenger_retries_on_error_limit` | `1` | Nombre de nouvelles tentatives d'un appel en échec |
| `messenger_retries_on_error_wait_in_ms` | `5000` | Délai entre deux tentatives |
| `tock_messenger_extended_profile_fields` | | Champs supplémentaires du profil utilisateur à récupérer |
| `tock_bot_messenger_reuse_attachment` | `true` | Réutilise les pièces jointes déjà téléversées vers Messenger |
| `tock_messenger_webhook_check_subscription` | `false` | Vérifie périodiquement l'abonnement webhook des pages |
| `tock_messenger_webhook_check_period` | `600` | Période de cette vérification, en secondes |
| `tock_messenger_webhook_url` | URL de l'abonnement actuel | URL du webhook utilisée pour renouveler l'abonnement |

### Autres connecteurs

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_microsoft_request_timeout` | `5000` | Teams : délai maximal des appels récupérant les clés Microsoft, en millisecondes |
| `tock_whatsapp_request_timeout_ms` | `30000` | Teams : délai maximal des appels à l'API Bot Framework (malgré son nom) |
| `tock_mattermost_request_timeout_ms` | `30000` | Mattermost : délai maximal des appels à l'API Mattermost |
| `tock_slack_old_api_style` | `false` | Slack : utilise l'ancien format de l'API Slack |
| `tock_api_google_chat_connector_test_send_intro_message` | `false` | Google Chat : envoie un message d'introduction en mode test |
| `tock_rest_connector_disabled` | `false` | Désactive le connecteur REST utilisé par les tests de _Tock Studio_ |
| `tock_rest_connector_check_nlp_stats` | `false` | Connecteur REST : renvoie les statistiques NLP avec la réponse |
| `tock_bot_rest_client_request_timeout_ms` | `100000` | Délai maximal des appels du client du connecteur REST |
| `tock_iadvize_secret_manager_provider` | `ENV` | iAdvize : gestionnaire de secrets contenant les identifiants |
| `tock_iadvize_credentials_secret_name` | `iadvize_credentials` | iAdvize : nom du secret contenant les identifiants |

## _Tock Studio_

Ces propriétés sont lues par le service `bot_admin`.

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `botadminverticle_base_href` | `/` | Chemin de base de _Tock Studio_ (voir [Installation](installation.md#mettre-a-disposition-linterface-dadministration-dans-un-sous-repertoire)) |
| `botadminverticle_content_path` | `/maven/dist` | Répertoire de l'application web _Tock Studio_ |
| `botadminverticle_nlp_external_host` | `localhost:8888` | Hôte de l'API NLP affiché dans _Tock Studio_ |
| `tock_bot_api` | `false` | Affiche les fonctionnalités _Bot API_ dans _Tock Studio_ |
| `tock_bot_global_message` | | Message affiché dans un bandeau de _Tock Studio_ |
| `tock_csv_delimiter` | `;` | Délimiteur des exports CSV |
| `tock_bot_compiler_disabled` | `false` | Désactive le compilateur de scripts |
| `tock_bot_compiler_service_url` | `http://localhost:8887` | URL du service `kotlin_compiler` |
| `tock_bot_compiler_timeout_in_ms` | `60000` | Délai maximal des appels à ce service |
| `tock_kotlin_compiler_classpath` | | _Classpath_ supplémentaire du service `kotlin_compiler` |
| `tock_bot_test_xray_url` | | URL de Xray : active les fonctionnalités Xray des plans de test |

### Réponses (i18n)

Voir [Construire un bot multilingue](../studio/i18n.md).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_i18n_enabled` | `false` | Active les réponses multilingues |
| `tock_load_all_labels_of_default_namespace` | `true` | Charge tous les libellés du namespace par défaut au démarrage |
| `tock_i18n_stat_write_enabled` | `true` | Enregistre les statistiques d'utilisation des libellés |
| `tock_i18n_stat_refresh_in_ms` | `10000` | Période d'enregistrement de ces statistiques |
| `tock_i18n_reset_value_on_default_change` | `false` | Réinitialise les traductions d'un libellé quand sa valeur par défaut change |
| `tock_translator_deepl_api_key` | | Clé d'API DeepL (module `tock-deepl-translate`) |
| `tock_translator_deepl_api_url` | `https://api.deepl.com/v2/translate` | URL de l'API DeepL |
| `tock_translator_deepl_target_languages` | toutes | Langues cibles prises en charge par DeepL |
| `tock_translator_deepl_glossary_map_ids` | | Glossaires DeepL, par langue |

## NLP

Ces propriétés sont lues par les services `nlp_api`, `bot_admin` et `build_worker`.

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_nlp_root` | `/rest/nlp` | Chemin racine de l'API NLP |
| `nlpverticle_tock_nlp_protect_path` | `false` | Protège l'API NLP par l'authentification |
| `nlpverticle_tock_nlp_check_healthcheck` | `false` | Vérifie les fournisseurs d'entités dans le _healthcheck_ |
| `tock_nlp_model_refresh` | `true` | Recharge les modèles quand ils sont reconstruits |
| `tock_nlp_model_fill_cache` | `false` | Charge tous les modèles au démarrage |
| `tock_parser_validate_sentence_test` | `false` | En mode test, renvoie la qualification validée d'une phrase connue |
| `nlp_duckling_url` | `http://localhost:8889` | URL du service Duckling |
| `tock_duckling_enabled` | `true` | Active les entités Duckling (dates, nombres...) |
| `tock_duckling_request_timeout_ms` | `4000` | Délai maximal des appels à Duckling |
| `tock_nlp_entity_type_url` | `http://localhost:5000/app/v1/` | URL du fournisseur d'entités REST (voir [Modèles NLP et d'entités](../develop/internals/nlp/models-and-entity-models.md)) |
| `tock_bot_rest_timeout_in_ms` | `10000` | Délai maximal des appels à ce fournisseur |
| `rasa_model_path` | `models/` | Répertoire des modèles Rasa |

### Conservation des données

Voir [Installation](installation.md#conservation-des-donnees).

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_nlp_log_index_ttl_days` | `7` | Conservation des logs des requêtes NLP |
| `tock_nlp_log_stats_index_ttl_days` | `365` | Conservation des statistiques NLP |
| `tock_user_log_index_ttl_days` | `365` | Conservation des logs des actions des utilisateurs de _Tock Studio_ |
| `tock_nlp_classified_sentences_index_ttl_days` | `-1` | Conservation des phrases non validées (`-1` : pas d'expiration) |
| `tock_nlp_classified_sentences_index_ttl_intent_names` | | Limite cette conservation aux phrases de ces intentions |

### Construction des modèles

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_build_worker_mode` | `VERTICLE` | Mode d'exécution du `build_worker` : `VERTICLE` (service), `COMMAND_LINE`, `ON_DEMAND` ou `DEV` |
| `tock_build_worker_verticle_enabled` | `true` | Obsolète : utiliser `tock_build_worker_mode` |
| `tock_complete_model_enabled` | `true` | Reconstruit périodiquement les modèles complets |
| `tock_test_model_enabled` | `true` | Exécute les tests des modèles (voir [Qualité du modèle](../studio/model-quality.md)) |
| `tock_test_model_timeframe` | `0,5` | Heures (début, fin) pendant lesquelles les tests des modèles s'exécutent |
| `tock_cleanup_model_enabled` | `true` | Supprime les modèles qui ne sont plus utilisés |
| `tock_build_worker_on_demand_type` | `AWS_BATCH` | Mode `ON_DEMAND` : type des jobs |
| `tock_build_worker_on_demand_delay_in_minutes_between_job_rebuild_diff` | `60` | Mode `ON_DEMAND` : délai entre deux mises à jour des modèles |
| `tock_build_worker_on_demand_timeframe_rebuild_diff` | `0,24` | Mode `ON_DEMAND` : heures pendant lesquelles les modèles sont mis à jour |
| `tock_build_worker_on_demand_delay_in_minutes_between_job_test` | `1440` | Mode `ON_DEMAND` : délai entre deux tests des modèles |
| `tock_build_worker_on_demand_timeframe_test` | `0,5` | Mode `ON_DEMAND` : heures pendant lesquelles les modèles sont testés |
| `tock_build_worker_on_demand_delay_in_minutes_between_job_cleanup` | `720` | Mode `ON_DEMAND` : délai entre deux nettoyages |
| `tock_build_worker_on_demand_timeframe_cleanup` | `0,24` | Mode `ON_DEMAND` : heures pendant lesquelles les nettoyages s'exécutent |
| `tock_worker_aws_batch_job_definition_name` | `tock-worker-job-definition` | AWS Batch : définition de job |
| `tock_worker_aws_batch_job_queue_name` | `tock-worker-job-queue` | AWS Batch : file de jobs |
| `tock_worker_aws_batch_job_name` | `tock-worker-job` | AWS Batch : préfixe des noms de jobs |
| `tock_worker_aws_batch_attempt_duration_seconds` | `7200` | AWS Batch : durée maximale d'un job |
| `tock_worker_aws_batch_vcpus` | `4` | AWS Batch : vCPU d'un job |
| `tock_worker_aws_batch_memory` | `12288` | AWS Batch : mémoire d'un job, en Mo |

### Modèles Amazon SageMaker

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_sagemaker_aws_region_name` | `eu-west-3` | Région AWS des endpoints SageMaker |
| `tock_sagemaker_aws_profile_name` | `default` | Profil AWS |
| `tock_sagemaker_aws_intent_endpoint_name` | `default` | Endpoint du modèle d'intentions |
| `tock_sagemaker_aws_entities_endpoint_name` | `default` | Endpoint du modèle d'entités |
| `tock_sagemaker_aws_content_type` | `application/json` | Type de contenu des requêtes |
| `tock_sagemaker_aws_model_file_name` | `default` | Nom du fichier du modèle |

## Orchestrateur Gen AI

L'orchestrateur Gen AI, en Python, lit des variables d'environnement préfixées par `tock_gen_ai_orchestrator_`.
Les variables de la base vectorielle sont décrites dans la page [Vector DB settings](../gen-ai/vector-store.md).

| Variable | Défaut | Description |
|----------|--------|-------------|
| `tock_gen_ai_orchestrator_application_environment` | `DEV` | `DEV` ou `PROD` |
| `tock_gen_ai_orchestrator_application_logging_config_ini` | fichier fourni | Fichier de configuration des logs |
| `tock_gen_ai_orchestrator_llm_provider_timeout` | `30` | Délai maximal des appels aux LLM, en secondes |
| `tock_gen_ai_orchestrator_llm_provider_max_retries` | `0` | Nombre de nouvelles tentatives d'un appel LLM en échec |
| `tock_gen_ai_orchestrator_llm_rate_limits` | `true` | Active la limitation de débit des appels aux LLM |
| `tock_gen_ai_orchestrator_em_provider_timeout` | `4` | Délai maximal des appels d'embedding, en secondes |
| `tock_gen_ai_orchestrator_compressor_provider_timeout` | `7` | Délai maximal des appels au compresseur de documents, en secondes |
| `tock_gen_ai_orchestrator_observability_provider_timeout` | `3` | Délai maximal des appels d'observabilité, en secondes |
| `tock_gen_ai_orchestrator_observability_provider_max_retries` | `0` | Nombre de nouvelles tentatives d'un appel d'observabilité en échec |
| `tock_gen_ai_orchestrator_db_pool_size` | `10` | Taille du pool de connexions PGVector |
| `tock_gen_ai_orchestrator_db_max_overflow` | `5` | Connexions supplémentaires autorisées au-delà de cette taille |
| `tock_gen_ai_orchestrator_db_pool_timeout` | `30` | Délai maximal pour obtenir une connexion, en secondes |
| `tock_gen_ai_orchestrator_db_pool_recycle` | `3600` | Durée de vie maximale d'une connexion, en secondes |
| `tock_gen_ai_orchestrator_vector_store_test_query` | `Any definition` | Requête utilisée pour tester la connexion à la base vectorielle |
| `tock_gen_ai_orchestrator_vector_store_test_max_docs_retrieved` | `4` | Nombre de documents récupérés par ce test |
| `tock_gcp_project_id` | | Projet GCP des secrets, avec le gestionnaire de secrets GCP |

## Autres modules

### Tests Xray

Propriétés du module `tock-bot-xray`, qui exécute les plans de test Xray d'un bot.

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_bot_test_url` | | URL de _Tock Studio_ |
| `tock_bot_test_login` | | Identifiant _Tock Studio_ |
| `tock_bot_test_password` | | Mot de passe _Tock Studio_ |
| `tock_bot_test_timeout_in_ms` | `3600000` | Délai maximal des appels à _Tock Studio_ |
| `tock_bot_test_botId` | | Identifiant du bot testé |
| `tock_bot_test_configuration_ids` | | Identifiants des configurations testées |
| `tock_bot_test_configuration_names` | | Noms des configurations testées |
| `tock_bot_test_locale` | valeur de `tock_default_locale` | Locale des tests |
| `tock_bot_test_start_sentence` | | Phrase envoyée au début de chaque test |
| `tock_bot_test_xray_login` | | Identifiant Xray |
| `tock_bot_test_xray_password` | | Mot de passe Xray |
| `tock_bot_test_xray_timeout_in_ms` | `60000` | Délai maximal des appels à Xray |
| `tock_bot_test_xray_test_plan_keys` | | Clés des plans de test à exécuter |
| `tock_bot_test_xray_test_keys` | | Clés des tests à exécuter |
| `tock_bot_test_xray_test_plan_env` | | Environnement indiqué dans les exécutions de test |
| `tock_bot_test_xray_test_plan_bot_url` | | URL du bot indiquée dans les exécutions de test |
| `tock_bot_test_jira_project` | | Projet Jira des tests |
| `tock_bot_test_jira_xray_test_type_field` | | Champ Jira du type de test |
| `tock_bot_test_jira_xray_manual_test_field` | | Champ Jira des étapes de test manuel |
| `tock_bot_test_jira_xray_automation_type_field` | | Champ Jira du type d'automatisation |
| `tock_bot_test_jira_linked_field` | | Champ Jira liant les tests |
| `tock_bot_test_jira_registered_bot_url` | `${botUrl}` | URL du bot enregistrée dans Jira |
| `tock_bot_test_connector_jira_map` | | Composants Jira par connecteur |
| `tock_bot_test_precondition_key_text_chat` | | Précondition Xray des tests texte |
| `tock_bot_test_precondition_key_voice_assistant` | | Précondition Xray des tests vocaux |
| `tock_bot_test_precondition_key_text_and_voice_assistant` | | Précondition Xray des tests texte et vocaux |
| `tock_bot_xray_creation_keyword` | `_xray_` | Mot-clé créant un test Xray à partir du dialogue en cours |
| `tock_bot_xray_update_keyword` | `_xray_update_` | Mot-clé mettant à jour un test Xray à partir du dialogue en cours |

### Dialogflow

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `dialogflow_project_id` | | Projet Google Cloud de l'agent Dialogflow à importer |
