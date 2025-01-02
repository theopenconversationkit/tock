# Les collections Tock

## tock_bot et tock_bot_test

| collection  |   description  | classe|
|---|---|---|
|action_nlp_stats  |  ? statistiques d'actions préenregistrées ? | NlpStatsCol|
|archived_entity_values  | ? valeur d'entités conservées ?  | ArchivedEntityValuesCol|
|bot   | Identité des bots  | BotConfiguration|
|bot_configuration  | Configuration des bots avec par exemple les configurations des connecteurs, les urls associées etc.  | BotApplicationConfiguration|
|connector_message  | Stocke par exemple les quickreplies/categories échangés depuis le connecteur  | ConnectorMessageCol|
|dialog   | Contient les informations générales de thread de dialogue avec le bot : les emetteurs/recepteurs, l'état de la conversation dans le thread, les stories déclenchées et leurs contenus avec les actions du bot et les actions utilisateurs. | DialogCol|
|dialog_snapshot  | Contient un snapshot des threads avec l'identifiant des stories et entities sauvegardées et déclenchés | SnapshotCol|
|dialog_text  | Contient les échanges textuels envoyés par les utilisateurs | DialogTextCol|
|feature   | Contient les configurations pour par exemple désactiver/activer un bot ou les features dans `Stories & Answers/Rules/Application Features`| Feature|
|flow_state  | Concerne les informations d'analytics : définit les différents états atteignable, le storyType (ex: builtin)  | DialogFlowStateCol|
|flow_transition  | Concerne les informations d'analytics : Définit les transitions entres les intentions | DialogFlowStateTransitionCol|
|flow_transition_stats   | Concerne les informations d'analytics : Liste les utterances pour pouvoir être comptabilisées  | DialogFlowStateTransitionStatCol|
|i18n_alternative_index  | Définit les alternatives dans les différentes langues pour un label i18n ? | I18nAlternativeIndex |
|i18n_label  | Definit les labels i18n pour les réponses du bot | I18nLabel|
|i18n_label_stat   | Statistiques d'utilisation des labels  | I18nLabelStat  |
|story_configuration  | Configuration des stories, le nom de l'intention, la classe type de handler, la définition des steps, les children  | StoryDefinitionConfiguration|
|story_configuration_history  | Historique de configuration des stories ?  | StoryDefinitionConfigurationHistoryCol|
|test_plan   | Plan de test, définit via une copie d'un Dialog et dispose du type du connecteur cible  | TestPlan|
|test_plan_execution | Statistiques de test plan exécutés : indique le nombre d'erreur, le temps d'exécution, le status (exemple COMPLETE) | TestPlanExecution|
|user_lock  | ? Gestion de l'état du lock utilisateur  | UserLock |
|user_timeline  | Informations utilisateur par rapport à `Analytics/users`  | UserTimelineCol|

## tock_front
Liée aux informations modifiables dans le front

| collection  | description  | classe | DTO|
|---|---|---|---|
|application_definition | Configuration de l'application : le nom, les langues, le type de nlp | ApplicationDefinition|
|classified_sentence | Informations d'une Sentence déclarée dans Tock et la classification NLP, son état (à review, unknown)  | ClassifiedSentenceCol|
|dictionary_data | Dictionnaire de données liées aux entités custom définies, peut contenir des valeurs pré-définies <br> exemple : d'export `{"namespace":"app","entityName":"test","values":[{"value":"donnéesA","labels":{"fr":["label1","label2"]}}],"onlyValues":false,"minDistance":0.5,"textSearch":false}` <br> Configurable dans `Language Understanding/Entities`| | DictionaryData|
|entity_test_error | ? Lié aux tests sur entités | | EntityTestError|
|entity_type_definition | Configuration des classes d'entités exemple : duckling | | EntityTypeDefinition ||
|intent_definition | Définition des intentions avec les informations tels que les entités, les sharedIntents, à quelle application elles sont liées | | IntentDefinition ||
|intent_test_error | ? Lié aux tests sur les intentions | | IntentTestError|
|model_build | ? Lié à `Trigger Build` options avancées dans `Settings/<Application>/Edit/Advanced Options| | ModelBuild|
|model_build_trigger | ? Lié à `Trigger Build` options avancées dans `Settings/<Application>/Edit/Advanced Options | | ModelBuildTrigger | |
|parse_request_log | Log de parsing de requête avec les informations spécifiques de classification NLP | | ParseRequestLog ||
|parse_request_log_intent_stats | Log de parsing de requête avec les informations spécifiques de classification NLP entre plusieurs intentions | | ParseRequestLogIntentStat ||
|parse_request_log_stats | Stats de requêtes par rapport au texte exprimant le nombre de fois qu'il a été appelé, l'intentProbability et l'entitiesProbability | | ParseRequestLogStat|
|test_build | ? build pour le botApi ? | | TestBuild|
|user_action_log | Logs d'actions utilisateur dans l'interface (exemple : màj d'une configuration, création/màj d'une intent)| | UserActionLog |
|user_namespace | Définition des différents utilisateurs de l'interface | | UserNamespace|

##tock_model
Probablement pour le téléchargement et l'export de données depuis Tock.

| collection  | description  | classe |
|---|---|---|
|fs_entity.chunks | Morceaux de données des entités | définit dans NlpEngineModelMongoDAO|
|fs_entity.files | Différents fichiers selon les bots pour les entités | définit dans NlpEngineModelMongoDAO|
|fs_intent.chunks | Morceaux de données des intentions | définit dans NlpEngineModelMongoDAO|
|fs_intent.files | Différents fichiers selon les bots pour les intentions | définit dans NlpEngineModelMongoDAO|
|nlp_application_configuration | Contient la configuration présente dans les options avancées dans `Settings/<Application>/Edit/Advanced Options` | NlpApplicationConfigurationCol|

