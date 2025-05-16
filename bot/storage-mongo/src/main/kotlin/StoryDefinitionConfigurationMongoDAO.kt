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

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.AnswerConfigurationType.builtin
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationExtendedSummaryRequest
import ai.tock.bot.admin.story.StoryDefinitionConfigurationMinimalSummaryRequest
import ai.tock.bot.admin.story.StoryDefinitionConfigurationSummaryExtended
import ai.tock.bot.admin.story.StoryDefinitionConfigurationSummaryMinimumMetrics
import ai.tock.bot.admin.story.StoryDefinitionConfigurationSummaryRequest
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_.Companion.BotId
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_.Companion.Category
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_.Companion.CurrentType
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_.Companion.Intent
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_.Companion.Name
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_.Companion.Namespace
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_.Companion.StoryId
import ai.tock.bot.admin.story.StoryDefinitionConfiguration_.Companion.Tags
import ai.tock.bot.definition.StoryTag
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.MongoBotConfiguration.database
import ai.tock.bot.mongo.StoryDefinitionConfigurationHistoryCol_.Companion.Conf
import ai.tock.bot.mongo.StoryDefinitionConfigurationHistoryCol_.Companion.Date
import ai.tock.shared.allowDiacriticsInRegexp
import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultZoneId
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.error
import ai.tock.shared.safeCollation
import ai.tock.shared.trace
import ai.tock.shared.warn
import ai.tock.shared.watch
import com.mongodb.client.model.Collation
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.exists
import com.mongodb.client.model.Filters
import mu.KotlinLogging
import org.bson.conversions.Bson
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.aggregate
import org.litote.kmongo.ascending
import org.litote.kmongo.contains
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.document
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.from
import org.litote.kmongo.getCollection
import org.litote.kmongo.getCollectionOfName
import org.litote.kmongo.group
import org.litote.kmongo.`in`
import org.litote.kmongo.match
import org.litote.kmongo.max
import org.litote.kmongo.ne
import org.litote.kmongo.or
import org.litote.kmongo.projection
import org.litote.kmongo.reactivestreams.getCollectionOfName
import org.litote.kmongo.regex
import org.litote.kmongo.save
import org.litote.kmongo.withDocumentClass
import java.time.Instant
import java.time.ZonedDateTime


/**
 *
 */
internal object StoryDefinitionConfigurationMongoDAO : StoryDefinitionConfigurationDAO {

    private val logger = KotlinLogging.logger {}
    private const val LIMIT_MAX_WORDS = 10

    @Data(internal = true)
    @JacksonData(internal = true)
    data class StoryDefinitionConfigurationHistoryCol(
        val conf: StoryDefinitionConfiguration,
        val deleted: Boolean = false,
        val date: Instant = Instant.now()
    )

    val col = database.getCollectionOfName<StoryDefinitionConfiguration>("story_configuration")
    private val asyncCol = asyncDatabase.getCollectionOfName<StoryDefinitionConfiguration>("story_configuration")
    private val historyCol =
        database.getCollection<StoryDefinitionConfigurationHistoryCol>("story_configuration_history")

