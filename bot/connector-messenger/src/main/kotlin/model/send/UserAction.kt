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

/**
 * Generic interface for messenger user action ( [Button] or [QuickReply] ).
 */
interface UserAction {

    companion object {
        fun extractQuickReplies(userActions: List<UserAction>): List<QuickReply>? {
            return userActions
                .filter { it is QuickReply }
                .map { it as QuickReply }
                .run {
                    if (isEmpty()) null
                    // 11 quick replies max cf https://developers.facebook.com/docs/messenger-platform/send-api-reference/quick-replies
                    else if (size > 11) error("more than 11 quick replies : $this")
                    else this
                }
        }

        fun extractButtons(userActions: List<UserAction>): List<Button> {
            return userActions
                .filter { it is Button }
                .map { it as Button }
        }
    }
}
