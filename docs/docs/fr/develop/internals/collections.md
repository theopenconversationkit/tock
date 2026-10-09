---
title: Collections MongoDB
---

# Les collections MongoDB

Tock stocke ses données dans quatre bases MongoDB, dont les noms sont définis par les propriétés `tock_*_mongo_db` (voir [Configuration](../../operate/configuration.md#mongodb)).

## tock_bot

Bots, dialogues, stories et réponses.

### Dialogues et utilisateurs

| Collection | Description | Classe |
|------------|-------------|-------|
| `dialog` | Informations générales des dialogues : participants, état de la conversation, stories déclenchées avec les actions du bot et de l'utilisateur | `DialogCol` |
| `dialog_snapshot` | Instantanés des dialogues, avec les stories et entités à chaque étape, utilisés par l'analytique | `SnapshotCol` |
| `dialog_text` | Textes envoyés par les utilisateurs, pour la recherche dans les dialogues | `DialogTextCol` |
| `action_nlp_stats` | Résultat NLP de chaque action utilisateur (intentions, entités, probabilités) | `NlpStatsCol` |
| `archived_entity_values` | Valeurs précédentes des entités d'un dialogue | `ArchivedEntityValuesCol` |
| `connector_message` | Messages spécifiques aux connecteurs des actions (quick replies, cartes...) | `ConnectorMessageCol` |
| `user_timeline` | Utilisateurs : profil, préférences, données mémorisées (_Analytics_ > _Users_) | `UserTimelineCol` |
| `client_id` | Regroupe les identifiants d'un même utilisateur sur plusieurs canaux | `ClientIdCol` |
| `user_lock` | Verrou d'un utilisateur pendant le traitement d'un de ses messages | `UserLock` |

### Bots, stories et réponses

| Collection | Description | Classe |
|------------|-------------|-------|
| `bot` | Identité des bots | `BotConfiguration` |
| `bot_configuration` | Configurations des bots : connecteurs, chemins, URL (_Settings_ > _Configurations_) | `BotApplicationConfiguration` |
| `story_configuration` | Stories configurées dans _Tock Studio_ : intentions, réponses, étapes, entités obligatoires | `StoryDefinitionConfiguration` |
| `story_configuration_history` | Historique des modifications des stories | `StoryDefinitionConfigurationHistoryCol` |
| `feature` | Fonctionnalités et règles des stories (_Stories & Answers_ > _Rules_) | `Feature` |
| `i18n_label` | Libellés des réponses, par locale, connecteur et interface | `I18nLabel` |
| `i18n_label_stat` | Statistiques d'utilisation des libellés | `I18nLabelStat` |
| `i18n_alternative_index` | Dernière alternative utilisée pour un libellé à plusieurs textes, pour la rotation des réponses | `I18nAlternativeIndex` |
| `orchestration` | État des orchestrations entre bots | `Orchestration` |

### Gen AI

| Collection | Description | Classe |
|------------|-------------|-------|
| `bot_rag_configuration` | Réglages du RAG (_Gen AI_ > _Rag settings_) | `BotRAGConfiguration` |
| `bot_business_rules_configuration` | Contexte du prompt du RAG (_Gen AI_ > _Rag prompt context_) | `BotBusinessRulesConfiguration` |
| `bot_vector_store_configuration` | Réglages de la base vectorielle (_Gen AI_ > _Vector DB settings_) | `BotVectorStoreConfiguration` |
| `bot_document_compressor_configuration` | Réglages du compresseur de documents | `BotDocumentCompressorConfiguration` |
| `bot_observability_configuration` | Réglages d'observabilité | `BotObservabilityConfiguration` |
| `bot_sentence_generation_configuration` | Réglages de la génération de phrases | `BotSentenceGenerationConfiguration` |

### Analytique, métriques et qualité

| Collection | Description | Classe |
|------------|-------------|-------|
| `flow_state` | États du flux de conversation (stories, intentions, étapes) | `DialogFlowStateCol` |
| `flow_transition` | Transitions entre ces états | `DialogFlowStateTransitionCol` |
| `flow_transition_stats` | Occurrences des transitions | `DialogFlowStateTransitionStatCol` |
| `flow_transition_stats_date`, `flow_transition_stats_dialog`, `flow_transition_stats_user` | Agrégats de ces occurrences par date, dialogue et utilisateur |  |
| `flow_configuration` | État de traitement des statistiques de flux | `DialogFlowConfiguration` |
| `indicator` | Indicateurs personnalisés (_Metrics_ > _Indicators_) | `Indicator` |
| `metric` | Valeurs de ces indicateurs (_Metrics_ > _Metrics_) | `Metric` |
| `bot_dashboard_metadata`, `bot_index_session_note`, `bot_history_event` | Données du _Dashboard_ : métadonnées, notes sur les sessions d'indexation, historique des événements |  |
| `evaluation`, `evaluation_sample` | Évaluations des réponses (_Answers Quality_ > _Evaluations_) |  |
| `dataset`, `dataset_run`, `dataset_run_question_result` | Jeux de données et leurs exécutions (_Answers Quality_ > _Datasets_) |  |
| `test_plan` | Plans de test, avec les dialogues à rejouer et le connecteur cible | `TestPlan` |
| `test_plan_execution` | Exécutions des plans de test : statut, erreurs, durée | `TestPlanExecution` |

### Connecteurs

| Collection | Description | Classe |
|------------|-------------|-------|
| `web_channel_event` | Messages en attente de livraison aux clients du connecteur web (SSE) | `ChannelEvent` |
| `whatsapp_payload` | _Payloads_ des boutons WhatsApp |  |

## tock_front

Applications NLP, intentions, entités et phrases.

| Collection | Description | Classe |
|------------|-------------|-------|
| `application_definition` | Applications NLP : nom, locales, moteur NLP | `ApplicationDefinition` |
| `intent_definition` | Intentions : entités, intentions partagées, applications | `IntentDefinition` |
| `entity_type_definition` | Types d'entités (personnalisés ou fournis, par exemple par Duckling) | `EntityTypeDefinition` |
| `dictionary_data` | Valeurs prédéfinies des entités (_Language Understanding_ > _Entities_) | `DictionaryData` |
| `classified_sentence` | Phrases et leur qualification : intention, entités, statut (à qualifier, validée...) | `ClassifiedSentenceCol` |
| `faq_definition` | FAQ (_Stories & Answers_ > _FAQs stories_) | `FaqDefinition` |
| `faq_settings` | Paramètres des FAQ (question de satisfaction) | `FaqSettings` |
| `model_build_trigger` | Constructions de modèles demandées, en attente du `build_worker` | `ModelBuildTrigger` |
| `model_build` | Historique des constructions de modèles (_Model Quality_ > _Model Builds_) | `ModelBuild` |
| `test_build` | Résultats des tests de modèles (_Model Quality_ > _Test Trends_) | `TestBuild` |
| `intent_test_error` | Erreurs d'intentions des tests de modèles (_Model Quality_ > _Test Intent Errors_) | `IntentTestError` |
| `entity_test_error` | Erreurs d'entités des tests de modèles (_Model Quality_ > _Test Entity Errors_) | `EntityTestError` |
| `parse_request_log` | Journal des requêtes NLP, avec leur résultat | `ParseRequestLog` |
| `parse_request_log_stats` | Statistiques des requêtes NLP par texte : nombre d'appels, probabilités | `ParseRequestLogStat` |
| `parse_request_log_intent_stats` | Statistiques des requêtes NLP par paire d'intentions | `ParseRequestLogIntentStat` |
| `user_action_log` | Actions des utilisateurs de _Tock Studio_ (mise à jour de configuration, création d'intention...) | `UserActionLog` |
| `user_namespace` | Namespaces des utilisateurs de _Tock Studio_ | `UserNamespace` |
| `namespace_configuration` | Configuration des namespaces | `NamespaceConfiguration` |

## tock_model

Modèles NLP construits par le `build_worker`.

| Collection | Description | Classe |
|------------|-------------|-------|
| `fs_intent.files`, `fs_intent.chunks` | Modèles d'intentions, stockés avec GridFS |  |
| `fs_entity.files`, `fs_entity.chunks` | Modèles d'entités, stockés avec GridFS |  |
| `nlp_application_configuration` | Configuration du moteur NLP des applications (_Settings_ > _Applications_ > _Advanced options_) | `NlpApplicationConfigurationCol` |

## tock_cache

Cache partagé par les composants (collection `cache`).

