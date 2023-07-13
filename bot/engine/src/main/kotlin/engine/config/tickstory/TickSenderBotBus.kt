/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.engine.config.tickstory

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.message.ActionWrappedMessage
import ai.tock.bot.sender.TickSender
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.Translator

/**
 * A bot bus tick sender, it uses a [BotBus] to send messages
 */
class TickSenderBotBus(private val botBus: BotBus): TickSender {

    override fun sendById(id: String) { botBus.send(translateId(id)) }

    override fun endById(id: String) { botBus.end(translateId(id)) }

    override fun sendPlainText(text: String) { botBus.send(text) }

    override fun endPlainText(text: String) { botBus.end(text) }

    override fun end() {
        botBus.end { null }
    }

    private fun translateId(id: String): ActionWrappedMessage {
        val label = Translator.getLabel(id)
        label ?: throw IllegalStateException("Label $id not found")

        return ActionWrappedMessage(
            SendSentence(
                botBus.botId,
                botBus.applicationId,
                botBus.userId,
                botBus.translate(I18nLabelValue(label))), 0)
    }
}