/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.engine.nlp

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.TockConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.DialogState
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.genai.orchestratorclient.requests.CompletionRequest
import ai.tock.genai.orchestratorclient.requests.Formatter
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorclient.services.CompletionService
import ai.tock.genai.orchestratorcore.models.llm.AzureOpenAILLMSetting
import ai.tock.nlp.api.client.NlpClient
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.NlpEntityValue
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpQueryContext
import ai.tock.nlp.api.client.model.NlpQueryState
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import ai.tock.nlp.api.client.model.dump.IntentDefinition
import ai.tock.nlp.api.client.model.dump.SentencesDump
import ai.tock.nlp.api.client.model.evaluation.EntityEvaluationQuery
import ai.tock.nlp.api.client.model.evaluation.EntityToEvaluate
import ai.tock.nlp.api.client.model.merge.ValueToMerge
import ai.tock.nlp.api.client.model.merge.ValuesMergeQuery
import ai.tock.nlp.api.client.model.monitoring.MarkAsUnknownQuery
import ai.tock.nlp.front.service.ApplicationConfigurationService
import ai.tock.nlp.front.service.applicationDAO
import ai.tock.nlp.front.service.entityTypeDAO
import ai.tock.nlp.front.service.faqDefinitionDAO
import ai.tock.nlp.front.service.intentDAO
import ai.tock.nlp.front.service.namespaceConfigurationDAO
import ai.tock.nlp.front.service.sentenceDAO
import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.service.storage.NamespaceConfigurationDAO
import ai.tock.nlp.front.service.storage.UserNamespaceDAO
import ai.tock.nlp.front.service.userNamespaceDAO
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.config.ValidatedSentence
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.shared.Executor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.defaultZoneId
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.shared.security.key.RawSecretKey
import ai.tock.shared.security.key.SecretKey
import ai.tock.shared.withNamespace
import mu.KotlinLogging
import java.io.InputStream
import java.time.ZonedDateTime
import java.util.Locale

/**
 * [NlpController] default implementation.
 */
