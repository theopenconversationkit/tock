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

package ai.tock.bot.connector.businesschat.model.input

import ai.tock.bot.engine.BotBus

/**
 * A Text Message used on the bot side to be sent on the [BotBus]
 */
data class BusinessChatConnectorTextMessage(
    override val sourceId: String,
    override val destinationId: String,
    val body: String?
) : BusinessChatConnectorMessage()
