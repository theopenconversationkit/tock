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

import ai.tock.bot.engine.action.FeedbackVote
import com.google.api.services.chat.v1.model.AccessoryWidget
import com.google.api.services.chat.v1.model.GoogleAppsCardV1Action
import com.google.api.services.chat.v1.model.GoogleAppsCardV1ActionParameter
import com.google.api.services.chat.v1.model.GoogleAppsCardV1Button
import com.google.api.services.chat.v1.model.GoogleAppsCardV1ButtonList
import com.google.api.services.chat.v1.model.GoogleAppsCardV1Icon
import com.google.api.services.chat.v1.model.GoogleAppsCardV1MaterialIcon
import com.google.api.services.chat.v1.model.GoogleAppsCardV1OnClick
import com.google.api.services.chat.v1.model.Message

internal const val GOOGLE_CHAT_FEEDBACK_ACTION_PARAMETER = "TOCK_ACTION"
internal const val GOOGLE_CHAT_FEEDBACK_ACTION_VALUE = "FEEDBACK"
internal const val GOOGLE_CHAT_FEEDBACK_ACTION_ID_PARAMETER = "TOCK_ACTION_ID"
internal const val GOOGLE_CHAT_FEEDBACK_VOTE_PARAMETER = "TOCK_FEEDBACK_VOTE"

class GoogleChatFeedback(
    private val callbackUrl: String,
) {
    fun addButtons(
        message: Message,
        actionId: String,
    ): Message = message.setAccessoryWidgets(listOf(buttonsWidget(actionId)))

    fun acknowledgement(
        vote: FeedbackVote,
        actionId: String,
    ): List<AccessoryWidget> =
        listOf(
            AccessoryWidget().setButtonList(
                GoogleAppsCardV1ButtonList().setButtons(
                    listOf(
                        feedbackButton(
                            vote = vote,
                            actionId = actionId,
                            disabled = true,
                        ),
                    ),
                ),
            ),
        )

    private fun buttonsWidget(actionId: String): AccessoryWidget =
        AccessoryWidget().setButtonList(
            GoogleAppsCardV1ButtonList().setButtons(
                FeedbackVote.entries.map { feedbackButton(it, actionId) },
            ),
        )

    private fun feedbackButton(
        vote: FeedbackVote,
        actionId: String,
        disabled: Boolean = false,
    ): GoogleAppsCardV1Button {
        val button =
            GoogleAppsCardV1Button()
                .setAltText(vote.altText)
                .setDisabled(disabled)
                .setIcon(
                    GoogleAppsCardV1Icon()
                        .setAltText(vote.altText)
                        .setMaterialIcon(GoogleAppsCardV1MaterialIcon().setName(vote.materialIcon)),
                )

        return button.setOnClick(
            GoogleAppsCardV1OnClick().setAction(
                GoogleAppsCardV1Action()
                    .setFunction(callbackUrl)
                    .setLoadIndicator("SPINNER")
                    .setParameters(
                        listOf(
                            actionParameter(GOOGLE_CHAT_FEEDBACK_ACTION_PARAMETER, GOOGLE_CHAT_FEEDBACK_ACTION_VALUE),
                            actionParameter(GOOGLE_CHAT_FEEDBACK_ACTION_ID_PARAMETER, actionId),
                            actionParameter(GOOGLE_CHAT_FEEDBACK_VOTE_PARAMETER, vote.name),
                        ),
                    ),
            ),
        )
    }

    private fun actionParameter(
        key: String,
        value: String,
    ): GoogleAppsCardV1ActionParameter = GoogleAppsCardV1ActionParameter().setKey(key).setValue(value)

    private val FeedbackVote.materialIcon: String
        get() =
            when (this) {
                FeedbackVote.UP -> "thumb_up"
                FeedbackVote.DOWN -> "thumb_down"
            }

    private val FeedbackVote.altText: String
        get() =
            when (this) {
                FeedbackVote.UP -> "Helpful answer"
                FeedbackVote.DOWN -> "Unhelpful answer"
            }
}
