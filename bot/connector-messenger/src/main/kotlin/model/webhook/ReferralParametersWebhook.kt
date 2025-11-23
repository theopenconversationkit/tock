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

package ai.tock.bot.connector.messenger.model.webhook

import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.Sender
import ai.tock.bot.connector.messenger.model.send.ReferralIdentifierType
import ai.tock.bot.connector.messenger.model.send.SourceType
import ai.tock.bot.connector.messenger.model.send.SourceType.SHORTLINK

internal data class ReferralParametersWebhook(
    override val sender: Sender,
    override val recipient: Recipient,
    override val timestamp: Long,
    val referral: Referral,
) : Webhook()

data class Referral(
    val ref: String = "",
    val source: SourceType = SHORTLINK,
    val type: ReferralIdentifierType = ReferralIdentifierType.OPEN_THREAD,
)
