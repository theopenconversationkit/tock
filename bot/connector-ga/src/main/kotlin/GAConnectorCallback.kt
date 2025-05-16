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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.ga.model.GAIntent
import ai.tock.bot.connector.ga.model.request.GARequest
import ai.tock.bot.connector.ga.model.response.GAExpectedInput
import ai.tock.bot.connector.ga.model.response.GAFinalResponse
import ai.tock.bot.connector.ga.model.response.GAInputPrompt
import ai.tock.bot.connector.ga.model.response.GAItem
import ai.tock.bot.connector.ga.model.response.GAResponse
import ai.tock.bot.connector.ga.model.response.GAResponseMetadata
import ai.tock.bot.connector.ga.model.response.GARichResponse
import ai.tock.bot.connector.ga.model.response.GASimpleResponse
import ai.tock.bot.connector.ga.model.response.GAStatus
import ai.tock.bot.connector.ga.model.response.GAStatusCode
import ai.tock.bot.connector.ga.model.response.GAStatusDetail
import ai.tock.bot.connector.ga.model.response.GASuggestion
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import com.google.common.base.Throwables
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.mutableListOf

/**
 *
 */
internal data class GAConnectorCallback(
    override val applicationId: String,
    val controller: ConnectorController,
    val context: RoutingContext,
    val request: GARequest,
    val actions: MutableList<ActionWithDelay> = CopyOnWriteArrayList()
) : ConnectorCallbackBase(applicationId, gaConnectorType) {

    @Volatile
    private var answered: Boolean = false

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    data class ActionWithDelay(val action: Action, val delayInMs: Long = 0)

    fun addAction(event: Event, delayInMs: Long) {
        if (event is Action) {
            actions.add(ActionWithDelay(event, delayInMs))
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }

    private fun GARichResponse.merge(simpleResponse: GAItem?): GARichResponse =
        if (simpleResponse == null) {
            this
        } else {
            copy(
                items = mergeItems(listOf(simpleResponse) + items),
                suggestions = suggestions,
                linkOutSuggestion = linkOutSuggestion
            )
        }

    private fun GARichResponse.merge(other: GARichResponse): GARichResponse =
        copy(
            items = mergeItems(items + other.items),
            suggestions = suggestions + other.suggestions,
            linkOutSuggestion = linkOutSuggestion ?: other.linkOutSuggestion
        )

    private fun GASimpleResponse.isMergeable(other: GASimpleResponse): Boolean {
        return (
            textToSpeech != null && other.textToSpeech != null ||
                (textToSpeech == null && other.textToSpeech == null)
            ) &&
            (
                ssml != null && other.ssml != null ||
                    (ssml == null && other.ssml == null)
                )
    }

    private fun GASimpleResponse.merge(other: GASimpleResponse): GASimpleResponse =
        copy(
            textToSpeech = if (textToSpeech == null) null else textToSpeech + " " + other.textToSpeech,
            ssml = if (ssml == null) null else ssml + " " + other.ssml,
            displayText = if (displayText == null) other.displayText else displayText + if (other.displayText == null) "" else " ${other.displayText}"
        )

    // the first has to be a simple response
    // cf https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#RichResponse
    private fun mergeItems(items: List<GAItem>): List<GAItem> =
        if (items.size < 2) {
            items
        } else {
            val r = mutableListOf<GAItem>()
            var i = 0
            var current = items.first()
            while (++i < items.size) {
                val a = current
                val b = items[i]
                val newA =
                    if (a.simpleResponse != null && b.simpleResponse != null && a.simpleResponse.isMergeable(b.simpleResponse)) {
                        GAItem(simpleResponse = a.simpleResponse.merge(b.simpleResponse))
                    } else {
                        a
                    }
                if (a !== newA) {
                    current = newA
                } else {
                    r.add(a)
                    current = b
                }
            }
            if (!r.contains(current)) {
                r.add(current)
            }
            r
        }

    fun buildResponse(): GAResponse {
        val texts =
            actions
                .filter { it.action is SendSentence && it.action.text != null }
                .mapIndexed { i, a ->
                    val s = a.action as SendSentence
                    val text = s.text!!
                    if (i == 0) {
                        simpleResponseWithoutTranslate(text)
                    } else {
                        simpleResponseWithoutTranslate(text)
                            .run {
                                if (a.delayInMs != 0L && ssml != null) {
                                    copy(
                                        ssml = "<break time=\"${a.delayInMs}ms\"/>" + ssml
                                    )
                                } else {
                                    this
                                }
                            }
                    }
                }

        val simpleResponse = if (texts.isNotEmpty()) {
            GAItem(
                texts.reduce { s, t ->
                    s.copy(
                        textToSpeech = concat(s.textToSpeech, t.textToSpeech),
                        ssml = concat(s.ssml, t.ssml),
                        displayText = concat(s.displayText ?: s.textToSpeech, t.displayText ?: t.textToSpeech)
                    )
                }.run {
                    val newSSML = ssml
                        ?.replace("<speak>", "", true)
                        ?.replace("</speak>", "", true)
                        ?.run { if (isBlank()) null else "<speak>$this</speak>" }
                    copy(
                        textToSpeech = if (newSSML.isNullOrBlank()) textToSpeech else null,
                        ssml = newSSML,
                        displayText = displayText?.run { if (isBlank()) null else this }
                    )
                }
            )
        } else {
            null
        }

        val connectorMessages = actions.map { it.action }
            .filterIsInstance<SendSentence>()
            .mapNotNull {
                (it.message(gaConnectorType) as GAResponseConnectorMessage?)
            }

        var finalResponse: GAFinalResponse? = null
        var expectedInput: GAExpectedInput? = null

        if (connectorMessages.any { it.finalResponse != null }) {
            finalResponse =
                connectorMessages
                    .first { it.finalResponse != null }
                    .finalResponse!!
                    .run {
                        copy(
                            richResponse = richResponse.merge(simpleResponse)
                        )
                    }
        } else {

            val firstExpectedInputWithCard =
                connectorMessages.firstOrNull { it.expectedInput?.inputPrompt?.richInitialPrompt?.items?.any { item -> item.basicCard != null } == true }?.expectedInput
            val message: GAExpectedInput? =
                connectorMessages
                    .mapNotNull { it.expectedInput }
                    .filter { it.inputPrompt.richInitialPrompt.items.all { item -> item.basicCard == null } || it == firstExpectedInputWithCard }
                    .run {
                        if (isEmpty())
                            null
                        else reduce { a, b ->
                            a.copy(
                                inputPrompt = a.inputPrompt.copy(
                                    richInitialPrompt = a.inputPrompt.richInitialPrompt.merge(b.inputPrompt.richInitialPrompt)
                                ),
                                possibleIntents = a.possibleIntents + b.possibleIntents.filter { ib -> a.possibleIntents.none { ia -> ia.intent == ib.intent } }
                            )
                        }
                    }

            expectedInput = if (message == null) {
                if (simpleResponse == null) {
                    logger.warn { "no simple response for $this" }
                    null
                } else {
                    GAExpectedInput(
                        GAInputPrompt(
                            GARichResponse(
                                listOf(simpleResponse)
                            )
                        )
                    )
                }
            } else {
                if (simpleResponse == null) {
                    message
                } else {
                    message.copy(
                        inputPrompt = message.inputPrompt.copy(
                            richInitialPrompt = message.inputPrompt.richInitialPrompt.merge(simpleResponse)
                        )
                    )
                }
            }?.run {
                if (possibleIntents.none { it.intent == GAIntent.text }) {
                    copy(possibleIntents = listOf(expectedTextIntent()) + possibleIntents)
                } else {
                    this
                }
            }
        }

        context.response().putHeader("Google-Actions-API-Version", "2")

        if (expectedInput != null) {
            expectedInput = rebuildGASuitableResponse(expectedInput)
        }
        if (finalResponse != null) {
            finalResponse = rebuildGASuitableResponse(finalResponse)
        }

        return GAResponse(
            request.conversation.conversationToken ?: "",
            finalResponse == null,
            if (expectedInput == null) null else listOf(expectedInput),
            finalResponse,
            null, // GAResponseMetadata(GAStatus(0, "OK")),
            request.isInSandbox
        )
    }

    private fun rebuildGASuitableResponse(expectedInput: GAExpectedInput): GAExpectedInput {
        return GAExpectedInput(
            GAInputPrompt(
                rebuildGASuitableRichResponse(
                    richResponse = expectedInput.inputPrompt.richInitialPrompt,
                    withFinalResponseCheck = false
                ),
                expectedInput.inputPrompt.noInputPrompts
            ),
            expectedInput.possibleIntents,
            expectedInput.speechBiasingHints
        )
    }

    private fun rebuildGASuitableResponse(finalResponse: GAFinalResponse): GAFinalResponse {
        return GAFinalResponse(
            rebuildGASuitableRichResponse(finalResponse.richResponse, true)
        )
    }

    private fun rebuildGASuitableRichResponse(richResponse: GARichResponse, withFinalResponseCheck: Boolean): GARichResponse {
        val simpleResponses = (richResponse.items.filter { item -> item.simpleResponse != null })
        val basicCardResponses = (richResponse.items.filter { item -> item.basicCard != null })
        val structuredResponses = (richResponse.items.filter { item -> item.structuredResponse != null })
        val mediaResponses = (richResponse.items.filter { item -> item.mediaResponse != null })

        val suggestions = mutableListOf<GASuggestion>()
        val items = mutableListOf<GAItem>()
        if (richResponse.items.first().simpleResponse == null) {
            logger.warn { "GA Condition failed : first item in rich response must be a simple response" }
        }
        if (simpleResponses.size> 2) {
            logger.warn { "GA Condition failed : ga message must have at most two simples responses" }
            items.addAll(richResponse.items.filter { item -> item.simpleResponse != null }.take(2))
        } else {
            items.addAll(simpleResponses)
        }
        if (basicCardResponses.size> 1) {
            logger.warn { "GA Condition failed : ga message must have at one basic card" }
            items.add(richResponse.items.first { item -> item.basicCard != null })
        } else {
            items.addAll(basicCardResponses)
        }
        if (structuredResponses.size> 1) {
            logger.warn { "GA Condition failed : ga message must have at one structuredResponse" }
            items.add(richResponse.items.first { item -> item.structuredResponse != null })
        } else {
            items.addAll(structuredResponses)
        }
        if (mediaResponses.size> 1) {
            logger.warn { "GA Condition failed : ga message must have at one mediaResponse" }
            items.add(richResponse.items.first { item -> item.mediaResponse != null })
        } else {
            items.addAll(mediaResponses)
        }
        if (withFinalResponseCheck && richResponse.suggestions.isNotEmpty()) {
            logger.warn { "GA Condition failed : ga final response cant have suggestion chips" }
        } else if (richResponse.suggestions.size> 8) {
            logger.warn { "GA Condition failed : ga message must have at most 8 suggestion chips" }
            suggestions.addAll(richResponse.suggestions.take(8))
        } else {
            suggestions.addAll(richResponse.suggestions)
        }
        return richResponse.copy(
            items = items,
            suggestions = suggestions,
            linkOutSuggestion = richResponse.linkOutSuggestion
        )
    }

    fun sendResponse() {
        try {
            if (!answered) {
                answered = true

                if (isLogoutEvent()) {
                    logger.debug { "ga logout event" }
                    context.response().setStatusCode(401).end()
                    return
                }

                val gaResponse = buildResponse()

                logger.debug { "ga response : $gaResponse" }

                val writeValueAsString = mapper.writeValueAsString(gaResponse)

                logger.debug { "ga json response: $writeValueAsString" }

                context.response().end(writeValueAsString)
            } else {
                logger.trace { "already answered: $this" }
            }
        } catch (t: Throwable) {
            logger.error(t)
            context.fail(t)
        }
    }

    /**
     * test if is logout event for account unlinking
     */
    private fun isLogoutEvent(): Boolean {
        return actions.map { it.action }
            .filterIsInstance<SendSentence>()
            .mapNotNull {
                (it.message(gaConnectorType) as GAResponseConnectorMessage?)
            }.firstOrNull { it.logoutEvent } != null
    }

    override fun eventSkipped(event: Event) {
        super.eventSkipped(event)
        sendResponse()
    }

    override fun eventAnswered(event: Event) {
        super.eventAnswered(event)
        sendResponse()
    }

    override fun exceptionThrown(event: Event, throwable: Throwable) {
        super.exceptionThrown(event, throwable)
        sendTechnicalError(throwable)
    }

    fun sendTechnicalError(
        throwable: Throwable,
        requestBody: String? = null,
        request: GARequest? = null
    ) {
        try {
            logger.error(throwable)
            context.response().end(
                mapper.writeValueAsString(
                    GAResponse(
                        request?.conversation?.conversationToken ?: "",
                        false,
                        emptyList(),
                        GAFinalResponse(
                            GARichResponse(
                                listOf(
                                    GAItem(
                                        GASimpleResponse(
                                            (
                                                controller.errorMessage(
                                                    PlayerId(
                                                        controller.botDefinition.botId,
                                                        PlayerType.bot
                                                    ),
                                                    applicationId,
                                                    PlayerId(
                                                        request?.conversation?.conversationId ?: "unknown",
                                                        PlayerType.user
                                                    )
                                                ) as? SendSentence
                                                )?.stringText ?: "Technical error"
                                        )
                                    )
                                )
                            )
                        ),
                        GAResponseMetadata(
                            GAStatus(
                                GAStatusCode.INTERNAL,
                                throwable.message ?: "error",
                                listOf(
                                    GAStatusDetail(
                                        Throwables.getStackTraceAsString(throwable),
                                        requestBody,
                                        request
                                    )
                                )
                            )
                        ),
                        false
                    )
                )
            )
        } catch (t: Throwable) {
            logger.error(t)
            context.fail(t)
        }
    }
}
