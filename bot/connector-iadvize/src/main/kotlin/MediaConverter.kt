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
package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.iadvize.model.response.conversation.QuickReply
import ai.tock.bot.connector.iadvize.model.payload.GenericCardPayload
import ai.tock.bot.connector.iadvize.model.payload.Payload
import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.response.conversation.payload.genericjson.Action
import ai.tock.bot.connector.iadvize.model.response.conversation.payload.genericjson.Image
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.connector.media.MediaAction
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.image

internal object MediaConverter {

    fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        when (message) {
            is MediaCard -> fromMediaCard(message)
            else -> emptyList()
        }
    }

    fun toSimpleMessage(
        message: CharSequence,
        suggestions: List<CharSequence>
    ): BotBus.() -> ConnectorMessage? = {
        val payload: Payload = TextPayload(translate(message).toString())
        val quickReply: MutableList<QuickReply> = suggestionToQuickReplies(suggestions)

        IadvizeConnectorMessage(IadvizeMessage(payload, quickReply))
    }

    private fun BotBus.fromMediaCard(message: MediaCard): List<ConnectorMessage> {
        val title: String = translate(message.title).toString()
        val text: String = translate(message.subTitle).toString()

        val file = message.file
        val image: Image? = toImage(file)

        val actions = message.actions
        val quickReply: MutableList<QuickReply> = actionToQuickReplies(actions)
        val payloadActions: List<Action> = toActions(actions)

        val payload: Payload = getPayload(title, text, image, payloadActions)

        val iadvizeMessageOnConnector = IadvizeConnectorMessage(IadvizeMessage(payload, quickReply))

        return listOf(iadvizeMessageOnConnector)
    }

    private fun BotBus.actionToQuickReplies(actions: List<MediaAction>): MutableList<QuickReply> {
        return actions.filter { isQuickReply(it) }
                      .map { toQuickReplies(it) }
                      .toMutableList()
    }

    private fun BotBus.suggestionToQuickReplies(suggestions: List<CharSequence>): MutableList<QuickReply> {
        return suggestions.map { toQuickReplies(it) }
                          .toMutableList()
    }

    private fun isQuickReply(action: MediaAction): Boolean {
       return action.url == null
    }

    private fun BotBus.toQuickReplies(action: MediaAction): QuickReply {
        return QuickReply(translate(action.title).toString())
    }

    private fun BotBus.toQuickReplies(suggestion: CharSequence): QuickReply {
        return QuickReply(translate(suggestion).toString())
    }

    private fun BotBus.toActions(actions: List<MediaAction>): List<Action> {
        return actions.filter { it.url != null }
                      .map {
                          Action(translate(it.title).toString(), it.url!!)
                      }
    }
    private fun BotBus.toImage(file: MediaFile?): Image? {
        return if(image.equals(file?.type)) {
            //if equals true, file cannot be null
            Image(file!!.url, file.name)
        } else {
            null
        }
    }

    private fun getPayload(title: String, text: String, image: Image?, actions: List<Action>): Payload {
        return if((title.isNotBlank() xor text.isNotBlank()) && image == null && actions.isEmpty()) {
            //There are only one between title and text, and no image, and no action
            // create a TextPayload by combine title and text, but only one is not empty
            TextPayload(title + text)
        } else {
            GenericCardPayload(title, text, image, actions)
        }
    }
}
