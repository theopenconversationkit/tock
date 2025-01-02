```mermaid
sequenceDiagram
    Note over NlpVerticle : override configure
    
    NlpVerticle->> ParserService: parse(query) <br> ParseQuery(queries=[Je veux 2 cookies au chocolat], namespace=app, applicationName=..., context=QueryContext(language=fr..)
    activate ParserService
    ParserService ->> ParserService: parse(query: ParseQuery, metadata: CallMetadata) : ParseResult
    deactivate ParserService
    
    activate ParserService

    Note over ParserService,IntentSelectorService : Vérification si on a bien une phrase classifié (intent et/ou entités reconnues)
    alt isValidClassifiedSentence
    ParserService ->> IntentSelectorService: isValidClassifiedSentence(data: ParserRequestData)

    Note over ParserService,NlpCoreService : Evaluation des entités
    activate NlpCoreService
    ParserService->> NlpCoreService: evaluateEntities()
    NlpCoreService->>NlpCoreService: evalutate
    
    deactivate NlpCoreService
    deactivate ParserService
    
    else
    activate NlpCoreService
    activate ParserService
    ParserService->> NlpCoreService: parse()

    Note over NlpCoreService,NlpClassifierService : Reconnaissance des entités et de l'intention par la NLP via le moteur de NLP (OpenNLP ou Rase)
    NlpCoreService->> NlpClassifierService: classifyIntents
    NlpClassifierService-->>NlpCoreService : IntentClassification
    Note over NlpCoreService,NlpClassifierService : Recupère le type de classifieur (via le provider ) pour l'entité (classifieur = modèle ou type de modèle NLP) et classifie l'entité
    NlpCoreService->> NlpClassifierService: classifyEntities(context: EntityCallContext,text: String)
    NlpClassifierService-->>NlpCoreService : List<EntityRecognition>
    
    Note over NlpCoreService : Parsing de l'intention et des entités qualifiées précédemment
    NlpCoreService->>NlpCoreService: parse

    Note over NlpCoreService : selection de l'intention et de sa probabilité
    NlpCoreService->>IntentSelector: selectIntent
    IntentSelector-->>NlpCoreService : Intent,probability

    Note over NlpCoreService : Evaluation et classification des entités en double Array[evaluatedEntities, notRetainedEntities]
    loop
        NlpCoreService->>NlpCoreService: classifiyAndEvaluate
        NlpCoreService->>NlpCoreService: evaluateEntities
        
        Note over NlpCoreService : Evaluation des entités reconnues
        NlpCoreService->>EntityCoreService: evaluateEntities

        Note over EntityCoreService : Récupère le type d'entity provider parmis ceux dispnobiles (Duckling, Dictionnary)
        EntityCoreService->>EntityCoreService: getEntityEvaluator
        NlpCoreService->>EntityCoreService: evaluate
        Note over EntityCoreService : Appelle le evaluate dans le parser spécifique (DucklingParser ou DictionnaryEntityTypeEvaluator) qui implémente EntityTypeEvaluator
        EntityCoreService->>EntityTypeEvaluator: evaluate
        EntityTypeEvaluator-->>EntityCoreService: EvaluationResult
    
        EntityCoreService-->>NlpCoreService: List<EntityRecognition>

            alt (mergeEntitytype && classifyEntityTypes) == true && classifiedEntityTypes > 0
            Note over NlpCoreService,EntityCoreService : Evaluation des différents types de contexte d'entités (EntityCallContextForIntent,EntityCallContextForEntity,EntityCallContextForSubEntities) selon les providers (Duckling ou autre)
            NlpCoreService->>EntityCoreService: classifyEntityTypes
            EntityCoreService -->>NlpCoreService: List<EntityTypeRecognition>

            Note over NlpCoreService : Vérification du support de la classification pour les providers détectés et enlèvement de doublons éventuels
            NlpCoreService->>NlpCoreService: classifyEntityTypesForIntent
            NlpCoreService->> EntityTypeClassifier : classifyEntities
            alt mergeEntityType == true
            
            Note over NlpCoreService : Gestion du merge des entités selon poids de reconnaissance
            NlpCoreService->> EntityMergeService: mergeEntityTypes
            EntityMergeService-->>NlpCoreService: List<EntityTypeRecognition>
            
            end

    end

    NlpCoreService-->>ParserService: ParsingResult

    end

        Note over ParserService,ParseRequestLogDao : Sauvegarde du Dao
    ParserService->> ParseRequestLogDao: save
    deactivate ParserService
    deactivate NlpCoreService

end
```