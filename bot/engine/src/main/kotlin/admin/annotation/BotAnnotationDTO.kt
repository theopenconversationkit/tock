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

package ai.tock.bot.admin.annotation

import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant

data class BotAnnotationDTO(
    val id: String? = null,
    val dialogId: String,
    val actionId: String,
    val state: BotAnnotationState,
    val reason: BotAnnotationReasonType? = null,
    val description: String,
    val groundTruth: String? = null,
    val events: List<BotAnnotationEventDTO> = emptyList()
) {
    constructor(annotation: BotAnnotation) : this(
        id = annotation._id.toString(),
        dialogId = annotation.dialogId,
        actionId = annotation.actionId,
        state = annotation.state,
        reason = annotation.reason,
        description = annotation.description,
        groundTruth = annotation.groundTruth,
        events = annotation.events.map { event ->
            when (event) {
                is BotAnnotationEventComment -> BotAnnotationEventDTO(
                    type = event.type,
                    comment = event.comment,
                    before = null,
                    after = null
                )
                is BotAnnotationEventChange -> BotAnnotationEventDTO(
                    type = event.type,
                    comment = null,
                    before = event.before,
                    after = event.after
                )
                else -> throw IllegalArgumentException("Unknown event type: ${event::class}")
            }
        }
    )
}