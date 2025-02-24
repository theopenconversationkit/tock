# The Tock collections

## tock_bot and tock_bot_test

| collection | description | class|
|---|---|---|
|action_nlp_stats | ? statistics of pre-recorded actions? | NlpStatsCol|
|archived_entity_values ​​| ? value of preserved entities? | ArchivedEntityValuesCol|
|bot | Identity of bots | BotConfiguration|
|bot_configuration | Configuration of bots with for example the configurations of connectors, the associated urls etc. | BotApplicationConfiguration|
|connector_message | Stores for example the quickreplies/categories exchanged from the connector | ConnectorMessageCol|
|dialog | Contains the general information of the dialog thread with the bot: the transmitters/receivers, the state of the conversation in the thread, the triggered stories and their contents with the actions of the bot and the user actions. | DialogCol|
|dialog_snapshot | Contains a snapshot of the threads with the ID of the stories and entities saved and triggered | SnapshotCol|
|dialog_text | Contains the textual exchanges sent by the users | DialogTextCol|
|feature | Contains the configurations to for example disable/enable a bot or the features in `Stories & Answers/Rules/Application Features`| Feature|
|flow_state | Concerns the analytics information: defines the different states that can be reached, the storyType (ex: builtin) | DialogFlowStateCol|
|flow_transition | Concerns the analytics information: Defines the transitions between the intentions | DialogFlowStateTransitionCol|
|flow_transition_stats | Concerns the analytics information: Lists the utterances to be counted | DialogFlowStateTransitionStatCol|
|i18n_alternative_index | Defines alternatives in different languages ​​for an i18n label? | I18nAlternativeIndex |
|i18n_label | Defines i18n labels for bot responses | I18nLabel|
|i18n_label_stat | Label usage statistics | I18nLabelStat |
|story_configuration | Story configuration, intent name, handler type class, step definition, children | StoryDefinitionConfiguration|
|story_configuration_history | Story configuration history? | StoryDefinitionConfigurationHistoryCol|
|test_plan | Test plan, defined via a copy of a Dialog and has the target connector type | TestPlan|
|test_plan_execution | Test plan statistics executed: indicates the number of errors, execution time, status (example COMPLETE) | TestPlanExecution|
|user_lock | ? User lock status management | UserLock |
|user_timeline | User information relative to `Analytics/users` | UserTimelineCol|

## tock_front
Related to editable information in the front

| collection | description | class | DTO|
|---|---|---|---|
|application_definition | Application configuration: name, languages, nlp type | ApplicationDefinition|
|classified_sentence | Information of a Sentence declared in Tock and the NLP classification, its status (to review, unknown) | ClassifiedSentenceCol|
|dictionary_data | Dictionary of data related to custom defined entities, can contain predefined values ​​<br> example: export `{"namespace":"app","entityName":"test","values":[{"value":"donnéesA","labels":{"fr":["label1","label2"]}}],"onlyValues":false,"minDistance":0.5,"textSearch":false}` <br> Configurable in `Language Understanding/Entities`| | DictionaryData|
|entity_test_error | ? Related to entity testing | | EntityTestError|
|entity_type_definition | Entity class configuration example: duckling | | EntityTypeDefinition ||
|intent_definition | Definition of intents with information such as entities, sharedIntents, which application they are related to | | IntentDefinition ||
|intent_test_error | ? Related to intent testing | | IntentTestError|
|model_build | ? Related to `Trigger Build` advanced options in `Settings/<Application>/Edit/Advanced Options| | ModelBuild|
|model_build_trigger | ? Related to `Trigger Build` advanced options in `Settings/<Application>/Edit/Advanced Options | | ModelBuildTrigger | |
|parse_request_log | Query parsing log with NLP classification specific information | | ParseRequestLog ||
|parse_request_log_intent_stats | Query parsing log with NLP classification specific information between multiple intents | | ParseRequestLogIntentStat ||
|parse_request_log_stats | Query stats against text expressing the number of times it was called, the intentProbability and the entitiesProbability | | ParseRequestLogStat|
|test_build | ? build for the botApi ? | | TestBuild|
|user_action_log | Logs of user actions in the interface (example: update of a configuration, creation/update of an intent)| | UserActionLog |
|user_namespace | Definition of the different users of the interface | | UserNamespace|

##tock_model

Probably for downloading and exporting data from Tock.

| collection | description | class |
|---|---|---|
|fs_entity.chunks | Chunks of entity data | defined in NlpEngineModelMongoDAO|
|fs_entity.files | Different files for different bots for entities | defined in NlpEngineModelMongoDAO|
|fs_intent.chunks | Chunks of intent data | defined in NlpEngineModelMongoDAO|
|fs_intent.files | Different files for different bots for intents | defined in NlpEngineModelMongoDAO|
|nlp_application_configuration | Contains the configuration present in the advanced options in `Settings/<Application>/Edit/Advanced Options` | NlpApplicationConfigurationCol|
