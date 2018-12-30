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

package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.nlp.NlpCallStats
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.jackson.data.JacksonData
import java.time.Instant

@Data(internal = true)
@JacksonData(internal = true)
internal data class NlpStatsColId(val actionId: Id<Action>, val dialogId: Id<Dialog>)

/**
 *
 */
@Data(internal = true)
@JacksonData(internal = true)
internal data class NlpStatsCol(
    val _id: NlpStatsColId,
    val stats: NlpCallStats,
    val appNamespace: String,
    val date: Instant = Instant.now()
)