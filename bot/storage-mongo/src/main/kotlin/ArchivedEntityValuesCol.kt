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

package ai.tock.bot.mongo

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.ArchivedEntityValue
import ai.tock.bot.engine.dialog.EntityStateValue
import ai.tock.bot.engine.dialog.EntityValue
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant

/**
 *
 */
@Data(internal = true)
@JacksonData(internal = true)
internal data class ArchivedEntityValuesCol(
    val _id: Id<ArchivedEntityValuesCol>,
    val values: List<ArchivedEntityValueWrapper>,
    val lastUpdateDate: Instant = Instant.now(),
) {
    constructor(values: List<ArchivedEntityValue>, id: Id<EntityStateValue>?) :
        this(id?.toString()?.toId() ?: newId(), values.map { ArchivedEntityValueWrapper(it) })

    @JacksonData(internal = true)
    internal class ArchivedEntityValueWrapper(
        val entityValue: EntityValue?,
        val actionId: Id<Action>?,
        val date: Instant = Instant.now(),
    ) {
        constructor(value: ArchivedEntityValue) : this(value.entityValue, value.action?.toActionId(), value.date)

        fun toArchivedEntityValue(actionsMap: Map<Id<Action>, Action>): ArchivedEntityValue {
            return ArchivedEntityValue(
                entityValue,
                actionsMap[actionId ?: ""],
                date,
            )
        }
    }
}
