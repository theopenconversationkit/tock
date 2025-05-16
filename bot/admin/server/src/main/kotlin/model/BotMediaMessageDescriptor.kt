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

package ai.tock.bot.admin.model

import ai.tock.bot.connector.media.MediaActionDescriptor
import ai.tock.bot.connector.media.MediaCardDescriptor
import ai.tock.bot.connector.media.MediaMessageDescriptor
import ai.tock.bot.connector.media.MediaMessageType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(

    JsonSubTypes.Type(value = BotMediaActionDescriptor::class, name = "action"),
    JsonSubTypes.Type(value = BotMediaActionDescriptor::class, name = "0"),

    JsonSubTypes.Type(value = BotMediaCardDescriptor::class, name = "card"),
    JsonSubTypes.Type(value = BotMediaCardDescriptor::class, name = "1")
)
interface BotMediaMessageDescriptor {

    companion object {
        fun fromDescriptor(desc: MediaMessageDescriptor, readOnly: Boolean = false): BotMediaMessageDescriptor =
            when (desc) {
                is MediaActionDescriptor -> BotMediaActionDescriptor(desc, readOnly)
                is MediaCardDescriptor -> BotMediaCardDescriptor(desc, readOnly)
                else -> error("unsupported type: $desc")
            }
    }

    val type: MediaMessageType

    fun toDescriptor(): MediaMessageDescriptor
}
