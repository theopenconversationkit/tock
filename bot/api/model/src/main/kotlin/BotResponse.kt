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

package ai.tock.bot.api.model

import ai.tock.bot.api.model.context.Entity
import ai.tock.bot.api.model.message.bot.BotMessage

fun merge(
    r1: BotResponse?,
    r2: BotResponse?,
) = if (r1 == null) {
    r2
} else if (r2 == null) {
    r1
} else {
    r2.copy(
        messages = r1.messages + r2.messages,
        entities = r1.entities + r2.entities,
    )
}

data class BotResponse(
    val messages: List<BotMessage> = emptyList(),
    val storyId: String,
    val step: String?,
    val entities: List<Entity> = emptyList(),
    val context: ResponseContext,
)
