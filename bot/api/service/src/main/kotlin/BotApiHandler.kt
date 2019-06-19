/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.api.service

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.api.model.BotResponse
import fr.vsct.tock.bot.api.model.UserRequest
import fr.vsct.tock.bot.api.model.message.bot.BotMessage
import fr.vsct.tock.bot.api.model.message.bot.Card
import fr.vsct.tock.bot.api.model.message.bot.I18nText
import fr.vsct.tock.bot.api.model.message.bot.Sentence
import fr.vsct.tock.bot.connector.media.MediaAction
import fr.vsct.tock.bot.connector.media.MediaCard
import fr.vsct.tock.bot.connector.media.MediaFile
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.WebSocketListener
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.config.UploadedFilesService
import fr.vsct.tock.bot.engine.message.ActionWrappedMessage
import fr.vsct.tock.bot.engine.message.MessagesList
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.translator.I18nContext
import fr.vsct.tock.translator.Translator
import mu.KotlinLogging
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

internal class BotApiHandler(webhookUrl: String?) {

    private val logger = KotlinLogging.logger {}

    private val client = webhookUrl?.let {
        try {
            BotApiClient(it)
        } catch (e: Exception) {
            logger.error(e)
            null
        }
    }

    fun send(bus: BotBus) {
        val request = bus.toUserRequest()
        if (client != null) {

            val response = client.send(request)
            bus.handleResponse(request, response)

        } else {
            val pushHandler = WebSocketListener.pushHandler
            if (pushHandler != null) {
                pushHandler.invoke(mapper.writeValueAsString(request))
                var response: BotResponse? = null
                val latch = CountDownLatch(1)
                WebSocketListener.receivedHandler = {
                    response = mapper.readValue(it)
                    latch.countDown()
                }
                latch.await(10, SECONDS)
                bus.handleResponse(request, response)
            } else {
                error("no webhook set and websocket is not enabled")
            }
        }
    }

    private fun BotBus.handleResponse(request: UserRequest, response: BotResponse?) {
        val messages = response?.messages
        if (messages.isNullOrEmpty()) {
            error("no response for $request")
        }
        messages.subList(0, messages.size - 1)
            .forEach { a ->
                send(a)
            }
        messages.last().apply {
            send(this, true)
        }
    }

    private fun BotBus.send(message: BotMessage, end: Boolean = false) {
        val actions =
            when (message) {
                is Sentence -> listOf(toAction(message))
                is Card -> toActions(message)
                else -> error("unsupported message $message")
            }

        val messagesList = MessagesList(actions.map { ActionWrappedMessage(it, 0) })
        val delay = botDefinition.defaultDelay(currentAnswerIndex)
        if (end) {
            end(messagesList, delay)
        } else {
            send(messagesList, delay)
        }
    }

    private fun BotBus.toAction(sentence: Sentence): Action {
        val text = translateText(sentence.text)
        if (sentence.suggestions.isNotEmpty() && text != null) {
            val message = targetConnector.addSuggestions(text, sentence.suggestions.mapNotNull { translateText(it.title) }).invoke(this)
            if (message != null) {
                return SendSentence(
                    botId,
                    applicationId,
                    userId,
                    null,
                    mutableListOf(message)
                )
            }
        }
        return SendSentence(
            botId,
            applicationId,
            userId,
            text
        )
    }

    private fun BotBus.toActions(card: Card): List<Action> {
        val connectorMessages =
            toMediaCard(card)
                .takeIf { it.isValid() }
                ?.let {
                    targetConnector.toConnectorMessage(it).invoke(this)
                }

        return connectorMessages?.map {
            SendSentence(
                botId,
                applicationId,
                userId,
                null,
                mutableListOf(it)
            )
        } ?: emptyList()
    }

    private fun BotBus.toMediaCard(card: Card): MediaCard =
        MediaCard(
            translateText(card.title),
            translateText(card.subTitle),
            card.attachment?.let {
                MediaFile(
                    it.url,
                    it.url,
                    it.type?.let { AttachmentType.valueOf(it.name) } ?: UploadedFilesService.attachmentType(it.url))
            },
            card.actions.map {
                MediaAction(
                    translateText(it.title) ?: "",
                    it.url
                )
            }
        )

}

private fun BotBus.translateText(i18n: I18nText?): CharSequence? =
    when {
        i18n == null -> null
        i18n.toBeTranslated -> translate(i18n.text, i18n.args)
        else -> Translator.formatMessage(
            i18n.text,
            I18nContext(userLocale,
                userInterfaceType,
                targetConnectorType.id,
                contextId),
            i18n.args
        )
    }
