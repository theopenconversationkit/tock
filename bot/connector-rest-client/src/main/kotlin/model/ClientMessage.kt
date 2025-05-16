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

package ai.tock.bot.connector.rest.client.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 *
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "eventType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ClientAttachment::class, name = "attachment"),
    JsonSubTypes.Type(value = ClientChoice::class, name = "choice"),
    JsonSubTypes.Type(value = ClientLocation::class, name = "location"),
    JsonSubTypes.Type(value = ClientSentence::class, name = "sentence"),
    JsonSubTypes.Type(value = ClientSentenceWithFootnotes::class, name = "sentenceWithFootnotes"),
    JsonSubTypes.Type(value = ClientDebug::class, name = "debug")
)
abstract class ClientMessage(
    val eventType: ClientEventType,
    var delay: Long = 0L
)
