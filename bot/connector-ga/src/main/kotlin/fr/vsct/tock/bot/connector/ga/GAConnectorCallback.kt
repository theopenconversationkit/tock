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

package fr.vsct.tock.bot.connector.ga

import fr.vsct.tock.bot.connector.ConnectorCallbackBase
import fr.vsct.tock.bot.connector.ga.model.GAIntent
import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.connector.ga.model.response.GABasicCard
import fr.vsct.tock.bot.connector.ga.model.response.GAExpectedInput
import fr.vsct.tock.bot.connector.ga.model.response.GAFinalResponse
import fr.vsct.tock.bot.connector.ga.model.response.GAInputPrompt
import fr.vsct.tock.bot.connector.ga.model.response.GAItem
import fr.vsct.tock.bot.connector.ga.model.response.GAResponse
import fr.vsct.tock.bot.connector.ga.model.response.GARichResponse
import fr.vsct.tock.bot.connector.ga.model.response.GASimpleResponse
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.shared.jackson.mapper
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 */
internal data class GAConnectorCallback(
        override val applicationId: String,
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


    private fun GABasicCard.merge(other: GABasicCard): GABasicCard =
            copy(
                    title = title?.takeUnless { it.isBlank() } ?: other.title,
                    subtitle = subtitle?.takeUnless { it.isBlank() } ?: other.subtitle,
                    formattedText = formattedText ?: other.formattedText,
                    image = image ?: other.image,
                    buttons = if (buttons.isNotEmpty()) buttons else other.buttons
            )

    private fun GASimpleResponse.isMergeable(other: GASimpleResponse): Boolean {
        return (textToSpeech != null && other.textToSpeech != null
                || (textToSpeech == null && other.textToSpeech == null))
                && (ssml != null && other.ssml != null
                || (ssml == null && other.ssml == null))
    }

    private fun GASimpleResponse.merge(other: GASimpleResponse): GASimpleResponse =
            copy(
                    textToSpeech = if (textToSpeech == null) null else textToSpeech + " " + other.textToSpeech,
                    ssml = if (ssml == null) null else ssml + " " + other.ssml,
                    displayText = if (displayText == null) other.displayText else displayText + if (other.displayText == null) "" else " ${other.displayText}"
            )

    //the first has to be a simple response
    //cf https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#RichResponse
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
                    val newA = if (a.simpleResponse != null && b.simpleResponse != null && a.simpleResponse.isMergeable(b.simpleResponse)) {
                        GAItem(simpleResponse = a.simpleResponse.merge(b.simpleResponse))
                    } else if (a.basicCard != null && b.basicCard != null) {
                        GAItem(basicCard = a.basicCard.merge(b.basicCard))
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
                                ?.run { if (isBlank()) null else "<speak>${this}</speak>" }
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
            val message: GAExpectedInput? =
                    connectorMessages
                            .mapNotNull { it.expectedInput }
                            .run {
                                if (isEmpty())
                                    null
                                else reduce { a, b ->
                                    a.copy(
                                            inputPrompt = a.inputPrompt.copy(
                                                    richInitialPrompt = a.inputPrompt.richInitialPrompt.merge(b.inputPrompt.richInitialPrompt)
                                            ),
                                            possibleIntents = a.possibleIntents + b.possibleIntents.filter { ib -> a.possibleIntents.none { ia -> ia.intent == ib.intent } })
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
                                            listOf(simpleResponse))))
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

        return GAResponse(
                request.conversation.conversationToken ?: "",
                finalResponse == null,
                if (expectedInput == null) null else listOf(expectedInput),
                finalResponse,
                null,
                null, //GAResponseMetadata(GAStatus(0, "OK")),
                request.isInSandbox
        )
    }

    private fun sendResponse() {
        if (!answered) {
            answered = true
            val gaResponse = buildResponse()

            logger.debug { "ga response : $gaResponse" }

            val writeValueAsString = mapper.writeValueAsString(gaResponse)

            logger.debug { "ga json response: $writeValueAsString" }

            context.response().end(writeValueAsString)
        } else {
            logger.error { "already answered: $this" }
        }
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
        sendResponse()
    }
}