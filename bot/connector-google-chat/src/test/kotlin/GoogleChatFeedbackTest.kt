/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

import ai.tock.bot.connector.ConnectorConfiguration
import ai.tock.bot.engine.action.FeedbackVote
import com.google.api.services.chat.v1.model.Message
import com.google.gson.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GoogleChatFeedbackTest {
    private val feedback = GoogleChatFeedback("https://example.com/google-chat")

    @Test
    fun `should append up and down feedback buttons to a message`() {
        val message = feedback.addButtons(Message().setText("Answer"), "action-123")

        assertThat(message.text).isEqualTo("Answer")
        val buttons =
            message.accessoryWidgets
                .single()
                .buttonList.buttons
        assertThat(buttons).hasSize(2)
        assertThat(buttons.map { it.icon.materialIcon.name }).containsExactly("thumb_up", "thumb_down")
        assertThat(buttons).allSatisfy { button ->
            assertThat(button.disabled).isFalse()
            assertThat(button.onClick.action.function).isEqualTo("https://example.com/google-chat")
            assertThat(button.onClick.action.loadIndicator).isEqualTo("SPINNER")
            assertThat(
                button.onClick.action.parameters
                    .associate { it.key to it.value },
            ).containsEntry(GOOGLE_CHAT_FEEDBACK_ACTION_PARAMETER, GOOGLE_CHAT_FEEDBACK_ACTION_VALUE)
                .containsEntry(GOOGLE_CHAT_FEEDBACK_ACTION_ID_PARAMETER, "action-123")
        }
        assertThat(
            buttons.map {
                it.onClick.action.parameters
                    .last()
                    .value
            },
        ).containsExactly("UP", "DOWN")
    }

    @Test
    fun `should replace feedback buttons with a disabled acknowledgement`() {
        val button =
            feedback
                .acknowledgement(FeedbackVote.DOWN, "action-123")
                .single()
                .buttonList.buttons
                .single()

        assertThat(button.disabled).isTrue()
        assertThat(button.text).isNull()
        assertThat(button.icon.materialIcon.name).isEqualTo("thumb_down")
        assertThat(button.onClick.action.function).isEqualTo("https://example.com/google-chat")
        val parameters =
            button.onClick.action.parameters
                .associate { it.key to it.value }
        assertThat(parameters).containsEntry(GOOGLE_CHAT_FEEDBACK_ACTION_ID_PARAMETER, "action-123")
    }

    @Test
    fun `should require an HTTPS authentication audience when feedback is enabled`() {
        val configuration =
            ConnectorConfiguration(
                connectorId = "connector-id",
                path = "/google-chat",
                type = GoogleChatConnectorProvider.connectorType,
                parameters =
                    mapOf(
                        "authenticationAudience" to "123456789",
                        "enableFeedback" to "1",
                    ),
            )

        assertThat(GoogleChatConnectorProvider.check(configuration))
            .contains("Authentication Audience must be an absolute HTTPS URL when feedback buttons are enabled")
    }

    @Test
    fun `should accept an HTTPS authentication audience when feedback is enabled`() {
        val configuration =
            ConnectorConfiguration(
                connectorId = "connector-id",
                path = "/google-chat",
                type = GoogleChatConnectorProvider.connectorType,
                parameters =
                    mapOf(
                        "authenticationAudience" to "https://example.com/google-chat",
                        "enableFeedback" to "1",
                    ),
            )

        assertThat(GoogleChatConnectorProvider.check(configuration)).isEmpty()
    }

    @Test
    fun `should convert a feedback button event`() {
        val messageEvent =
            JsonParser
                .parseString(
                    """
                    {
                      "commonEventObject": {
                        "parameters": {
                          "$GOOGLE_CHAT_FEEDBACK_ACTION_PARAMETER": "$GOOGLE_CHAT_FEEDBACK_ACTION_VALUE",
                          "$GOOGLE_CHAT_FEEDBACK_ACTION_ID_PARAMETER": "action-123",
                          "$GOOGLE_CHAT_FEEDBACK_VOTE_PARAMETER": "UP"
                        }
                      },
                      "chat": {
                        "user": { "name": "users/user-123" },
                        "buttonClickedPayload": {
                          "message": { "name": "spaces/space-123/messages/message-123" }
                        }
                      }
                    }
                    """.trimIndent(),
                ).asJsonObject
        val request =
            GoogleChatRequestConverter.toFeedbackRequest(
                messageEvent,
                messageEvent.getAsJsonObject("chat"),
                "connector-id",
            )

        assertThat(request).isNotNull
        assertThat(request!!.messageName).isEqualTo("spaces/space-123/messages/message-123")
        assertThat(request.vote).isEqualTo(FeedbackVote.UP)
        assertThat(request.event.userId.id).isEqualTo("users/user-123")
        assertThat(request.event.actionId).isEqualTo("action-123")
        assertThat(request.event.feedback?.vote).isEqualTo(FeedbackVote.UP)
        assertThat(request.event.replaceExisting).isFalse()
    }

    @Test
    fun `should ignore an unsupported button event`() {
        val messageEvent =
            JsonParser
                .parseString(
                    """
                    {
                      "commonEventObject": { "parameters": { "action": "other" } },
                      "chat": {
                        "user": { "name": "users/user-123" },
                        "buttonClickedPayload": {
                          "message": { "name": "spaces/space-123/messages/message-123" }
                        }
                      }
                    }
                    """.trimIndent(),
                ).asJsonObject

        assertThat(
            GoogleChatRequestConverter.toFeedbackRequest(
                messageEvent,
                messageEvent.getAsJsonObject("chat"),
                "connector-id",
            ),
        ).isNull()
    }
}
