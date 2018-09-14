/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.engine.nlp

import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.TockConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.DialogState
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.EntityValue
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.nlp.api.client.NlpClient
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.api.client.model.NlpEntityValue
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpQueryContext
import fr.vsct.tock.nlp.api.client.model.NlpQueryState
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.api.client.model.dump.ApplicationDump
import fr.vsct.tock.nlp.api.client.model.dump.IntentDefinition
import fr.vsct.tock.nlp.api.client.model.dump.SentencesDump
import fr.vsct.tock.nlp.api.client.model.evaluation.EntityEvaluationQuery
import fr.vsct.tock.nlp.api.client.model.evaluation.EntityToEvaluate
import fr.vsct.tock.nlp.api.client.model.merge.ValueToMerge
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.api.client.model.monitoring.MarkAsUnknownQuery
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.defaultZoneId
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.withNamespace
import mu.KotlinLogging
import java.io.InputStream
import java.time.ZonedDateTime

/**
 * [NlpController] default implementation.
 */
internal class Nlp : NlpController {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val nlpClient: NlpClient get() = injector.provide()
    private val executor: Executor get() = injector.provide()

    private class SentenceParser(
        val nlpClient: NlpClient,
        val sentence: SendSentence,
        val userTimeline: UserTimeline,
        val dialog: Dialog,
        val connector: TockConnectorController,
        val botDefinition: BotDefinition
    ) {

        fun parse() {
            logger.debug { "Parse sentence : $sentence" }

            findKeyword(sentence.stringText)?.apply {
                dialog.state.currentIntent = this
                return
            }

            toNlpQuery().let { query ->
                try {
                    val result = if (sentence.precomputedNlp == null) {
                        parse(query)
                    } else {
                        evaluateEntitiesForPrecomputedNlp(query, sentence.precomputedNlp)
                    }

                    result?.let { nlpResult ->

                        listenNlpSuccessCall(query, nlpResult)
                        val intent = findIntent(userTimeline, dialog, sentence, nlpResult)

                        val customEntityEvaluations = BotRepository.nlpListeners.flatMap {
                            try {
                                it.evaluateEntities(userTimeline, dialog, sentence, nlpResult)
                            } catch (e: Exception) {
                                logger.error(e)
                                emptyList<EntityValue>()
                            }
                        }
                        val entityEvaluations = customEntityEvaluations +
                                nlpResult.entities
                                    .asSequence()
                                    .filter { e -> customEntityEvaluations.none { it.entity == e.entity } }
                                    .map { EntityValue(nlpResult, it) }
                        sentence.state.entityValues.addAll(entityEvaluations)

                        dialog.apply {
                            state.currentIntent = intent

                            val finalEntityValues = state.mergeEntityValuesFromAction(sentence)

                            sentence.nlpStats = NlpCallStats(
                                userTimeline.userPreferences.locale,
                                intent,
                                entityEvaluations,
                                finalEntityValues,
                                query,
                                nlpResult
                            )
                        }
                    } ?: listenNlpErrorCall(query, null)
                } catch (t: Throwable) {
                    logger.error(t)
                    listenNlpErrorCall(query, t)
                }
            }
        }

        private fun findIntent(
            userTimeline: UserTimeline,
            dialog: Dialog,
            sentence: SendSentence,
            nlpResult: NlpResult
        ): Intent {

            for (l in BotRepository.nlpListeners) {
                val i =
                    try {
                        l.findIntent(userTimeline, dialog, sentence, nlpResult)
                    } catch (e: Exception) {
                        logger.error(e)
                        null
                    }
                if (i != null) {
                    return i.wrappedIntent()
                }
            }

            return botDefinition.findIntent(nlpResult.intent)
        }

        private fun evaluateEntitiesForPrecomputedNlp(nlpQuery: NlpQuery, nlpResult: NlpResult): NlpResult {
            fun NlpEntityValue.toEntityToEvaluate(): EntityToEvaluate = EntityToEvaluate(
                start,
                end,
                entity,
                subEntities.map { it.toEntityToEvaluate() }
            )

            return try {
                if (nlpResult.entities.isEmpty()) {
                    nlpResult
                } else {
                    val result = nlpClient.evaluateEntities(
                        EntityEvaluationQuery(
                            nlpQuery.namespace,
                            nlpQuery.applicationName,
                            nlpQuery.context,
                            nlpResult.entities.map { it.toEntityToEvaluate() },
                            nlpResult.retainedQuery
                        )
                    )
                    if (result != null) {
                        nlpResult.copy(
                            entities = result.values
                                    + nlpResult.entities.filter { e ->
                                result.values.none { it.start == e.start }
                            })
                    } else {
                        nlpResult
                    }
                }
            } catch (exception: Exception) {
                logger.error(exception)
                nlpResult
            }
        }


        private fun findKeyword(sentence: String?): Intent? {
            if (sentence != null) {
                BotRepository.nlpListeners.forEach {
                    try {
                        it.handleKeyword(sentence)?.apply { return this }
                    } catch (e: Exception) {
                        logger.error(e)
                    }
                }
            }
            return null
        }

        private fun listenNlpSuccessCall(query: NlpQuery, result: NlpResult) {
            BotRepository.nlpListeners.forEach {
                try {
                    it.success(query, result)
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }

        private fun listenNlpErrorCall(query: NlpQuery, throwable: Throwable?) {
            BotRepository.nlpListeners.forEach {
                try {
                    it.error(query, throwable)
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
                registerQuery = !test && !userTimeline.userState.botDisabled
            )
        }

        private fun toNlpQuery(): NlpQuery {
            return NlpQuery(
                listOf(sentence.stringText ?: ""),
                botDefinition.namespace,
                botDefinition.nlpModelName,
                toQueryContext(),
                NlpQueryState(
                    dialog.state.nextActionState?.states
                            ?: listOfNotNull(dialog.currentStory?.definition?.mainIntent()?.name).toSet()
                )
            )
        }

        private fun mergeEntityValues(
            action: Action,
            newValues: List<EntityValue>,
            oldValue: EntityStateValue? = null
        ): EntityStateValue {
            val entity = newValues.first().entity
            val defaultNewValue = newValues.firstOrNull { it.value != null } ?: newValues.first()
            val eligibleToMergeValues = newValues.filter { it.mergeSupport && it.value != null }
            return if (oldValue == null) {
                if (eligibleToMergeValues.size < 2) {
                    EntityStateValue(action, defaultNewValue)
                } else {
                    val result = mergeValues(entity, eligibleToMergeValues, defaultNewValue)
                    EntityStateValue(action, result)
                }
            } else {
                if (eligibleToMergeValues.isEmpty()
                    || (eligibleToMergeValues.size == 1 && oldValue.value?.value == null)
                ) {
                    oldValue.changeValue(defaultNewValue, action)
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
            initialValue: EntityStateValue? = null
        ): EntityValue {
            val result = nlpClient.mergeValues(
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
                            it.probability
                        )
                    } + listOfNotNull(
                        initialValue
                            ?.value
                            ?.let { value ->
                                value.value?.let {
                                    ValueToMerge(it, value.content, true)
                                }
                            }
                    )
                )
            )
            return if (result?.value == null) {
                defaultNewValue
            } else {
                EntityValue(entity, result.value, result.content)
            }
        }

        private fun DialogState.mergeEntityValuesFromAction(action: Action): List<EntityValue> {
            var merge: List<NlpEntityMergeContext> = action.state.entityValues
                .asSequence()
                .groupBy { it.entity.role }
                .map { NlpEntityMergeContext(it.key, entityValues[it.key], it.value) }
            //sort entities
            BotRepository.nlpListeners.forEach { merge = it.sortEntitiesToMerge(merge) }

            return merge.mapNotNull { mergeContext ->
                var context = mergeContext
                BotRepository.nlpListeners.forEach { context = it.mergeEntityValues(this, action, context) }
                val result = mergeEntityValues(action, context.newValues, context.initialValue)
                entityValues[context.entityRole] = result
                result.value
            }
        }

        private fun parse(request: NlpQuery): NlpResult? {
            logger.debug { "Sending sentence '${sentence.stringText}' to NLP" }
            val intentsQualifiers = dialog.state.nextActionState?.intentsQualifiers
            val useQualifiers = intentsQualifiers != null && intentsQualifiers.isNotEmpty()
            val result = if (!useQualifiers) {
                nlpClient.parse(request)
            } else {
                nlpClient.parse(
                    request.copy(intentsSubset = intentsQualifiers!!.asSequence().map {
                        it.copy(
                            intent = it.intent.withNamespace(
                                request.namespace
                            )
                        )
                    }.toSet())
                )
            }
            if (result != null && useQualifiers) {
                //force intents qualifiers if unknown answer
                if (intentsQualifiers!!.none { it.intent == result.intent }) {
                    return result.copy(
                        intent = intentsQualifiers.maxBy { it.modifier }?.intent ?: intentsQualifiers.first().intent
                    ).also {
                        logger.warn { "${result.intent} not in intents qualifier $intentsQualifiers - use $it" }
                    }
                }
            }
            return result
        }

    }

    override fun parseSentence(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        dialog: Dialog,
        connector: ConnectorController,
        botDefinition: BotDefinition
    ) {
        SentenceParser(
            nlpClient,
            sentence,
            userTimeline,
            dialog,
            connector as TockConnectorController,
            botDefinition
        ).parse()
    }

    override fun markAsUnknown(
        sentence: SendSentence,
        userTimeline: UserTimeline,
        botDefinition: BotDefinition
    ) {
        if (sentence.stringText != null) {
            executor.executeBlocking {
                nlpClient.markAsUnknown(
                    MarkAsUnknownQuery(
                        botDefinition.namespace,
                        botDefinition.nlpModelName,
                        userTimeline.userPreferences.locale,
                        sentence.stringText
                    )
                )
            }
        }
    }

    override fun getIntentsByNamespaceAndName(namespace: String, name: String): List<IntentDefinition>? =
        nlpClient.getIntentsByNamespaceAndName(namespace, name) ?: emptyList()

    override fun importNlpDump(stream: InputStream): Boolean =
        nlpClient.importNlpDump(stream)

    override fun importNlpPlainDump(dump: ApplicationDump): Boolean =
        nlpClient.importNlpPlainDump(dump)

    override fun importNlpPlainSentencesDump(dump: SentencesDump): Boolean =
        nlpClient.importNlpPlainSentencesDump(dump)

    override fun importNlpSentencesDump(stream: InputStream): Boolean =
        nlpClient.importNlpSentencesDump(stream)

    override fun waitAvailability(timeToWaitInMs: Long) {
        val s = System.currentTimeMillis()
        while (!nlpClient.healthcheck() && System.currentTimeMillis() - s < timeToWaitInMs) {
        }
    }
}