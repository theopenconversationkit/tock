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

package ai.tock.bot.admin

import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByActionType
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByDate
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByDateAndConfiguration
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByDateAndConnectorType
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByDateAndIntent
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByDateAndStory
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByDayOfWeek
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByHour
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByIntent
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByStory
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByStoryCategory
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByStoryLocale
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countMessagesByStoryType
import ai.tock.bot.admin.BotAdminAnalyticsService.Operation.countUsersByDate
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.dialog.DialogFlowAggregateData
import ai.tock.bot.admin.model.DialogFlowRequest
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.user.UserAnalyticsQueryResult
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.shared.defaultLocale
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.github.salomonbrys.kodein.instance
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle.FULL_STANDALONE
import java.time.temporal.ChronoUnit.HOURS
import java.util.stream.LongStream
import java.util.stream.Stream

object BotAdminAnalyticsService {
    private val applicationConfigurationDAO: BotApplicationConfigurationDAO by injector.instance()
    private val dialogFlowDAO: DialogFlowDAO get() = injector.provide()
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO by injector.instance()

    private val requestCache =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(1))
            .build(
                object : CacheLoader<RequestCacheKey, UserAnalyticsQueryResult>() {
                    override fun load(key: RequestCacheKey): UserAnalyticsQueryResult = key.operation.loader(key)
                },
            )
    private val storyConfigurationIdNameCache =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(1))
            .build(
                object : CacheLoader<String, String>() {
                    override fun load(key: String): String =
                        if (ObjectId.isValid(key)) {
                            storyDefinitionDAO.getStoryDefinitionById(key.toId())?.name ?: key
                        } else {
                            key
                        }
                },
            )

    @Suppress("ktlint:standard:enum-entry-name-case")
    private enum class Operation(
        val dialogFlowMethod: (
            DialogFlowDAO.(
                namespace: String,
                botId: String,
                applicationIds: Set<Id<BotApplicationConfiguration>>,
                from: LocalDateTime?,
                to: LocalDateTime?,
            ) -> Map<String, List<DialogFlowAggregateData>>
        )? = null,
        val loader: (RequestCacheKey) -> UserAnalyticsQueryResult = {
            reportAnalytics(
                it.request,
                it.operation.dialogFlowMethod ?: error("loader must be set if not dialogFlowMethod"),
            )
        },
    ) {
        countMessagesByDate(DialogFlowDAO::countMessagesByDate),
        countUsersByDate(DialogFlowDAO::countUsersByDate),
        countMessagesByDateAndConnectorType(DialogFlowDAO::countMessagesByDateAndConnectorType),
        countMessagesByDateAndConfiguration(DialogFlowDAO::countMessagesByDateAndConfiguration),
        countMessagesByDateAndIntent(DialogFlowDAO::countMessagesByDateAndIntent),
        countMessagesByDateAndStory(DialogFlowDAO::countMessagesByDateAndStory),
        countMessagesByDayOfWeek(loader = { (request, _) ->
            val namespace = request.namespace
            val botId = request.botId
            val applicationIds = loadApplications(request).map { it._id }
            val usersData =
                dialogFlowDAO.countMessagesByDayOfWeek(
                    namespace,
                    botId,
                    applicationIds.toSet(),
                    request.from,
                    request.to,
                )
            UserAnalyticsQueryResult(
                DayOfWeek.values().map { usersData[it] ?: 0 },
                DayOfWeek.values().map { it.getDisplayName(FULL_STANDALONE, defaultLocale) },
            )
        }),
        countMessagesByHour(loader = { (request, _) ->
            val namespace = request.namespace
            val botId = request.botId
            val applicationIds = loadApplications(request).map { it._id }
            val usersData =
                dialogFlowDAO.countMessagesByHour(namespace, botId, applicationIds.toSet(), request.from, request.to)
            UserAnalyticsQueryResult(
                (0..23).map { usersData[it] ?: 0 },
                (0..23).map { "${it}h" },
            )
        }),
        countMessagesByIntent(loader = { (request, _) ->
            val namespace = request.namespace
            val botId = request.botId
            val applicationIds = loadApplications(request).map { it._id }
            val (series, data) =
                dialogFlowDAO.countMessagesByIntent(
                    namespace,
                    botId,
                    applicationIds.toSet(),
                    request.from,
                    request.to,
                ).toList().unzip()
            UserAnalyticsQueryResult(data, series)
        }),
        countMessagesByStory(loader = { (request, _) ->
            val namespace = request.namespace
            val botId = request.botId
            val applicationIds = loadApplications(request).map { it._id }
            val (series, data) =
                dialogFlowDAO.countMessagesByStory(
                    namespace,
                    botId,
                    applicationIds.toSet(),
                    request.from,
                    request.to,
                ).toList().unzip()
            UserAnalyticsQueryResult(data, series.map { storyConfigurationIdNameCache.get(it) })
        }),
        countMessagesByStoryCategory(loader = { (request, _) ->
            val namespace = request.namespace
            val botId = request.botId
            val applicationIds = loadApplications(request).map { it._id }
            val (series, data) =
                dialogFlowDAO.countMessagesByStoryCategory(
                    namespace,
                    botId,
                    applicationIds.toSet(),
                    request.from,
                    request.to,
                ).toList().unzip()
            UserAnalyticsQueryResult(data, series)
        }),
        countMessagesByStoryType(loader = { (request, _) ->
            val namespace = request.namespace
            val botId = request.botId
            val applicationIds = loadApplications(request).map { it._id }
            val (series, data) =
                dialogFlowDAO.countMessagesByStoryType(
                    namespace,
                    botId,
                    applicationIds.toSet(),
                    request.from,
                    request.to,
                ).toList().unzip()
            UserAnalyticsQueryResult(data, series)
        }),
        countMessagesByStoryLocale(loader = { (request, _) ->
            val namespace = request.namespace
            val botId = request.botId
            val applicationIds = loadApplications(request).map { it._id }
            val (series, data) =
                dialogFlowDAO.countMessagesByStoryLocale(
                    namespace,
                    botId,
                    applicationIds.toSet(),
                    request.from,
                    request.to,
                ).toList().unzip()
            UserAnalyticsQueryResult(data, series)
        }),
        countMessagesByActionType(loader = { (request, _) ->
            val namespace = request.namespace
            val botId = request.botId
            val applicationIds = loadApplications(request).map { it._id }
            val (series, data) =
                dialogFlowDAO.countMessagesByActionType(
                    namespace,
                    botId,
                    applicationIds.toSet(),
                    request.from,
                    request.to,
                ).toList().unzip()
            val usedIntents =
                dialogFlowDAO.countMessagesByIntent(
                    namespace,
                    botId,
                    applicationIds.toSet(),
                    request.from,
                    request.to,
                ).keys.toList()
            UserAnalyticsQueryResult(data, series, usedIntents)
        }),
    }

    private data class RequestCacheKey(val request: DialogFlowRequest, val operation: Operation) {
        override fun equals(other: Any?): Boolean =
            if (other is RequestCacheKey) {
                operation == other.operation &&
                    request.copy(
                        from = request.from?.truncatedTo(HOURS),
                        to = request.to?.truncatedTo(HOURS),
                    ) ==
                    other.request.copy(
                        from = other.request.from?.truncatedTo(HOURS),
                        to = other.request.to?.truncatedTo(HOURS),
                    )
            } else {
                false
            }

        override fun hashCode(): Int = operation.hashCode()
    }

    fun reportMessagesByType(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByDate))

    fun reportUsersByType(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countUsersByDate))

    fun countMessagesByConnectorType(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByDateAndConnectorType))

    fun reportMessagesByConfiguration(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByDateAndConfiguration))

    fun reportMessagesByConnectorType(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByDateAndConnectorType))

    fun reportMessagesByDateAndIntent(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByDateAndIntent))

    fun reportMessagesByDateAndStory(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByDateAndStory))

    fun reportMessagesByDayOfWeek(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByDayOfWeek))

    fun reportMessagesByHour(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByHour))

    fun reportMessagesByIntent(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByIntent))

    fun reportMessagesByStory(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByStory))

    fun reportMessagesByStoryCategory(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByStoryCategory))

    fun reportMessagesByStoryType(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByStoryType))

    fun reportMessagesByStoryLocale(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByStoryLocale))

    fun reportMessagesByActionType(request: DialogFlowRequest): UserAnalyticsQueryResult = requestCache.get(RequestCacheKey(request, countMessagesByActionType))

    private fun reportAnalytics(
        request: DialogFlowRequest,
        method: DialogFlowDAO.(
            namespace: String,
            botId: String,
            applicationIds: Set<Id<BotApplicationConfiguration>>,
            from: LocalDateTime?,
            to: LocalDateTime?,
        ) -> Map<String, List<DialogFlowAggregateData>>,
    ): UserAnalyticsQueryResult {
        val namespace = request.namespace
        val botId = request.botId
        val applicationIds = loadApplications(request).map { it._id }
        val from = request.from ?: LocalDateTime.now()
        val to = request.to ?: from
        val fromDate = atTimeOfDay(from, LocalTime.MIN)
        val toDate = atTimeOfDay(to, LocalTime.MAX)
        val usersData = dialogFlowDAO.method(namespace, botId, applicationIds.toSet(), from, to)
        return prepareAnalyticsResponse(fromDate, toDate, usersData)
    }

    private fun prepareAnalyticsResponse(
        fromDate: LocalDateTime,
        toDate: LocalDateTime,
        queryResult: Map<String, List<DialogFlowAggregateData>>,
    ): UserAnalyticsQueryResult {
        val series: List<String> = collectAnalyticsSeries(queryResult)
        val datesBetween = getDatesBetween(fromDate.toLocalDate(), toDate.toLocalDate())
        val emptySeries = (0..(maxOf(1, series.size))).map { 0 }
        val usersAnalytics =
            datesBetween.map { date ->
                queryResult[date]?.run {
                    series.map { s -> find { it.seriesKey == s }?.count ?: 0 }
                } ?: emptySeries
            }
        return UserAnalyticsQueryResult(
            datesBetween,
            usersAnalytics,
            series.map { storyConfigurationIdNameCache.get(it) },
        )
    }

    private fun loadApplications(request: DialogFlowRequest): Set<BotApplicationConfiguration> {
        val namespace = request.namespace
        val botId = request.botId
        val configurationName = request.botConfigurationName
        val tests = request.includeTestConfigurations
        return if (request.botConfigurationId != null) {
            if (tests && configurationName != null) {
                val configurations =
                    applicationConfigurationDAO.getConfigurationsByBotNamespaceAndConfigurationName(
                        namespace = namespace,
                        botId = botId,
                        configurationName = configurationName,
                    )
                val actualConfiguration = configurations.find { it._id == request.botConfigurationId }
                val testConfiguration =
                    configurations.find { it.applicationId == "test-${actualConfiguration?.applicationId}" }
                listOfNotNull(actualConfiguration, testConfiguration).toSet()
            } else {
                listOfNotNull(applicationConfigurationDAO.getConfigurationById(request.botConfigurationId)).toSet()
            }
        } else if (configurationName != null) {
            applicationConfigurationDAO
                .getConfigurationsByBotNamespaceAndConfigurationName(namespace, botId, configurationName)
                .filter { tests || it.connectorType != ConnectorType.rest }
                .toSet()
        } else {
            applicationConfigurationDAO
                .getConfigurationsByNamespaceAndBotId(namespace, botId)
                .filter { tests || it.connectorType != ConnectorType.rest }
                .toSet()
        }
    }

    private fun atTimeOfDay(
        date: LocalDateTime,
        time: LocalTime,
    ): LocalDateTime = LocalDateTime.of(date.toLocalDate(), time)

    private fun LocalDate.datesUntil(endExclusive: LocalDate): Stream<LocalDate> {
        val end = endExclusive.toEpochDay()
        val start: Long = toEpochDay()
        require(end >= start) { "$endExclusive < $this" }
        return LongStream.range(start, end).mapToObj { epochDay: Long ->
            LocalDate.ofEpochDay(epochDay)
        }
    }

    private fun getDatesBetween(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<String> {
        return startDate.datesUntil(endDate.plusDays(1))
            .map { it.toString() }.toList()
    }

    private fun collectAnalyticsSeries(queryResult: Map<String, List<DialogFlowAggregateData>>): List<String> {
        val series =
            queryResult.values
                .asSequence()
                .flatten()
                .map { it.seriesKey }
                .associateWith { 0 }
                .toMutableMap()
        queryResult.values.forEach { results ->
            results.forEach { r ->
                series.compute(r.seriesKey) { _, u -> (u ?: 0) + r.count }
            }
        }
        return series.toList().sortedByDescending { it.second }.map { it.first }
    }
}
