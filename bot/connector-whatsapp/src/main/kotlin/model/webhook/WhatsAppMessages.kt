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

package ai.tock.bot.connector.whatsapp.model.webhook

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.whatsapp.model.common.WhatsAppError
import ai.tock.bot.connector.whatsapp.whatsAppConnectorType
import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * See https://developers.facebook.com/docs/whatsapp/api/webhooks/inbound#message-obj.
 */
data class WhatsAppMessages(
    val messages: List<WhatsAppMessage> = emptyList(),
    val statuses: List<WhatsAppStatus> = emptyList(),
    val errors: List<WhatsAppError> = emptyList(),
    val contacts: List<WhatsAppContact> = emptyList(),
) : ConnectorMessage {
    @get:JsonIgnore
    override val connectorType: ConnectorType
        get() = whatsAppConnectorType
}
