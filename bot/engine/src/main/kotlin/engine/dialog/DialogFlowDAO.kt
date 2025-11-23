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

package ai.tock.bot.engine.dialog

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.dialog.ApplicationDialogFlowData
import ai.tock.bot.admin.dialog.DialogFlowAggregateData
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.DialogFlowDefinition
import org.litote.kmongo.Id
import java.time.DayOfWeek
import java.time.LocalDateTime

interface DialogFlowDAO {
    /**
     * Init stat craw - used only in admin by default.
     */
    fun initFlowStatCrawl()

    fun saveFlow(
        bot: BotDefinition,
        flow: DialogFlowDefinition,
    )

    fun loadApplicationData(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
        intent: String? = null,
    ): ApplicationDialogFlowData

    fun countMessagesByDate(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>>

    /**
     * Counts the number of users per day over a given period of time.
     *
     * In the returned map, the keys represent dates using the "YYYY-MM-DD" format.
     * Values are singleton lists containing the data point for that day.
     *
     * @return a [Map] of user counts indexed by days
     */
    fun countUsersByDate(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>>

    /**
     * Counts the number of messages per day and connector type over a given period of time.
     *
     * In the returned map, the keys represent dates using the "YYYY-MM-DD" format.
     * Values are lists containing one data point for each represented connector type (web, messenger, etc.).
     *
     * @return a [Map] of message counts per connector type, indexed by days
     */
    fun countMessagesByDateAndConnectorType(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>>

    /**
     * Counts the number of messages per day and bot configuration over a given period of time.
     *
     * In the returned map, the keys represent dates using the "YYYY-MM-DD" format.
     * Values are lists containing one data point for each represented bot configuration.
     *
     * @return a [Map] of message counts per configuration, indexed by days
     */
    fun countMessagesByDateAndConfiguration(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>>

    /**
     * Counts the total number of messages sent on each day-of-week.
     *
     * @return a [Map] of message counts, indexed by day-of-week
     */
    fun countMessagesByDayOfWeek(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<DayOfWeek, Int>

    /**
     * Counts the total number of messages sent on each hour of the day over a given period of time.
     *
     * @return a [Map] of message counts, indexed by hour (from 0 to 23)
     */
    fun countMessagesByHour(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<Int, Int>

    /**
     * Counts the number of messages per day and intent over a given period of time.
     *
     * In the returned map, the keys represent dates using the "YYYY-MM-DD" format.
     * Values are lists containing one data point for each represented intent.
     *
     * @return a [Map] of message counts per intent, indexed by day
     */
    fun countMessagesByDateAndIntent(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>>

    /**
     * Counts the total number of messages sent for each intent over a given period of time.
     *
     * @return a [Map] of message counts, indexed by intent name
     */
    fun countMessagesByIntent(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int>

    /**
     * Counts the total number of messages sent for each story over a given period of time.
     *
     * @return a [Map] of message counts, indexed by story name
     */
    fun countMessagesByStory(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int>

    /**
     * Counts the number of messages per day and story over a given period of time.
     *
     * In the returned map, the keys represent dates using the "YYYY-MM-DD" format.
     * Values are lists containing one data point for each represented intent.
     *
     * @return a [Map] of message counts per intent, indexed by day
     */
    fun countMessagesByDateAndStory(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, List<DialogFlowAggregateData>>

    /**
     * Counts the total number of messages sent for each story category over a given period of time.
     *
     * @return a [Map] of message counts, indexed by story category
     */
    fun countMessagesByStoryCategory(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int>

    /**
     * Counts the total number of messages sent for each story category over a given period of time.
     *
     * @return a [Map] of message counts, indexed by story type
     */
    fun countMessagesByStoryType(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int>

    /**
     * Counts the total number of messages sent for each story locale over a given period of time.
     *
     * @return a [Map] of message counts, indexed by story locale
     */
    fun countMessagesByStoryLocale(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int>

    /**
     * Counts the total number of messages sent for each action type over a given period of time.
     *
     * @return a [Map] of message counts, indexed by action type
     */
    fun countMessagesByActionType(
        namespace: String,
        botId: String,
        applicationIds: Set<Id<BotApplicationConfiguration>>,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): Map<String, Int>
}
