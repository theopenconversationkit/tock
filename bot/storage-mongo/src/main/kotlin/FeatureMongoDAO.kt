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

import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureState
import ai.tock.bot.mongo.Feature_.Companion.BotId
import ai.tock.bot.mongo.Feature_.Companion.Namespace
import ai.tock.bot.mongo.Feature_.Companion._id
import ai.tock.bot.mongo.ai.tock.bot.mongo.FeatureCache
import ai.tock.shared.error
import ai.tock.shared.internalDefaultZoneId
import mu.KotlinLogging
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import java.time.ZonedDateTime
import kotlin.math.abs

@Data(internal = true)
@JacksonData(internal = true)
internal data class Feature(
    val _id: String,
    val key: String,
    val enabled: Boolean,
    val botId: String,
    val namespace: String,
    val startDate: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val graduation: Int? = null,
)

internal class FeatureMongoDAO(
    private val cache: FeatureCache,
    asyncCol: com.mongodb.reactivestreams.client.MongoCollection<Feature>,
) : FeatureDAO {

    private val col = asyncCol.coroutine
    private val logger = KotlinLogging.logger {}

    private fun calculateId(botId: String, namespace: String, category: String, name: String, applicationId: String?) =
        "$botId,$namespace,$category,$name${applicationId?.let { "+$it" } ?: ""}"

    override suspend fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?,
        default: Boolean,
        userId: String?,
    ): Boolean {
        val id = calculateId(botId, namespace, category, name, applicationId)
        val idWithoutApplicationId = calculateId(botId, namespace, category, name, null)

        val (connectorFeature, globalFeature) = getValues(id, idWithoutApplicationId)

        return if (connectorFeature == null && globalFeature == null) {
            default.also {
                addFeature(botId, namespace, default, category, name, null, null, null)
            }
        } else {
            val feature = (connectorFeature ?: globalFeature)!!
            isEnabled(feature, userId)
        }
    }

    private fun isEnabled(feature: Feature, userId: String?): Boolean {
        return feature.enabled
                && isEnableForDate(feature, ZonedDateTime.now(internalDefaultZoneId))
                && isEnableForUser(feature, userId)
    }

    private fun isEnableForUser(feature: Feature, userId: String?): Boolean {
        userId ?: return true
        return when (feature.graduation) {
            null -> true
            0 -> false
            100 -> true
            else -> (abs(userId.hashCode()) % 100 < feature.graduation)
        }
    }

    private fun isEnableForDate(feature: Feature, now: ZonedDateTime): Boolean {
        return when {
            feature.startDate != null && feature.endDate == null -> now.isAfter(feature.startDate)
            feature.startDate != null && feature.endDate != null -> now.isAfter(feature.startDate) &&
                    now.isBefore(feature.endDate)
            // FIXME : startDate == null && endDate != null is not handle ?
            else -> true
        }
    }

    private suspend fun getValues(id: String, idWithoutApplicationId: String): Pair<Feature?, Feature?> {
        val cacheForId = cache.stateOf(id)
        val cacheForIdWithoutApplicationId = cache.stateOf(idWithoutApplicationId)
        val pair = if (cacheForId == null && cacheForIdWithoutApplicationId == null) {
            cacheAllConnectorFeatureWithId(idWithoutApplicationId)

            Pair(
                col.findOne(_id eq id)?.also { cache.setState(id, it) },
                col.findOne(_id eq idWithoutApplicationId)?.also { cache.setState(idWithoutApplicationId, it) }
            )
        } else {
            Pair(
                cacheForId,
                cacheForIdWithoutApplicationId
            )
        }
        return pair
    }

    private suspend fun cacheAllConnectorFeatureWithId(globalId: String) {
        col.find("{\"_id\": /^$globalId\\+/}").toList().forEach {
            cache.setState(it._id, it)
        }
    }

    override suspend fun enable(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        startDate: ZonedDateTime?,
        endDate: ZonedDateTime?,
        applicationId: String?,
        graduation: Int?,
    ) {
        val id = calculateId(botId, namespace, category, name, applicationId)
        val feature = Feature(id, "$category,$name", true, botId, namespace, startDate, endDate, graduation)

        col.save(feature)
    }

    override suspend fun disable(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?
    ) {
        val id = calculateId(botId, namespace, category, name, applicationId)
        val feature = Feature(id, "$category,$name", false, botId, namespace)
        col.save(feature)
    }

    override suspend fun getFeatures(botId: String, namespace: String): List<FeatureState> =
        col.find(BotId eq botId, Namespace eq namespace)
            .toList()
            .mapNotNull {
                try {
                    val index = it.key.lastIndexOf(',')
                    val applicationIndex = it._id.lastIndexOf('+')
                    val category = if (index == -1) "" else it.key.substring(0, index)
                    val name = if (index == -1) it.key else it.key.substring(index + 1, it.key.length)
                    val applicationId = if (applicationIndex == -1) null else it._id.substring(applicationIndex + 1)
                    FeatureState(category, name, it.enabled, it.startDate, it.endDate, applicationId, it.graduation)
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
            }

    override suspend fun addFeature(
        botId: String,
        namespace: String,
        enabled: Boolean,
        category: String,
        name: String,
        startDate: ZonedDateTime?,
        endDate: ZonedDateTime?,
        applicationId: String?,
        graduation: Int?,
    ) {
        val id = calculateId(botId, namespace, category, name, applicationId)
        val feature = Feature(id, "$category,$name", enabled, botId, namespace, startDate, endDate, graduation)
        col.save(feature)
    }

    override suspend fun deleteFeature(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?
    ) {
        val id = calculateId(botId, namespace, category, name, applicationId)
        col.deleteOneById(id)
    }
}
