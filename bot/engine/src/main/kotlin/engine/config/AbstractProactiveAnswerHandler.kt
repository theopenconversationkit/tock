/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package engine.config

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.config.ProactiveConversationStatus
import ai.tock.bot.engine.config.ProactiveConversationStatus.LUNCHED
import ai.tock.bot.engine.config.ProactiveConversationStatus.STARTED
import ai.tock.bot.engine.config.ProactiveConversationStatus.CLOSED

private const val PROACTIVE_CONVERSATION_STATUS: String = "PROACTIVE_CONVERSATION_STATUS"

interface AbstractProactiveAnswerHandler {

    fun handleProactiveAnswer(botBus: BotBus)
    fun handle(botBus: BotBus) {
        with(botBus) {
            startProactiveConversation()
            handleProactiveAnswer(this)
            endProactiveConversation()
        }
    }

    private fun BotBus.startProactiveConversation() {
        if(getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS) == null) {
            setBusContextValue(PROACTIVE_CONVERSATION_STATUS, LUNCHED)
            if(underlyingConnector.startProactiveConversation(connectorData.callback)){
                setBusContextValue(PROACTIVE_CONVERSATION_STATUS, STARTED)
            }
        }
    }

    fun BotBus.flushProactiveConversation() {
        if(getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS) == STARTED) {
            underlyingConnector.flushProactiveConversation(connectorData.callback, connectorData.metadata)
        }
    }

    private fun BotBus.endProactiveConversation() {
        if(getBusContextValue<ProactiveConversationStatus>(PROACTIVE_CONVERSATION_STATUS) == STARTED) {
            setBusContextValue(PROACTIVE_CONVERSATION_STATUS, CLOSED)
            underlyingConnector.endProactiveConversation(connectorData.callback, connectorData.metadata)
        }
        else {
            end()
        }
    }
}