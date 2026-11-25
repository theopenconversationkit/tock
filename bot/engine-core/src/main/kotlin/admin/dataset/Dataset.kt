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

package ai.tock.bot.admin.dataset

import ai.tock.shared.Dice
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

data class DatasetQuestion(
    val id: String = Dice.newId(),
    val question: String,
    val groundTruth: String? = null,
)

data class Dataset(
    val _id: Id<Dataset> = newId(),
    val namespace: String,
    val botId: String,
    val name: String,
    val description: String,
    val questions: List<DatasetQuestion>,
    val createdAt: Instant,
    val createdBy: String,
    val updatedAt: Instant? = null,
    val updatedBy: String? = null,
)
