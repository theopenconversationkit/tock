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
package ai.tock.bot.connector.web.channel

import ai.tock.bot.connector.web.WebConnectorResponse
import com.fasterxml.jackson.annotation.JsonValue
import io.vertx.core.Future
import java.time.Instant
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 *  Event that will be retrieved by the bot and sent to the recipient if SSE is activated
 */
internal data class ChannelEvent(
    val appId: String = "unknown",
    val recipientId: String,
    val webConnectorResponse: WebConnectorResponse,
    val status: Status = Status.ENQUEUED,
    val enqueuedAt: Instant = Instant.now(),
    val _id: Id<ChannelEvent> = newId(),
) {
    enum class Status(@JsonValue val id: Int) {
        /* Capped MongoDB collections cannot update a document's size after insertion.
           Therefore, we have to serialize this enum as a fixed-size value */
        ENQUEUED(0), PROCESSED(1)
    }

    fun interface Handler {
        /**
         * @return `true` if the event has been handled successfully
         */
        operator fun invoke(event: ChannelEvent): Future<Boolean>
    }
}
