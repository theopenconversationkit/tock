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

package ai.tock.nlp.dialogflow

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.nlp.NlpCallStats
import ai.tock.bot.engine.nlp.NlpController
import ai.tock.bot.engine.nlp.engine.nlp.AsyncNlpListener
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.NlpClient
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpQueryContext
import ai.tock.nlp.api.client.model.NlpQueryState
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import ai.tock.nlp.api.client.model.dump.IntentDefinition
import ai.tock.nlp.api.client.model.dump.SentencesDump
import ai.tock.nlp.api.client.model.monitoring.MarkAsUnknownQuery
import ai.tock.shared.Executor
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.defaultZoneId
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.withNamespace
import java.io.InputStream
import java.time.ZonedDateTime
import mu.KotlinLogging

/**
 * [NlpController] Dialogflow implementation.
 */
internal class DialogflowNlp : NlpController {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val nlpClient: NlpClient get() = injector.provide()
    private val executor: Executor get() = injector.provide()

    @OptIn(ExperimentalTockCoroutines::class)
    private class SentenceParser(
        val nlpClient: NlpClient,
        val sentence: SendSentence,
        val userTimeline: UserTimeline,
        val dialog: Dialog,
        val connector: ConnectorController,
        val botDefinition: BotDefinition
    ) {

        suspend fun parse() {
            logger.debug { "Parse sentence : $sentence" }

            findKeyword(sentence.stringText)?.apply {
                dialog.state.currentIntent = this
                return
            }

            toNlpQuery().let { query ->
                try {
                    val result = sentence.precomputedNlp ?: parse(query)

                    result?.let { nlpResult ->

                        listenNlpSuccessCall(query, nlpResult)
                        val intent = findIntent(userTimeline, dialog, sentence, nlpResult)

                        val customEntityEvaluations: MutableList<EntityValue> = mutableListOf()
                        BotRepository.forEachNlpListener {
                            customEntityEvaluations.addAll(
                                try {
                                    if (it is AsyncNlpListener) {
                                        it.processEntities(userTimeline, dialog, sentence, nlpResult)
                                    } else {
                                        it.evaluateEntities(userTimeline, dialog, sentence, nlpResult)
                                    }
                                } catch (e: Exception) {
                                    logger.error(e)
                                    emptyList<EntityValue>()
                                }
                            )
                        }

                        val entityEvaluations = customEntityEvaluations +
                            nlpResult.entities
                                .asSequence()
                                .filter { e -> customEntityEvaluations.none { it.entity == e.entity } }
                                .map { EntityValue(nlpResult, it) }
                        sentence.state.entityValues.addAll(entityEvaluations)

                        dialog.apply {
                            state.currentIntent = intent
                            nlpResult.staticResponse?.let {
                                // Add the static response from Dialogflow to the state context as a message
                                // Thus, this message can be used later in a story
                                state.setContextValue("message", nlpResult.staticResponse)
                            }

                            entityEvaluations
                                .map {
                                    state.entityValues[it.entity.role] = EntityStateValue(sentence, it)
                                }

                            sentence.nlpStats = NlpCallStats(
                                userTimeline.userPreferences.locale,
                                intent,
                                entityEvaluations,
                                entityEvaluations,
                                query,
                                nlpResult
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
            nlpResult: NlpResult
        ): Intent {
            var i: Intent? = null
            BotRepository.forEachNlpListener {
                if (i == null) {
                    i = try {
                        if (it is AsyncNlpListener) {
                            it.selectIntent(userTimeline, dialog, sentence, nlpResult)
                        } else {
                            it.findIntent(userTimeline, dialog, sentence, nlpResult)
                        }?.wrappedIntent()
                    } catch (e: Exception) {
                        logger.error(e)
                        null
                    }
                }
            }

            return i ?: botDefinition.findIntent(nlpResult.intent, sentence.applicationId)
        }

        private suspend fun findKeyword(sentence: String?): Intent? {
            return if (sentence != null) {
                var i: Intent? = null
                BotRepository.forEachNlpListener {
                    if (i == null) {
                        i = try {
                            if (it is AsyncNlpListener) {
                                it.detectKeyword(sentence)
                            } else {
                                it.handleKeyword(sentence)
                            }
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

        private suspend fun listenNlpSuccessCall(query: NlpQuery, result: NlpResult) {
            BotRepository.forEachNlpListener {
                try {
                    if (it is AsyncNlpListener) {
                        it.onSuccess(query, result)
                    } else {
                        it.success(query, result)
                    }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }

        private suspend fun listenNlpErrorCall(query: NlpQuery, dialog: Dialog, throwable: Throwable?) {
            BotRepository.forEachNlpListener {
                try {
                    if (it is AsyncNlpListener) {
                        it.onError(query, dialog, throwable)
                    } else {
                        it.error(query, dialog, throwable)
                    }
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

        private fun parse(request: NlpQuery): NlpResult? {
            logger.debug { "Sending sentence '${sentence.stringText}' to NLP" }
            val intentsQualifiers = dialog.state.nextActionState?.intentsQualifiers
            val useQualifiers = intentsQualifiers != null && intentsQualifiers.isNotEmpty()
            val result = if (!useQualifiers) {
                nlpClient.parse(request)
            } else {
                nlpClient.parse(
                    request.copy(
                        intentsSubset = intentsQualifiers.asSequence().map {
                            it.copy(
                                intent = it.intent.withNamespace(
                                    request.namespace
                                )
                            )
                        }.toSet()
                    )
                )
            }
            if (result != null && useQualifiers) {
                // force intents qualifiers if unknown answer
                if (intentsQualifiers.none { it.intent == result.intent }) {
                    return result.copy(
                        intent = intentsQualifiers.maxByOrNull { it.modifier }?.intent
                            ?: intentsQualifiers.first().intent
                    ).also {
                        logger.warn { "${result.intent} not in intents qualifier $intentsQualifiers - use $it" }
                    }
                }
            }
            return result
        }
    }

    override suspend fun parseSentence(
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
            connector,
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
                        sentence.stringText!!
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
