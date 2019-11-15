/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.mongo

import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.newId

/**
 *
 */
@Data(internal = true)
@JacksonData(internal = true)
internal data class ClientIdCol(
    val userIds: Set<String>,
    val _id: Id<ClientIdCol> = newId()
)