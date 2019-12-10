/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
    @Deprecated("Available until January 15th, 2020 Partially covered as the new POST_PURCHASE_UPDATE")
    SHIPPING_UPDATE,
    @Deprecated("Available until January 15th, 2020 Partially covered as the new tag POST_PURCHASE_UPDATE")
    RESERVATION_UPDATE,
    @Deprecated("Available until January 15th, 2020 Partially covered as the new tag HUMAN_AGENT")
    ISSUE_RESOLUTION,
    @Deprecated("Available until January 15th, 2020 Partially covered as the new tag CONFIRMED_EVENT_UPDATE")
    APPOINTMENT_UPDATE,
    @Deprecated("No longer supported after January 15, 2020")
    GAME_EVENT,
    @Deprecated("Available until January 15th, 2020 Partially covered as the new tag CONFIRMED_EVENT_UPDATE")
    TRANSPORTATION_UPDATE,
    @Deprecated("Available until January 15th, 2020 Partially covered as the new tag HUMAN_AGENT")
    FEATURE_FUNCTIONALITY_UPDATE,
    @Deprecated("Available until January 15th, 2020 Partially covered as the new tag CONFIRMED_EVENT_UPDATE")
    TICKET_UPDATE,
    @Deprecated("Available until January 15th, 2020 Partially covered as the new tag POST_PURCHASE_UPDATE")
    PAYMENT_UPDATE,
    @Deprecated("Available until January 15th, 2020 Partially covered as the new tag ACCOUNT_UPDATE")
    PERSONAL_FINANCE_UPDATE,

    CONFIRMED_EVENT_UPDATE,
    POST_PURCHASE_UPDATE,
    ACCOUNT_UPDATE,
    HUMAN_AGENT;

    companion object {
        fun toMessageTag(action: Action): MessageTag? {
            return when (action.metadata.notificationType) {
                ActionNotificationType.transportationUpdate -> TRANSPORTATION_UPDATE
                ActionNotificationType.issueResolution -> ISSUE_RESOLUTION
                ActionNotificationType.newFeatureFunctionality -> FEATURE_FUNCTIONALITY_UPDATE
                ActionNotificationType.reservationUpdate -> RESERVATION_UPDATE
                ActionNotificationType.paymentUpdate -> PAYMENT_UPDATE
                ActionNotificationType.personalFinanceUpdate -> PERSONAL_FINANCE_UPDATE
                ActionNotificationType.confirmedEventUpdate -> CONFIRMED_EVENT_UPDATE
                ActionNotificationType.humanAgent -> HUMAN_AGENT
                ActionNotificationType.postPurchaseUpdate -> POST_PURCHASE_UPDATE
                ActionNotificationType.accountUpdate -> ACCOUNT_UPDATE
                else -> null
            }
        }
    }
}