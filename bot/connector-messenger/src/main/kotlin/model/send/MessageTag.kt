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
    SHIPPING_UPDATE,
    RESERVATION_UPDATE,
    ISSUE_RESOLUTION,
    APPOINTMENT_UPDATE,
    GAME_EVENT,
    TRANSPORTATION_UPDATE,
    FEATURE_FUNCTIONALITY_UPDATE,
    TICKET_UPDATE,
    ACCOUNT_UPDATE,
    PAYMENT_UPDATE,
    PERSONAL_FINANCE_UPDATE;

    companion object {
        fun toMessageTag(action: Action): MessageTag? {
            return when (action.metadata.notificationType) {
                ActionNotificationType.transportationUpdate -> TRANSPORTATION_UPDATE
                ActionNotificationType.issueResolution -> ISSUE_RESOLUTION
                ActionNotificationType.newFeatureFunctionality -> FEATURE_FUNCTIONALITY_UPDATE
                ActionNotificationType.reservationUpdate -> RESERVATION_UPDATE
                ActionNotificationType.accountUpdate -> ACCOUNT_UPDATE
                ActionNotificationType.paymentUpdate -> PAYMENT_UPDATE
                ActionNotificationType.personalFinanceUpdate -> PERSONAL_FINANCE_UPDATE
                else -> null
            }
        }
    }
}