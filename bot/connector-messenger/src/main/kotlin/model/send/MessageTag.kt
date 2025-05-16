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

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionNotificationType

enum class MessageTag {

    CONFIRMED_EVENT_UPDATE,
    POST_PURCHASE_UPDATE,
    ACCOUNT_UPDATE,
    HUMAN_AGENT;

    companion object {
        fun toMessageTag(action: Action): MessageTag? {
            return when (action.metadata.notificationType) {
                ActionNotificationType.confirmedEventUpdate -> CONFIRMED_EVENT_UPDATE
                ActionNotificationType.humanAgent -> HUMAN_AGENT
                ActionNotificationType.postPurchaseUpdate -> POST_PURCHASE_UPDATE
                ActionNotificationType.accountUpdate -> ACCOUNT_UPDATE
                else -> null
            }
        }
    }
}
