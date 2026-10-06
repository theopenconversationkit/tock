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

package ai.tock.bot.connector.web.sse.channel

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.service.RedactionResult
import ai.tock.shared.service.UserDataRedactionProvider

class SseChannelsRedactionProvider : UserDataRedactionProvider {
    private val channels: SseChannels get() = injector.provide()

    override val name: String = "web_sse_messages"

    override suspend fun migrateUserId(
        namespace: String,
        oldUserId: String,
        newUserId: String,
    ) = RedactionResult(channels.migrate(appId = null, oldUserId, newUserId))

    override suspend fun deleteByUserId(
        namespace: String,
        userId: String,
    ) = RedactionResult(channels.deletePersistedEvents(userId))
}
