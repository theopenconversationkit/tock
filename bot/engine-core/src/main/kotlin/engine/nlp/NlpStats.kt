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

package ai.tock.bot.engine.nlp

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import org.litote.kmongo.Id
import java.time.Instant

/**
 * Stats about nlp call.
 */
data class NlpStats(
    val dialogId: Id<Dialog>,
    val actionId: Id<Action>,
    val stats: NlpCallStats,
    val appNamespace: String,
    val date: Instant,
)