    init {
        try {
            col.ensureIndex(Namespace, BotId)
            historyCol.ensureIndex(Date)
            historyCol.ensureIndex(Conf.namespace, Conf.storyId, Date)
            try {
                col.ensureUniqueIndex(Namespace, BotId, Intent.name_)
            } catch (e: Exception) {
                logger.warn(e)
                //there is a misleading data state when creating index
                logger.warn("try to remove builtin stories and set the index")
                try {
                    col.deleteMany(CurrentType eq builtin)
                    col.ensureUniqueIndex(Namespace, BotId, Intent.name_)
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun listenChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun getStoryDefinitionById(id: Id<StoryDefinitionConfiguration>): StoryDefinitionConfiguration? {
        return col.findOneById(id)
    }

    override fun getStoryDefinitionsByNamespaceAndBotIdWithFileAttached(
        namespace: String,
        botId: String
    ): List<StoryDefinitionConfiguration> {
        return col.find(
            Namespace eq namespace, BotId eq botId,
            exists("answers.answers.mediaMessage.file.id", true)
        ).toList()
    }

    override fun getRuntimeStorySettings(namespace: String, botId: String): List<StoryDefinitionConfiguration> {
        return col.find(
            and(
                Namespace eq namespace,
                BotId eq botId,
                or(StoryTag.values().map { Tags contains it })
            )
        ).toList()
    }

    override fun getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
        namespace: String,
        botId: String,
        intent: String
    ): StoryDefinitionConfiguration? {
        return col.findOne(
            Namespace eq namespace,
            BotId eq botId,
            CurrentType ne AnswerConfigurationType.builtin,
            Intent.name_ eq intent
        )
    }

    override fun getConfiguredStoriesDefinitionByNamespaceAndBotIdAndIntent(
        namespace: String,
        botId: String,
        intentNames: List<String>
    ): List<StoryDefinitionConfiguration> {
        return col.find(
            Namespace eq namespace,
            BotId eq botId,
            CurrentType ne AnswerConfigurationType.builtin,
            Intent.name_ `in` (intentNames.asIterable())
        ).toList()
    }

    override fun getStoryDefinitionByNamespaceAndBotIdAndIntent(
        namespace: String,
        botId: String,
        intent: String
    ): StoryDefinitionConfiguration? {
        return col.findOne(Namespace eq namespace, BotId eq botId, Intent.name_ eq intent)
    }

    override fun getStoryDefinitionByNamespaceAndBotIdAndStoryId(
        namespace: String,
        botId: String,
        storyId: String
    ): StoryDefinitionConfiguration? {
        return col.findOne(Namespace eq namespace, BotId eq botId, StoryId eq storyId)
    }

    override fun getStoryDefinitionsByNamespaceAndBotId(
        namespace: String,
        botId: String
    ): List<StoryDefinitionConfiguration> {
        return col.find(and(Namespace eq namespace, BotId eq botId)).toList()
    }

    fun customRegexToFindWord(textSearch: String) = if (textSearch.trim().isEmpty()) {
        ""
    } else {
        textSearch
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .joinToString("", "^", "\$", LIMIT_MAX_WORDS) { wordTextSearch ->
                "(.*?(${allowDiacriticsInRegexp(wordTextSearch)})[^\$]*)"
            }
    }

    override fun searchStoryDefinitionSummariesExtended(request: StoryDefinitionConfigurationExtendedSummaryRequest): List<StoryDefinitionConfigurationSummaryExtended> {
        //get last date from history
        val dateById = historyCol
            .aggregate<DateProjection>(
                match(Conf.namespace eq request.namespace),
                group(
                    document(Namespace from Conf.namespace, StoryId from Conf.storyId),
                    Date.max(Date)
                )
            )
            .toList()
            .associateBy({ it._id.storyId }) { it.date.withZoneSameInstant(defaultZoneId) }


        return col.withDocumentClass<StoryDefinitionConfigurationSummaryExtended>()
            .find(
                //default list of var args Bson
                *filterStoryDefinitionSummaries(request)
                    // specific filters for extended
                    .plusElement(
                        request.textSearch?.takeUnless { it.isBlank() }
                            ?.let { Name.regex(customRegexToFindWord(request.textSearch ?: ""), "i") }
                    )
                    .plusElement(
                        if (request.onlyConfiguredStory) CurrentType ne AnswerConfigurationType.builtin else null
                    )
            )
            .projection(
                StoryDefinitionConfigurationSummaryExtended::_id,
                StoryDefinitionConfigurationSummaryExtended::storyId,
                StoryDefinitionConfigurationSummaryExtended::botId,
                StoryDefinitionConfigurationSummaryExtended::intent,
                StoryDefinitionConfigurationSummaryExtended::currentType,
                StoryDefinitionConfigurationSummaryExtended::name,
                StoryDefinitionConfigurationSummaryExtended::category,
                StoryDefinitionConfigurationSummaryExtended::description
            )
            .safeCollation(Collation.builder().locale(defaultLocale.language).build())
            .sort(ascending(StoryDefinitionConfigurationSummaryExtended::name))
            .map { it.copy(lastEdited = dateById[it.storyId]) }
            .toList()
    }

    override fun searchStoryDefinitionSummaries(request: StoryDefinitionConfigurationMinimalSummaryRequest): List<StoryDefinitionConfigurationSummaryMinimumMetrics> {
        val data = filterStoryDefinitionSummaries(request)
        return col.withDocumentClass<StoryDefinitionConfigurationSummaryMinimumMetrics>()
            .find(*data)
            .projection(
                StoryDefinitionConfigurationSummaryMinimumMetrics::_id,
                StoryDefinitionConfigurationSummaryMinimumMetrics::storyId,
                StoryDefinitionConfigurationSummaryMinimumMetrics::intent,
                StoryDefinitionConfigurationSummaryMinimumMetrics::currentType,
                StoryDefinitionConfigurationSummaryMinimumMetrics::name,
                StoryDefinitionConfigurationSummaryMinimumMetrics::category,
                StoryDefinitionConfigurationSummaryMinimumMetrics::metricStory
            )
            .safeCollation(Collation.builder().locale(defaultLocale.language).build())
            .sort(ascending(StoryDefinitionConfigurationSummaryMinimumMetrics::name))
            .toList()
    }

    /**
     * filter story definition summaries
     */
    private fun filterStoryDefinitionSummaries(request: StoryDefinitionConfigurationSummaryRequest): Array<Bson?> =
        arrayOf(
            Namespace eq request.namespace,
            BotId eq request.botId,
            if (request.category.isNullOrBlank()) null else Category eq request.category,
        )

    override fun save(story: StoryDefinitionConfiguration) {
        val previous = col.findOneById(story._id)
        val toSave =
            if (previous != null) {
                story.copy(version = previous.version + 1)
            } else {
                story
            }
        historyCol.save(StoryDefinitionConfigurationHistoryCol(toSave))
        col.save(toSave)
    }

    override fun delete(story: StoryDefinitionConfiguration) {
        val previous = col.findOneById(story._id)
        if (previous != null) {
            historyCol.save(StoryDefinitionConfigurationHistoryCol(previous, true))
        }
        col.deleteOneById(story._id)
    }

    override fun deleteByNamespaceAndBotId(namespace: String, botId: String) {
        val deletingStories = col.find(
            Filters.eq("namespace", namespace),
            Filters.eq("botId", botId)
        ).toList()
        if (deletingStories.isNotEmpty()) {
            deletingStories.forEach { deletingStory ->
                historyCol.save(StoryDefinitionConfigurationHistoryCol(deletingStory, true))
            }
        }
        col.deleteMany(
            and(
                BotId eq botId,
                Namespace eq namespace
            )
        )
    }

    override fun createBuiltInStoriesIfNotExist(stories: List<StoryDefinitionConfiguration>) {
        stories.forEach {
            // unique index throws exception if the story already exists
            try {
                col.insertOne(it)
            } catch (e: Exception) {
                logger.trace(e)
            }
        }
    }
}

private data class DateProjectionKey(val storyId: String)
private data class DateProjection(val _id: DateProjectionKey, val date: ZonedDateTime)
