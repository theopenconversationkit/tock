/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.messenger.model.send

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Location

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "content_type"
)
@JsonSubTypes(
        JsonSubTypes.Type(value = TextQuickReply::class, name = "text"),
        JsonSubTypes.Type(value = LocationQuickReply::class, name = "location"),
        JsonSubTypes.Type(value = EmailQuickReply::class, name = "user_email")
)
abstract class QuickReply(
        @get:JsonProperty("content_type") val contentType: QuickReplyContentType) : UserAction {

    open fun toChoice(): Choice? = null

    open fun toLocation(): Location? = null
}