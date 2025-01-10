/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.engine.action

import ai.tock.genai.orchestratorclient.responses.ObservabilityInfo

data class ActionMetadata(
    /** Is it the last answer of the bot. */
    var lastAnswer: Boolean = false,
    /** Significance deals with the notification level. */
    var priority: ActionPriority = ActionPriority.normal,
    /** Tag deals with type of message notification. */
    var notificationType: ActionNotificationType? = null,
    /** Visibility of the message. */
    var visibility: ActionVisibility = ActionVisibility.UNKNOWN,
    /** Message which is a reply to another. */
    var replyMessage: ActionReply = ActionReply.UNKNOWN,
    /** Message which contains a quote. */
    var quoteMessage: ActionQuote = ActionQuote.UNKNOWN,
    /** The message triggers an orchestration lock.*/
    var orchestrationLock: Boolean = false,
    /** Message delegated by another bot.*/
    val orchestratedBy: String? = null,
    /** Does the action returns history ? **/
    val returnsHistory: Boolean = false,
    /** Is the debugging function enabled? **/
    var debugEnabled: Boolean = false,
    /** Does the action returns source content ? **/
    var sourceWithContent: Boolean = false,
    /** is Gen AI RAG's answer? **/
    var isGenAiRagAnswer: Boolean = false,
    /** is response streamed ? **/
    var streamedResponse: Boolean = false,
    /** ObservabilityInfo **/
    val observabilityInfo: ObservabilityInfo? = null,
)

