---
title: NLP evaluation
description: "How the Tock NLP API analyzes a sentence: intent detection, entity evaluation and merge of the results."
---

# NLP evaluation

This sequence diagram shows how the NLP API (`nlp_api`) analyzes a sentence: detection of the intent by the NLP
engine, then evaluation of the entities by the entity providers (Duckling, dictionaries) and merge of the results.
See [NLP and entity models](models-and-entity-models.md) for the classifiers and providers involved.

```mermaid
sequenceDiagram
    Note over NlpVerticle : override configure
    
    NlpVerticle->> ParserService: parse(query) <br> ParseQuery(queries=[I want 2 chocolate cookies], namespace=app, applicationName=..., context=QueryContext(language=en..)
    activate ParserService
    ParserService ->> ParserService: parse(query: ParseQuery, metadata: CallMetadata) : ParseResult
    Note over ParserService : formats the query (tabulations), keeps the first one
    ParserService ->> ParserService: formatQuery(query: string, metadata: CallMetadata) : ParseResult
    Note over ParserService,ApplicationConfiguration : checks whether the sentence is already validated
    ParserService ->> ApplicationConfiguration:  
    ApplicationConfiguration -->> ParserService:  validatedSentences
    Note over ParserService,ConfigurationRepository : gets the intent definitions of the application
    ParserService ->> ConfigurationRepository: 
    ConfigurationRepository -->> ParserService: intents
    deactivate ParserService
    
    activate ParserService
    Note over ParserService,IntentSelectorService : checks whether the sentence is already classified (intent and/or entities)
    alt isValidClassifiedSentence
    ParserService ->> IntentSelectorService: isValidClassifiedSentence(data: ParserRequestData)

    Note over ParserService,NlpCoreService : entity evaluation
    activate NlpCoreService
    ParserService->> NlpCoreService: evaluateEntities()
    NlpCoreService->>NlpCoreService: evaluate
    
    deactivate NlpCoreService
    deactivate ParserService
    
    else
    activate NlpCoreService
    activate ParserService
    ParserService->> NlpCoreService: parse()
    Note over NlpCoreService : truncates the text to a maximum number of characters (50000)
    NlpCoreService->>NlpCoreService: prepareText

    Note over NlpCoreService,NlpClassifierService : intent and entity recognition by the NLP engine (OpenNLP or other)
    Note over NlpCoreService,NlpClassifierService : gets the intent classifier (model) and classifies the intent
    NlpClassifierService->> NlpEngineRepository: getIntentClassifier
    NlpClassifierService -->> NlpCoreService : IntentClassification

    NlpCoreService->> NlpClassifierService: classifyEntities(context: EntityCallContext,text: String)
    Note over NlpCoreService,NlpEngineRepository : gets the entity classifier (from the provider)
    NlpClassifierService->> NlpEngineRepository: getEntityClassifier
    NlpClassifierService -->> NlpCoreService : List<EntityRecognition>

    Note over NlpCoreService : parses the intent and the entities classified above
    NlpCoreService->>NlpCoreService: parse
    NlpCoreService->>NlpCoreService: parse internal

    Note over NlpCoreService : selects the intent and its probability
    NlpCoreService->>IntentSelector: selectIntent
    IntentSelector-->>NlpCoreService : Intent,probability

    Note over NlpCoreService : evaluates and classifies the entities into [evaluatedEntities, notRetainedEntities]
    loop
        NlpCoreService->>NlpCoreService: classifyAndEvaluate
        NlpCoreService->>NlpCoreService: evaluateEntities
        
        Note over NlpCoreService : evaluates the recognized entities
        NlpCoreService->>EntityCoreService: evaluateEntities

        Note over EntityCoreService : gets the entity provider among the available ones (Duckling, dictionary)
        EntityCoreService->>EntityCoreService: getEntityEvaluator
        NlpCoreService->>EntityCoreService: evaluate
        Note over EntityCoreService : calls the evaluate function of the specific parser (DucklingParser or DictionaryEntityTypeEvaluator)
        EntityCoreService->>EntityCoreService: evaluate
        EntityCoreService-->>EntityCoreService: EvaluationResult
    
        EntityCoreService-->>NlpCoreService: List<EntityRecognition>

            alt (mergeEntitytype && classifyEntityTypes) == true && classifiedEntityTypes > 0
            Note over NlpCoreService,EntityCoreService : evaluates the entity contexts (EntityCallContextForIntent, EntityCallContextForEntity, EntityCallContextForSubEntities) with the providers (Duckling or other)
            NlpCoreService->>EntityCoreService: classifyEntityTypes
            EntityCoreService -->>NlpCoreService: List<EntityTypeRecognition>

            Note over NlpCoreService : checks that the detected providers support the classification, removes duplicates
            NlpCoreService->>NlpCoreService: classifyEntityTypesForIntent

            Note over NlpCoreService : evaluates the recognized entities via Duckling
            NlpCoreService->>DucklingParser: classifyEntities
            activate DucklingParser
            DucklingParser->>DucklingParser : classifyforIntent
            DucklingParser-->>NlpCoreService: List<EntityTypeRecognition>
            deactivate DucklingParser

        end
        alt mergeEntityType == true

            Note over NlpCoreService : merges the entities according to their recognition weight
            NlpCoreService->> EntityMergeService: mergeEntityTypes
            EntityMergeService-->>NlpCoreService: List<EntityTypeRecognition>

        end

    end
    

    NlpCoreService-->>ParserService: ParsingResult

    Note over ParserService,ParseRequestLogDao : saves the request log
    ParserService->> ParseRequestLogDao: save
    deactivate ParserService
    deactivate NlpCoreService



    end
```
