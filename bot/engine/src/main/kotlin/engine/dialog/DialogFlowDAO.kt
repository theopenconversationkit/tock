/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.bot.engine.dialog

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.dialog.ApplicationDialogFlowData
import ai.tock.bot.admin.dialog.DialogFlowTransitionStatsData
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.DialogFlowDefinition
import org.litote.kmongo.Id
import java.time.ZonedDateTime

interface DialogFlowDAO {

    fun saveFlow(bot: BotDefinition, flow: DialogFlowDefinition)

    fun loadApplicationData(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: ZonedDateTime?,
        to: ZonedDateTime?,
        intent: String? = null
        ): ApplicationDialogFlowData

    fun search(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: ZonedDateTime?,
        to: ZonedDateTime?,
        intent: String? = null
    ): List<DialogFlowTransitionStatsData>

    fun searchByDateWithIntent(
            namespace: String,
            botId: String,
            applicationIds: Set<Id<BotApplicationConfiguration>>,
            from: ZonedDateTime?,
            to: ZonedDateTime?,
            intent: String? = null
    ): Pair<List<DialogFlowTransitionStatsData>, List<String>>

    fun searchByDateWithActionType(
            namespace: String,
            botId: String,
            applicationIds: Set<Id<BotApplicationConfiguration>>,
            from: ZonedDateTime?,
            to: ZonedDateTime?,
            intent: String? = null
    ): Pair<List<DialogFlowTransitionStatsData>, List<String>>

    fun searchByDateWithStory(
            namespace: String,
            botId: String,
            applicationIds: Set<Id<BotApplicationConfiguration>>,
            from: ZonedDateTime?,
            to: ZonedDateTime?,
            intent: String? = null
    ): Pair<List<DialogFlowTransitionStatsData>, List<String>>

}