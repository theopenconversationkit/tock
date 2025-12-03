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

package ai.tock.bot.connector.messenger.model.send

import ai.tock.bot.connector.messenger.AttachmentCacheService
import ai.tock.bot.connector.messenger.MessengerConfiguration.reuseAttachmentByDefault
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendAttachment
import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 */
data class UrlPayload(
    val url: String?,
    @JsonProperty("attachment_id")
    val attachmentId: String?,
    @JsonProperty("is_reusable")
    val reusable: Boolean?,
) : Payload() {
    companion object {
        /**
         * Create an UrlPayload from an url.
         * Uses default cache configuration.
         */
        fun getUrlPayload(attachment: SendAttachment): UrlPayload {
            return getUrlPayload(
                attachment.applicationId,
                attachment.url,
                reuseAttachmentByDefault && !attachment.state.testEvent,
            )
        }

        internal fun getUrlPayload(
            bus: BotBus,
            url: String,
        ): UrlPayload = getUrlPayload(bus.applicationId, url, reuseAttachmentByDefault && !bus.action.state.testEvent)

        /**
         * Create an UrlPayload from an url.
         * @param applicationId the applicationId
         * @param url the url
         * @param useCache is cache is used?
         */
        fun getUrlPayload(
            applicationId: String,
            url: String,
            useCache: Boolean,
        ): UrlPayload {
            return if (useCache) {
                val attachmentId = AttachmentCacheService.getAttachmentId(applicationId, url)
                if (attachmentId == null) {
                    UrlPayload(url, null, true)
                } else {
                    UrlPayload(null, attachmentId, null)
                }
            } else {
                UrlPayload(url, null, null)
            }
        }
    }
}
