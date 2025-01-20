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

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.litote.kmongo.Id
import java.time.Instant


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BotAnnotationEventState::class, name = "STATE"),
    JsonSubTypes.Type(value = BotAnnotationEventGroundTruth::class, name = "GROUND_TRUTH"),
    JsonSubTypes.Type(value = BotAnnotationEventReason::class, name = "REASON"),
    JsonSubTypes.Type(value = BotAnnotationEventDescription::class, name = "DESCRIPTION"),
)
abstract class BotAnnotationEventChange(
    eventId: Id<BotAnnotationEvent>,
    type: BotAnnotationEventType,
    creationDate: Instant,
    lastUpdateDate: Instant,
    user: String,
    open val before: String?,
    open val after: String?
) : BotAnnotationEvent(eventId, type, creationDate, lastUpdateDate, user)
