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

package ai.tock.bot.admin.model.annotation


import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

data class Annotation(
    val _id: Id<Annotation> = newId(),
    val actionId: String,
    val dialogId: String,
    var state: AnnotationState,
    var reason: AnnotationReasonType?,
    var description: String,
    var groundTruth: String?,
    val events: MutableList<AnnotationEvent>,
    val createdAt: Instant = Instant.now(),
    var lastUpdateDate: Instant = Instant.now(),
)