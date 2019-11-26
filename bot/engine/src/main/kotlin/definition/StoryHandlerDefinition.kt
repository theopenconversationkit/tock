/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorMessageProvider
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.message.Sentence
import mu.KotlinLogging

/**
 * Story handler definitions are used in [StoryHandler] to provide custom context and to manage specific connector behaviour.
 *
 * Implementations should usually use [StoryHandlerDefinitionBase].
 */
interface StoryHandlerDefinition : BotBus {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * The [ConnectorStoryHandler] provided for the current [BotBus.targetConnectorType] - null if it does not exist.
     */
    val connector: ConnectorStoryHandler<*>? get() = null

    /**
     * The main method to implement.
     */
    fun handle()

    private fun toMessageList(default: CharSequence? = null,
                              messageProvider: () -> Any?): MessagesList {
        val result = messageProvider()
        val list = if (result is Collection<*>) result else listOfNotNull(result)
        val messages = list.mapNotNull { m ->
            when (m) {
                is Message -> m
                is CharSequence -> Sentence(translate(m).toString())
                is ConnectorMessageProvider -> Sentence(null, mutableListOf(m.toConnectorMessage()))
                else -> {
                    logger.error { "message not handled: $m" }
                    null
                }
            }
        }.takeUnless { it.isEmpty() }
            ?: listOfNotNull(default?.let { Sentence(translate(it).toString()) })

        return MessagesList(messages)
    }

    fun answerWith(
        default: CharSequence? = null,
        messageProvider: () -> Any?) {
        end(messages = toMessageList(default, messageProvider))
    }

}