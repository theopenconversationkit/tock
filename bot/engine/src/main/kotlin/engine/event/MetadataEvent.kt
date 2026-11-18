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

package ai.tock.bot.engine.event

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.event.MetadataEvent.Companion.STREAM_RESPONSE_METADATA
import java.util.UUID

class MetadataEvent(
    val type: String,
    val value: String,
    applicationId: String,
) : Event(applicationId) {
    companion object {
        fun intent(
            intent: Intent,
            applicationId: String,
        ) = MetadataEvent(INTENT_METADATA, intent.name, applicationId)

        fun responseId(
            uuid: UUID,
            applicationId: String,
        ) = MetadataEvent(RESPONSE_ID_METADATA, uuid.toString(), applicationId)

        fun lastAnswer(applicationId: String) = MetadataEvent(LAST_ANSWER_METADATA, "true", applicationId)

        const val INTENT_METADATA = "INTENT"
        const val RESPONSE_ID_METADATA = "RESPONSE_ID"
        const val LAST_ANSWER_METADATA = "LAST_ANSWER"
        const val STREAM_RESPONSE_METADATA = "TOCK_STREAM_RESPONSE"

        /** Present (`true`) on the transient frames of a RAG answer being generated. */
        const val STREAM_PARTIAL_METADATA = "TOCK_STREAM_PARTIAL"

        /** `actionId` of the final message that replaces the streamed bubble; the other messages are added. */
        const val STREAM_REPLACE_METADATA = "TOCK_STREAM_REPLACE"

        /** Present (`true`) when the streamed bubble must be removed because no final message replaces it. */
        const val STREAM_DISCARD_METADATA = "TOCK_STREAM_DISCARD"

        /** Request identifier sent by the client, echoed on every frame and on the response of that request. */
        const val REQUEST_ID_METADATA = "TOCK_REQUEST_ID"
    }

    fun isEndStreamMetadata(): Boolean = isStreamMetadata() && value != "true"

    fun isStreamMetadata(): Boolean = type == STREAM_RESPONSE_METADATA
}

fun Map<String, String>.hasStreamMetadata(): Boolean = this[STREAM_RESPONSE_METADATA] == "true"