@OptIn(ExperimentalTockCoroutines::class)
internal class Nlp :
    NlpController {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val nlpClient: NlpClient get() = injector.provide()
    private val executor: Executor get() = injector.provide()
    private val config = ApplicationConfigurationService
//    private val appConfiguration: ApplicationConfiguration get() = injector.provide()
    private val completionService: CompletionService get() = injector.provide()

    private class SentenceParser(
        val nlpClient: NlpClient,
        val sentence: SendSentence,
        val userTimeline: UserTimeline,
        val dialog: Dialog,
        val connector: TockConnectorController,
        val botDefinition: BotDefinition,
        val appConfiguration: ApplicationConfiguration,
        val completionService: CompletionService,
    ) {
        suspend fun parse() {
            logger.debug { "Parse sentence : $sentence" }

            findKeyword(sentence.stringText)?.apply {
                dialog.state.currentIntent = this
                return
            }

            toNlpQuery().let { query ->
                try {
                    val precomputedNlp = sentence.precomputedNlp
                    val result =
                        if (precomputedNlp == null) {
                            parse(query)
                        } else {
                            evaluateEntitiesForPrecomputedNlp(query, precomputedNlp)
                        }

                    result?.let { nlpResult ->

                        listenNlpSuccessCall(query, nlpResult)
                        val intent = findIntent(userTimeline, dialog, sentence, nlpResult)

                        val customEntityEvaluations: MutableList<EntityValue> = mutableListOf()
                        BotRepository.forEachNlpListener {
                            customEntityEvaluations.addAll(
                                try {
                                    it.evaluateEntities(userTimeline, dialog, sentence, nlpResult)
                                } catch (e: Exception) {
                                    logger.error(e)
                                    emptyList()
                                },
                            )
                        }

                        val entityEvaluations =
                            customEntityEvaluations +
                                nlpResult.entities
                                    .asSequence()
                                    .filter { e -> customEntityEvaluations.none { it.entity == e.entity } }
                                    .map { EntityValue(nlpResult, it) }
                        sentence.state.entityValues.addAll(entityEvaluations)

                        dialog.apply {
                            state.currentIntent = intent

                            val finalEntityValues = state.mergeEntityValuesFromAction(sentence)

                            sentence.nlpStats =
                                NlpCallStats(
                                    userTimeline.userPreferences.locale,
                                    intent,
                                    entityEvaluations,
                                    finalEntityValues,
                                    query,
                                    nlpResult,
                                )
                        }
                    } ?: listenNlpErrorCall(query, dialog, null)
                } catch (t: Throwable) {
                    logger.error(t)
                    listenNlpErrorCall(query, dialog, t)
                }
            }
        }

        private suspend fun findIntent(
            userTimeline: UserTimeline,
            dialog: Dialog,
            sentence: SendSentence,
            nlpResult: NlpResult,
        ): Intent {
            var i: Intent? = null
            BotRepository.forEachNlpListener {
                if (i == null) {
                    i =
                        try {
                            it.findIntent(userTimeline, dialog, sentence, nlpResult)?.wrappedIntent()
                        } catch (e: Exception) {
                            logger.error(e)
                            null
                        }
                }
            }

            return i ?: botDefinition.findIntent(nlpResult.intent, sentence.connectorId)
        }

        private fun evaluateEntitiesForPrecomputedNlp(
            nlpQuery: NlpQuery,
            nlpResult: NlpResult,
        ): NlpResult {
            fun NlpEntityValue.toEntityToEvaluate(): EntityToEvaluate =
                EntityToEvaluate(
                    start,
                    end,
                    entity,
                    subEntities.map { it.toEntityToEvaluate() },
                )

            return try {
                if (nlpResult.entities.isEmpty()) {
                    nlpResult
                } else {
                    val result =
                        nlpClient.evaluateEntities(
                            EntityEvaluationQuery(
                                nlpQuery.namespace,
                                nlpQuery.applicationName,
                                nlpQuery.context,
                                nlpResult.entities.map { it.toEntityToEvaluate() },
                                nlpResult.retainedQuery,
                            ),
                        )
                    if (result != null) {
                        nlpResult.copy(
                            entities =
                                result.values +
                                    nlpResult.entities.filter { e ->
                                        result.values.none { it.start == e.start }
                                    },
                        )
                    } else {
                        nlpResult
                    }
                }
            } catch (exception: Exception) {
                logger.error(exception)
                nlpResult
            }
        }

        private suspend fun findKeyword(sentence: String?): Intent? {
            return if (sentence != null) {
                var i: Intent? = null
                BotRepository.forEachNlpListener {
                    if (i == null) {
                        i =
                            try {
                                it.detectKeyword(sentence)
                            } catch (e: Exception) {
                                logger.error(e)
                                null
                            }
                    }
                }
                i
            } else {
                null
            }
        }

        private suspend fun listenNlpSuccessCall(
            query: NlpQuery,
            result: NlpResult,
        ) {
            BotRepository.forEachNlpListener {
                try {
                    it.onSuccess(query, result)
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }

        private suspend fun listenNlpErrorCall(
            query: NlpQuery,
            dialog: Dialog,
            throwable: Throwable?,
        ) {
            BotRepository.forEachNlpListener {
                try {
                    it.onError(query, dialog, throwable)
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }

        private fun toQueryContext(): NlpQueryContext {
            val test = userTimeline.userPreferences.test
            return NlpQueryContext(
                userTimeline.userPreferences.locale,
                sentence.playerId.id,
                dialog.id.toString(),
                connector.connectorType.toString(),
                referenceDate = dialog.state.nextActionState?.referenceDate ?: ZonedDateTime.now(defaultZoneId),
                referenceTimezone = dialog.state.nextActionState?.referenceTimezone ?: defaultZoneId,
                test = test,
                registerQuery = !test && !userTimeline.userState.botDisabled,
            )
        }

        private suspend fun toNlpQuery(): NlpQuery {
            return NlpQuery(
                listOf(sentence.stringText ?: ""),
                botDefinition.namespace,
                botDefinition.nlpModelName,
                toQueryContext(),
                NlpQueryState(
                    dialog.state.nextActionState?.states
                        ?: listOfNotNull(dialog.currentStory?.definition?.mainIntent()?.name).toSet(),
                ),
                configuration = connector.botConfiguration.applicationId,
            ).run {
                var query = this
                BotRepository.forEachNlpListener {
                    query = it.updateQuery(sentence, userTimeline, dialog, botDefinition, query)
                }
                query
            }
        }

        private fun mergeEntityValues(
            action: Action,
            newValues: List<EntityValue>,
            oldValue: EntityStateValue? = null,
        ): EntityStateValue {
            val entity = newValues.first().entity
            val defaultNewValue = newValues.firstOrNull { it.value != null } ?: newValues.first()
            val eligibleToMergeValues = newValues.filter { it.mergeSupport && it.value != null }
            return if (oldValue == null) {
                if (eligibleToMergeValues.size < 2) {
                    EntityStateValue(action, defaultNewValue)
                        .apply {
                            multiRequestedValues = newValues
                        }
                } else {
                    val result = mergeValues(entity, eligibleToMergeValues, defaultNewValue)
                    EntityStateValue(action, result)
                }
            } else {
                if (eligibleToMergeValues.isEmpty() ||
                    (eligibleToMergeValues.size == 1 && oldValue.value?.value == null)
                ) {
                    oldValue.changeValue(defaultNewValue, action)
                        .apply {
                            multiRequestedValues = newValues
                        }
                } else {
                    val result = mergeValues(entity, eligibleToMergeValues, defaultNewValue, oldValue)
                    oldValue.changeValue(result, action)
                }
            }
        }

        private fun mergeValues(
            entity: Entity,
            newValues: List<EntityValue>,
            defaultNewValue: EntityValue,
            initialValue: EntityStateValue? = null,
        ): EntityValue {
            val result =
                nlpClient.mergeValues(
                    ValuesMergeQuery(
                        botDefinition.namespace,
                        botDefinition.nlpModelName,
                        toQueryContext(),
                        entity,
                        newValues.map {
                            ValueToMerge(
                                it.value!!,
                                it.content,
                                false,
                                it.start,
                                it.probability,
                            )
                        } +
                            listOfNotNull(
                                initialValue
                                    ?.value
                                    ?.let { value ->
                                        value.value?.let {
                                            ValueToMerge(it, value.content, true)
                                        }
                                    },
                            ),
                    ),
                )
            return if (result?.value == null) {
                defaultNewValue
            } else {
                EntityValue(entity, result.value, result.content)
            }
        }

        private suspend fun DialogState.mergeEntityValuesFromAction(action: Action): List<EntityValue> {
            var merge: List<NlpEntityMergeContext> =
                action.state.entityValues
                    .asSequence()
                    .groupBy { it.entity.role }
                    .map { NlpEntityMergeContext(it.key, entityValues[it.key], it.value) }
            // sort entities
            BotRepository.forEachNlpListener { merge = it.sortEntitiesBeforeMerge(merge) }

            return merge.mapNotNull { mergeContext ->
                var context = mergeContext
                BotRepository.forEachNlpListener { context = it.mergeEntityValues(this, action, context) }
                val result = mergeEntityValues(action, context.newValues, context.initialValue)
                entityValues[context.entityRole] = result
                result.value
            }
        }

        private fun parse(request: NlpQuery): NlpResult? {
            logger.debug { "Sending sentence '${sentence.stringText}' to NLP" }
            val intentsQualifiers = dialog.state.nextActionState?.intentsQualifiers
            val useQualifiers = !intentsQualifiers.isNullOrEmpty()

            val app = appConfiguration.getApplicationByNamespaceAndName(request.namespace, request.applicationName)!!

            val resultMass =
                appConfiguration.search(
                    query =
                        SentencesQuery(
                            applicationId = app._id,
                            size = 1000,
                            status =
                                setOf(
                                    ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated,
                                    ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model,
                                ),
                        ),
                )

            val intents = appConfiguration.getIntentsByApplicationId(app._id)

            val intentById = intents.associateBy { it._id }

            val sentences: List<ValidatedSentence> =
                resultMass.sentences.mapNotNull { sentence ->
                    val intentName = intentById[sentence.classification.intentId]?.name
                    intentName?.let {
                        ValidatedSentence(
                            text = sentence.text,
                            intent = it,
                        )
                    }
                }

            // Get prompt
            val prompt =
                PromptTemplate(
                    formatter = Formatter.JINJA2.id,
                    template =
                        """
                        ## NLU déterministe

                        Tu es un **moteur NLU déterministe**.
                        Tu simules le fonctionnement d’un **modèle NLU classique basé sur la similarité sémantique**.

                        ### Objectif

                        Comparer une phrase utilisateur à un ensemble de **phrases validées**, chacune associée à une **intention existante**, et déterminer :

                        * l’intention la plus proche sémantiquement,
                        * les intentions secondaires possibles si un lien sémantique existe.

                        ### Fonctionnement attendu

                        * Tu compares la phrase utilisateur **uniquement** aux phrases validées fournies.
                        * Chaque phrase validée est associée à **une intention existante**.
                        * Tu choisis l’intention **la plus proche sémantiquement**.
                        * Tu **n’inventes jamais** de nouvelles intentions.
                        * Tu **n’utilises que** les intentions présentes dans les données fournies.
                        * Si aucun rapprochement sémantique pertinent n’existe, l’intention principale doit être **"unknown"**.

                        ### Contraintes STRICTES

                        * Ne jamais créer d’intention inexistante.
                        * Ne jamais reformuler ou enrichir une intention.
                        * Ne jamais expliquer ton raisonnement.
                        * La sortie doit être **STRICTEMENT du JSON valide**, sans aucun texte hors JSON.

                        ## Schéma de sortie STRICT (JSON uniquement)

                        ```json
                        {
                          "language": "",
                          "intent": "",
                          "similarity": "HIGH" | "STRONG" | "MEDIUM" | "LOW" | "AMBIGUOUS",
                          "score": 0,
                          "suggestions": [
                            {
                              "intent": "",
                              "similarity": "HIGH" | "STRONG" | "MEDIUM" | "LOW" | "AMBIGUOUS",
                              "score": 0
                            }
                          ]
                        }
                        ```

                        ## Définition des champs

                        ### `language`

                        * Détecte automatiquement la langue de la phrase utilisateur.

                        ### `intent`

                        * L’intention **la plus proche sémantiquement** parmi les intentions existantes.
                        * `"unknown"` si aucun rapprochement pertinent n’est trouvé.

                        ### `suggestions`

                        * Contient les **autres intentions possibles** ayant un lien sémantique réel.
                        * Maximum **5 éléments**.
                        * Ne jamais inclure l’intention principale.
                        * Liste vide si aucune autre intention pertinente n’existe.

                        ## Similarité sémantique (classification)

                        * **HIGH**
                          Sens quasi équivalent, reformulation directe possible sans perte de sens ni d’intention.

                        * **STRONG**
                          Intention identique et même objectif utilisateur, avec une **légère nuance**
                          (angle différent, précision supplémentaire, implicite vs explicite).

                        * **MEDIUM**
                          Thème commun, mais intention ou portée partiellement différente.

                        * **LOW**
                          Lien sémantique lointain mais existant.

                        * **AMBIGUOUS**
                          Sens très proche, mais **plusieurs intentions possibles**, rendant la décision incertaine.

                        ## Score déterministe (OBLIGATOIRE)

                        Le champ `score` est **strictement dérivé** du champ `similarity`.

                        Aucune autre valeur n’est autorisée.

                        | similarity  | score  |
                        |-------------|--------|
                        | LOW         | 0.0    |
                        | AMBIGUOUS   | 0.5    |
                        | MEDIUM      | 0.6    |
                        | STRONG      | 0.8    |
                        | HIGH        | 1.0    |

                        > Le score doit toujours être **mécaniquement recalculable** à partir de `similarity`.

                        ## Règles de décision

                        1. **Correspondance forte unique**

                            * Une phrase validée est très proche sémantiquement.
                            * → reprendre son intention
                            * → `similarity = HIGH` ou `STRONG`

                        2. **Correspondances multiples**

                            * Plusieurs intentions sont proches.
                            * → choisir la plus pertinente comme `intent`
                            * → placer les autres dans `suggestions` avec leur `similarity` et `score`.

                        3. **Absence de rapprochement pertinent**

                            * Aucun lien sémantique clair.
                            * → `intent = "unknown"`
                            * → `suggestions = []`
                            * → `similarity = LOW`
                            * → `score = 0.0`

                        ## Données fournies (mémoire NLU)

                        Phrases validées (intention + exemples) :
                        ```
                        {{examples}}
                        ```

                        ## Phrase utilisateur à analyser
                        ```
                        {{sentence}}
                        ```

                        """.trimIndent(),
                    inputs =
                        mapOf(
                            "sentence" to request.queries.first(),
                            "examples" to sentences,
                        ),
                )

            // call the completion service to generate sentences
            val parsedSentence = completionService
                .parseSentence(
                    CompletionRequest(
                        AzureOpenAILLMSetting<SecretKey>(
                            apiKey = RawSecretKey(secret = property("SPIKE_1700", "XXXX")),
                            temperature = "0.5",
                            apiBase = "https://apim-di01-rec-openai-swct.azure-api.net",
                            deploymentName = "cd-di01-tock-bot-gpt4o-rec-swct",
                            apiVersion = "2024-03-01-preview",
                            model = "gpt-4o",
                        ),
                        prompt,
                        null,
                    ),
                )

            val genAiResult =parsedSentence?.let {
                val language = Locale.forLanguageTag(it.language)

                NlpResult(
                    intent = it.intent,
                    intentNamespace = app.namespace,
                    language = language,
                    entities = emptyList(),
                    notRetainedEntities = emptyList(),
                    intentProbability = it.score,
                    entitiesProbability = 0.0,
                    retainedQuery = request.queries.first(),
                    otherIntentsProbabilities = it.suggestions.associate { o -> o.intent to o.score },
                    originalIntentsProbabilities = emptyMap(),
                )

            }


//            val result =
//                if (!useQualifiers) {
//                    nlpClient.parse(request)
//                } else {
//                    nlpClient.parse(
//                        request.copy(
//                            intentsSubset =
//                                intentsQualifiers.asSequence().map {
//                                    it.copy(
//                                        intent =
//                                            it.intent.withNamespace(
//                                                request.namespace,
//                                            ),
//                                    )
//                                }.toSet(),
//                        ),
//                    )
//                }
//            if (result != null && useQualifiers) {
//                // force intents qualifiers if unknown answer
//                if (intentsQualifiers.none { it.intent == result.intent }) {
//                    return result.copy(
//                        intent =
//                            intentsQualifiers.maxByOrNull { it.modifier }?.intent
//                                ?: intentsQualifiers.first().intent,
//                    ).also {
//                        logger.warn { "${result.intent} not in intents qualifier $intentsQualifiers - use $it" }
//                    }
//                }
//            }
//            return result

            return genAiResult
        }
    }

    override suspend fun parseSentence(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        connector: ConnectorController,
        botDefinition: BotDefinition,
    ) {
        BotRepository.forEachNlpListener {
            val result = it.precompute(sentence, userTimeline, dialog, botDefinition)
            if (result != null) {
                sentence.precomputedNlp = result
            }
        }

        SentenceParser(
            nlpClient,
            sentence,
            userTimeline,
            dialog,
            connector as TockConnectorController,
            botDefinition,
            config,
            completionService,
        ).parse()
    }

    override fun markAsUnknown(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        botDefinition: BotDefinition,
    ) {
        if (sentence.stringText != null) {
            executor.executeBlocking {
                nlpClient.markAsUnknown(
                    MarkAsUnknownQuery(
                        botDefinition.namespace,
                        botDefinition.nlpModelName,
                        userTimeline.userPreferences.locale,
                        sentence.stringText,
                    ),
                )
            }
        }
    }

    override fun getIntentsByNamespaceAndName(
        namespace: String,
        name: String,
    ): List<IntentDefinition> = nlpClient.getIntentsByNamespaceAndName(namespace, name) ?: emptyList()

    override fun importNlpDump(stream: InputStream): Boolean = nlpClient.importNlpDump(stream)

    override fun importNlpPlainDump(dump: ApplicationDump): Boolean = nlpClient.importNlpPlainDump(dump)

    override fun importNlpPlainSentencesDump(dump: SentencesDump): Boolean = nlpClient.importNlpPlainSentencesDump(dump)

    override fun importNlpSentencesDump(stream: InputStream): Boolean = nlpClient.importNlpSentencesDump(stream)

    override fun waitAvailability(timeToWaitInMs: Long) {
        val s = System.currentTimeMillis()
        while (!nlpClient.healthcheck() && System.currentTimeMillis() - s < timeToWaitInMs) {
        }
    }
}
