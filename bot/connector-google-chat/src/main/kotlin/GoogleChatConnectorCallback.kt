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
import ai.tock.shared.booleanProperty
import com.google.api.services.chat.v1.HangoutsChat
import com.google.api.services.chat.v1.model.Thread
import mu.KotlinLogging

private val sendIntroMessage: Boolean = booleanProperty("tock_api_google_chat_connector_test_send_intro_message", false)

data class GoogleChatConnectorCallback(
    override val applicationId: String,
    val spaceName: String,
    val threadName: String,
    private val chatService: HangoutsChat,
    private val introMessage: String?,
    val useThread: Boolean,
    private val waitingMessage: String,
) : ConnectorCallbackBase(applicationId, googleChatConnectorType) {
    private val logger = KotlinLogging.logger {}
    var processingMessageName: String? = null

    fun initializeProcessingMessage() {
        processingMessageName =
            sendGoogleMessageAndGetName(
                GoogleChatConnectorTextMessageOut(waitingMessage),
            )
    }

    /**
     * Called when the UserTimeline is loaded.
     * Sends the intro message if this is a new conversation (empty dialog).
     */
    override fun initialUserTimelineLoaded(userTimeline: UserTimeline) {
        if (shouldSendIntro(userTimeline) || sendIntroMessage) {
            sendIntroMessage()
            initializeProcessingMessage()
        }
    }

    /**
     * Determines if the intro message should be sent.
     * Returns true if:
     * - An intro message is configured
     * - The user timeline has no bot actions (new conversation)
     */
    private fun shouldSendIntro(userTimeline: UserTimeline): Boolean = introMessage != null && !userTimeline.containsBotAction()

    /**
     * Sends the intro message to the Google Chat space/thread.
     */
    private fun sendIntroMessage() {
        val message = introMessage ?: return
        logger.info {
            "Sending Google Chat intro message: space=$spaceName" +
                if (useThread) ", thread=$threadName" else ""
        }
        val gMessage = GoogleChatConnectorTextMessageOut(message)

        processingMessageName?.let {
            patchGoogleMessage(it, gMessage)
            processingMessageName = null
        } ?: sendGoogleMessageAndGetName(gMessage)
    }

    /**
     * Sends message to the Google Chat space/thread.
     */
    fun sendGoogleMessageAndGetName(message: GoogleChatConnectorMessage): String? {
        var messageName: String? = null
        try {
            val googleMessage = message.toGoogleMessage()
            logger.debug { "Google Message content: $googleMessage" }

            if (useThread) {
                googleMessage.thread = Thread().setName(threadName)
            }

            logger.info {
                "Sending Google Chat message: space=$spaceName" +
                    if (useThread) ", thread=$threadName" else ""
            }

            val request =
                chatService
                    .spaces()
                    .messages()
                    .create(spaceName, googleMessage)

            if (useThread) {
                request.messageReplyOption = "REPLY_MESSAGE_FALLBACK_TO_NEW_THREAD"
            }

            val response = request.execute()
            messageName = response?.name

            logger.info { "Google Chat API response: $messageName" }
        } catch (e: Exception) {
            logger.error(e) {
                "Failed to send Google Chat message: " +
                    "space=$spaceName" +
                    if (useThread) ", thread=$threadName" else ""
            }
        }
        return messageName
    }

    fun patchGoogleMessage(
        messageName: String,
        message: GoogleChatConnectorMessage,
    ) {
        try {
            chatService
                .spaces()
                .messages()
                .patch(
                    messageName,
                    message.toGoogleMessage(),
                )
                .setUpdateMask("text")
                .execute()

            logger.info {
                "Google Chat message patched: $messageName"
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Failed to patch Google Chat message: $messageName"
            }
        }
    }
}
