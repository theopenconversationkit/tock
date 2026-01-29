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

package ai.tock.bot.api.model.message.bot

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Sentence::class, name = "sentence"),
    JsonSubTypes.Type(value = Card::class, name = "card"),
    JsonSubTypes.Type(value = CustomMessage::class, name = "custom"),
    JsonSubTypes.Type(value = Carousel::class, name = "carousel"),
    JsonSubTypes.Type(value = Debug::class, name = "debug"),
    JsonSubTypes.Type(value = Event::class, name = "event"),
    JsonSubTypes.Type(value = CustomAction::class, name = "action"),
)
interface BotMessage {
    /**
     * The delay to wait before sending this message.
     */
    val delay: Long
}
