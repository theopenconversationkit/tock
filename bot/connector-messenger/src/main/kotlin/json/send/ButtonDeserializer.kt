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

package ai.tock.bot.connector.messenger.json.send

import ai.tock.bot.connector.messenger.model.send.Button
import ai.tock.bot.connector.messenger.model.send.ButtonType
import ai.tock.bot.connector.messenger.model.send.CallButton
import ai.tock.bot.connector.messenger.model.send.LoginButton
import ai.tock.bot.connector.messenger.model.send.LogoutButton
import ai.tock.bot.connector.messenger.model.send.PostbackButton
import ai.tock.bot.connector.messenger.model.send.UrlButton
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import mu.KotlinLogging

/**
 *
 */
internal class ButtonDeserializer : JacksonDeserializer<Button>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Button? {
        data class ButtonFields(
            var type: ButtonType? = null,
            var url: String? = null,
            var title: String? = null,
            var payload: String? = null,
            var other: EmptyJson? = null
        )

        val (type, url, title, payload) = jp.read<ButtonFields> { fields, name ->
            with(fields) {
                when (name) {
                    Button::type.name -> type = jp.readValue()
                    UrlButton::url.name -> url = jp.valueAsString
                    UrlButton::title.name -> title = jp.valueAsString
                    PostbackButton::payload.name -> payload = jp.valueAsString
                    else -> other = jp.readUnknownValue()
                }
            }
        }

        return if (type != null) {
            when (type) {
                ButtonType.postback -> PostbackButton(payload ?: "", title ?: "")
                ButtonType.web_url -> UrlButton(url ?: "", title ?: "")
                ButtonType.account_link -> LoginButton(url ?: "")
                ButtonType.account_unlink -> LogoutButton()
                ButtonType.phone_number -> CallButton(title ?: "", payload ?: "")
            }
        } else {
            logger.warn { "invalid button" }
            null
        }
    }
}
