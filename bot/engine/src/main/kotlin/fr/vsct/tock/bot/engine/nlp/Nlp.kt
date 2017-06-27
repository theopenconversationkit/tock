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
import fr.vsct.tock.bot.definition.IntentContext
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.ContextValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.State
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.nlp.api.client.NlpClient
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.api.client.model.QueryContext
import fr.vsct.tock.nlp.api.client.model.QueryState
import fr.vsct.tock.nlp.api.client.model.dump.ApplicationDump
import fr.vsct.tock.nlp.api.client.model.merge.ValueToMerge
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeResult
import fr.vsct.tock.shared.error
import mu.KotlinLogging
import java.io.InputStream
import java.time.ZonedDateTime

/**
 * [NlpController] default implementation.
 */
object Nlp : NlpController {

    private val logger = KotlinLogging.logger {}
    private val nlpClient = NlpClient()

    private class SentenceParser(val sentence: SendSentence,
                                 val userTimeline: UserTimeline,
                                 val dialog: Dialog,
                                 val connector: ConnectorController,
                                 val botDefinition: BotDefinition) {

        fun parse() {
            logger.debug { "Parse sentence : $sentence" }
            if (userTimeline.userState.waitingRawInput || sentence.text.isNullOrBlank()) {
                //do nothing
            } else {
                findKeyword(sentence.text)?.apply {
                    sentence.state.currentIntent = this
                    dialog.state.currentIntent = this
                    return
                }

                toNlpQuery().let { query ->
                    try {
                        logger.debug { "Sending sentence '${sentence.text}' to NLP" }
                        parse(query)
                                ?.let { nlpResult ->
                                    listenNlpSuccessCall(query, nlpResult)
                                    sentence.state.currentIntent = botDefinition.findIntentForBot(
                                            nlpResult.intent,
                                            IntentContext(userTimeline, dialog, sentence)
                                    )
                                    sentence.state.entityValues.addAll(nlpResult.entities.map { ContextValue(nlpResult.retainedQuery, it) })
                                    dialog.apply {
                                        state.currentIntent = sentence.state.currentIntent
                                        state.mergeEntityValuesFromAction(sentence)
                                    }
                                } ?: listenNlpErrorCall(query, null)
                    } catch(t: Throwable) {
                        logger.error(t)
                        listenNlpErrorCall(query, t)
                    }
                }
            }
        }

        private fun findKeyword(sentence: String?): Intent? {
            if (sentence != null) {
                BotRepository.nlpListeners.forEach {
                    try {
                        it.handleKeyword(sentence)?.apply { return this }
                    } catch(e: Exception) {
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
                } catch(e: Exception) {
                    logger.error(e)
                }
            }
        }

        private fun listenNlpErrorCall(query: NlpQuery, throwable: Throwable?) {
            BotRepository.nlpListeners.forEach {
                try {
                    it.error(query, throwable)
                } catch(e: Exception) {
                    logger.error(e)
                }
            }
        }

        private fun toQueryContext(): QueryContext {
            return QueryContext(
                    userTimeline.userPreferences.locale,
                    sentence.playerId.id,
                    dialog.id,
                    connector.connectorType.toString(),
                    referenceDate = ZonedDateTime.now(userTimeline.userPreferences.timezone),
                    referenceTimezone = userTimeline.userPreferences.timezone
            )
        }

        private fun toNlpQuery(): NlpQuery {
            return NlpQuery(
                    listOf(sentence.text ?: ""),
                    botDefinition.namespace,
                    botDefinition.nlpModelName,
                    toQueryContext(),
                    QueryState.noState)
        }

        private fun mergeEntityValues(action: Action, newValues: List<ContextValue>, oldValue: EntityStateValue? = null): EntityStateValue {
            val entity = newValues.first().entity
            val defaultNewValue = newValues.filter { it.value != null }.firstOrNull() ?: newValues.first()
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
                        || (eligibleToMergeValues.size == 1 && oldValue.value?.value == null)) {
                    oldValue.changeValue(defaultNewValue, action)
                } else {
                    val result = mergeValues(entity, eligibleToMergeValues, defaultNewValue, oldValue)
                    oldValue.changeValue(result, action)
                }
            }
        }

        private fun mergeValues(
                entity: Entity,
                newValues: List<ContextValue>,
                defaultNewValue: ContextValue,
                initialValue: EntityStateValue? = null): ContextValue {
            val result = mergeValues(
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
                                        it.probability)
                            } + listOfNotNull(
                                    if (initialValue == null) null
                                    else ValueToMerge(
                                            initialValue.value!!.value!!,
                                            initialValue.value!!.content,
                                            true)
                            )
                    )
            )
            return if (result == null || result.value == null) {
                defaultNewValue
            } else {
                ContextValue(entity, result.value, result.content)
            }
        }

        private fun State.mergeEntityValuesFromAction(action: Action) {
            entityValues.putAll(
                    action.state.entityValues
                            .groupBy { it.entity.role }
                            .mapValues {
                                mergeEntityValues(action, it.value, entityValues.get(it.key))
                            }
            )
        }

        private fun parse(request: NlpQuery): NlpResult? {
            val response = nlpClient.parse(request)
            val result = response.body()
            if (result == null) {
                logger.error { "nlp error : ${response.errorBody()?.string()}" }
            }
            return result
        }

        private fun mergeValues(request: ValuesMergeQuery): ValuesMergeResult? {
            val response = nlpClient.mergeValues(request)
            val result = response.body()
            if (result == null) {
                logger.error { "nlp error : ${response.errorBody()?.string()}" }
            }
            return result
        }

    }

    override fun parseSentence(sentence: SendSentence,
                               userTimeline: UserTimeline,
                               dialog: Dialog,
                               connector: ConnectorController,
                               botDefinition: BotDefinition) {
        SentenceParser(sentence, userTimeline, dialog, connector, botDefinition).parse()
    }

    override fun importNlpDump(stream: InputStream): Boolean = nlpClient.importNlpDump(stream).body() ?: false

    override fun importNlpPlainDump(dump: ApplicationDump): Boolean = nlpClient.importNlpPlainDump(dump).body() ?: false
}