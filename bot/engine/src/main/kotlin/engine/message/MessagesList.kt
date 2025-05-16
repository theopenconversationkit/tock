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

package ai.tock.bot.engine.message

import ai.tock.bot.connector.ConnectorMessageProvider
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.Bus
import mu.KotlinLogging

/**
 * A list of messages.
 */
data class MessagesList(val messages: List<Message>) {

    constructor(vararg messages: Message) : this(messages.toList())

    companion object {
        private val logger = KotlinLogging.logger {}

        /**
         * Transforms [messageProvider] result into a [MessageList].
         * @param default the default message used if [messageProvider] returns no message
         * @param messageProvider the message provider.
         */
        fun toMessageList(
            default: CharSequence? = null,
            bus: BotBus,
            messageProvider: BotBus.() -> Any?
        ): MessagesList {

            val result = messageProvider(bus)
            val list = if (result is Collection<*>) result else listOfNotNull(result)
            val messages = list.mapNotNull { m ->
                when (m) {
                    is Message -> m
                    is CharSequence -> Sentence(bus.translate(m).toString())
                    is ConnectorMessageProvider -> Sentence(null, mutableListOf(m.toConnectorMessage()))
                    else -> {
                        if (m !is Unit && m !is Bus<*>) {
                            logger.warn { "message not handled: $m" }
                        }
                        null
                    }
                }
            }.takeUnless { it.isEmpty() }
                ?: listOfNotNull(default?.let { Sentence(bus.translate(it).toString()) })

            return MessagesList(messages)
        }
    }
}
