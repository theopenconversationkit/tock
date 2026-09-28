---
title: MongoDB collections
---

# MongoDB collections

Tock stores its data in four MongoDB databases, whose names are set by the `tock_*_mongo_db` properties (see [Configuration](../../operate/configuration.md#mongodb)).

## tock_bot

Bots, dialogs, stories and answers.

### Dialogs and users

| Collection | Description | Class |
|------------|-------------|-------|
| `dialog` | General information of the dialogs: participants, state of the conversation, stories triggered with the actions of the bot and of the user | `DialogCol` |
| `dialog_snapshot` | Snapshots of the dialogs, with the stories and entities at each step, used by the analytics | `SnapshotCol` |
| `dialog_text` | Texts sent by the users, for the search in the dialogs | `DialogTextCol` |
| `action_nlp_stats` | NLP result of each user action (intents, entities, probabilities) | `NlpStatsCol` |
| `archived_entity_values` | Previous values of the entities of a dialog | `ArchivedEntityValuesCol` |
| `connector_message` | Connector-specific messages of the actions (quick replies, cards...) | `ConnectorMessageCol` |
| `user_timeline` | Users: profile, preferences, memorized data (_Analytics_ > _Users_) | `UserTimelineCol` |
| `client_id` | Groups the identifiers of the same user on several channels | `ClientIdCol` |
| `user_lock` | Lock of a user while one of its messages is processed | `UserLock` |

### Bots, stories and answers

| Collection | Description | Class |
|------------|-------------|-------|
| `bot` | Identity of the bots | `BotConfiguration` |
| `bot_configuration` | Configurations of the bots: connectors, paths, URLs (_Settings_ > _Configurations_) | `BotApplicationConfiguration` |
| `story_configuration` | Stories configured in _Tock Studio_: intents, answers, steps, mandatory entities | `StoryDefinitionConfiguration` |
| `story_configuration_history` | History of the modifications of the stories | `StoryDefinitionConfigurationHistoryCol` |
| `feature` | Features and story rules (_Stories & Answers_ > _Rules_) | `Feature` |
| `i18n_label` | Labels of the answers, by locale, connector and interface | `I18nLabel` |
| `i18n_label_stat` | Usage statistics of the labels | `I18nLabelStat` |
| `i18n_alternative_index` | Alternative last used for a label with several texts, for the rotation of the answers | `I18nAlternativeIndex` |
| `orchestration` | State of the orchestrations between bots | `Orchestration` |

### Gen AI

| Collection | Description | Class |
|------------|-------------|-------|
| `bot_rag_configuration` | RAG settings (_Gen AI_ > _Rag settings_) | `BotRAGConfiguration` |
| `bot_business_rules_configuration` | Prompt context of the RAG (_Gen AI_ > _Rag prompt context_) | `BotBusinessRulesConfiguration` |
| `bot_vector_store_configuration` | Vector store settings (_Gen AI_ > _Vector DB settings_) | `BotVectorStoreConfiguration` |
| `bot_document_compressor_configuration` | Document compressor settings | `BotDocumentCompressorConfiguration` |
| `bot_observability_configuration` | Observability settings | `BotObservabilityConfiguration` |
| `bot_sentence_generation_configuration` | Sentence generation settings | `BotSentenceGenerationConfiguration` |

### Analytics, metrics and quality

| Collection | Description | Class |
|------------|-------------|-------|
| `flow_state` | States of the conversation flow (stories, intents, steps) | `DialogFlowStateCol` |
| `flow_transition` | Transitions between these states | `DialogFlowStateTransitionCol` |
| `flow_transition_stats` | Occurrences of the transitions | `DialogFlowStateTransitionStatCol` |
| `flow_transition_stats_date`, `flow_transition_stats_dialog`, `flow_transition_stats_user` | Aggregates of these occurrences by date, dialog and user |  |
| `flow_configuration` | Processing state of the flow statistics | `DialogFlowConfiguration` |
| `indicator` | Custom indicators (_Metrics_ > _Indicators_) | `Indicator` |
| `metric` | Values of these indicators (_Metrics_ > _Metrics_) | `Metric` |
| `bot_dashboard_metadata`, `bot_index_session_note`, `bot_history_event` | Data of the _Dashboard_: metadata, notes on the indexing sessions, history of the events |  |
| `evaluation`, `evaluation_sample` | Evaluations of the answers (_Answers Quality_ > _Evaluations_) |  |
| `dataset`, `dataset_run`, `dataset_run_question_result` | Datasets and their runs (_Answers Quality_ > _Datasets_) |  |
| `test_plan` | Test plans, with the dialogs to replay and the target connector | `TestPlan` |
| `test_plan_execution` | Executions of the test plans: status, errors, duration | `TestPlanExecution` |

### Connectors

| Collection | Description | Class |
|------------|-------------|-------|
| `web_channel_event` | Messages waiting to be delivered to the web connector clients (SSE) | `ChannelEvent` |
| `whatsapp_payload` | Payloads of the WhatsApp buttons |  |

## tock_front

NLP applications, intents, entities and sentences.

| Collection | Description | Class |
|------------|-------------|-------|
| `application_definition` | NLP applications: name, locales, NLP engine | `ApplicationDefinition` |
| `intent_definition` | Intents: entities, shared intents, applications | `IntentDefinition` |
| `entity_type_definition` | Entity types (custom or provided, for instance by Duckling) | `EntityTypeDefinition` |
| `dictionary_data` | Predefined values of the entities (_Language Understanding_ > _Entities_) | `DictionaryData` |
| `classified_sentence` | Sentences and their qualification: intent, entities, status (inbox, validated...) | `ClassifiedSentenceCol` |
| `faq_definition` | FAQs (_Stories & Answers_ > _FAQs stories_) | `FaqDefinition` |
| `faq_settings` | FAQ parameters (satisfaction question) | `FaqSettings` |
| `model_build_trigger` | Model builds requested, waiting for the `build_worker` | `ModelBuildTrigger` |
| `model_build` | History of the model builds (_Model Quality_ > _Model Builds_) | `ModelBuild` |
| `test_build` | Results of the model tests (_Model Quality_ > _Test Trends_) | `TestBuild` |
| `intent_test_error` | Intent errors of the model tests (_Model Quality_ > _Test Intent Errors_) | `IntentTestError` |
| `entity_test_error` | Entity errors of the model tests (_Model Quality_ > _Test Entity Errors_) | `EntityTestError` |
| `parse_request_log` | Log of the NLP requests, with their result | `ParseRequestLog` |
| `parse_request_log_stats` | Statistics of the NLP requests by text: number of calls, probabilities | `ParseRequestLogStat` |
| `parse_request_log_intent_stats` | Statistics of the NLP requests by pair of intents | `ParseRequestLogIntentStat` |
| `user_action_log` | Actions of the users of _Tock Studio_ (configuration update, intent creation...) | `UserActionLog` |
| `user_namespace` | Namespaces of the users of _Tock Studio_ | `UserNamespace` |
| `namespace_configuration` | Configuration of the namespaces | `NamespaceConfiguration` |

## tock_model

NLP models built by the `build_worker`.

| Collection | Description | Class |
|------------|-------------|-------|
| `fs_intent.files`, `fs_intent.chunks` | Intent models, stored with GridFS |  |
| `fs_entity.files`, `fs_entity.chunks` | Entity models, stored with GridFS |  |
| `nlp_application_configuration` | NLP engine configuration of the applications (_Settings_ > _Applications_ > _Advanced options_) | `NlpApplicationConfigurationCol` |

## tock_cache

Cache shared by the components (`cache` collection).

