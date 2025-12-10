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

package ai.tock.bot.connector.googlechat

import ai.tock.bot.connector.ConnectorCallbackBase
import ai.tock.bot.connector.googlechat.builder.googleChatConnectorType
import ai.tock.bot.engine.user.UserTimeline
import com.google.api.services.chat.v1.HangoutsChat
import com.google.api.services.chat.v1.model.Thread
import mu.KotlinLogging

data class GoogleChatConnectorCallback(
    override val applicationId: String,
    val spaceName: String,
    val threadName: String,
    private val chatService: HangoutsChat,
    private val introMessage: String?,
) : ConnectorCallbackBase(applicationId, googleChatConnectorType) {

    private val logger = KotlinLogging.logger {}

    /**
     * Called when the UserTimeline is loaded.
     * Sends the intro message if this is a new conversation (empty dialog).
     */
    override fun initialUserTimelineLoaded(userTimeline: UserTimeline) {
        if (shouldSendIntro(userTimeline)) {
            sendIntroMessage()
        }
    }

    /**
     * Determines if the intro message should be sent.
     * Returns true if:
     * - An intro message is configured
     * - The user timeline has no bot actions (new conversation)
     */
    private fun shouldSendIntro(userTimeline: UserTimeline): Boolean {
        return introMessage != null && !userTimeline.containsBotAction()
    }

    /**
     * Sends the intro message to the Google Chat space/thread.
     */
    private fun sendIntroMessage() {
        if (introMessage == null) return

        try {
            logger.info {
                "Sending intro message to Google Chat: space=$spaceName, thread=$threadName"
            }

            val response = chatService
                .spaces()
                .messages()
                .create(
                    spaceName,
                    GoogleChatConnectorTextMessageOut(introMessage)
                        .toGoogleMessage()
                        .setThread(Thread().setName(threadName))
                )
                .setMessageReplyOption("REPLY_MESSAGE_FALLBACK_TO_NEW_THREAD")
                .execute()

            logger.info { "Google Chat API intro response: ${response?.name}" }
        } catch (e: Exception) {
            logger.error(e) {
                "Failed to send intro message to Google Chat (space=$spaceName, thread=$threadName)"
            }
        }
    }
}