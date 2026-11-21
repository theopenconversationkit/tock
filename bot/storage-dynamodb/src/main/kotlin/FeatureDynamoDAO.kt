/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureState
import ai.tock.bot.engine.feature.validateFeatureCategory
import ai.tock.bot.engine.feature.validateFeatureName
import ai.tock.shared.longProperty
import ai.tock.storage.dynamodb.DynamoStore.configTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * The features of a bot, in the partition `FEAT#<botId>,<namespace>`: the sort key is the end of the Mongo id,
 * `<category>,<name>` for the feature of every connector, `<category>,<name>+<applicationId>` for the feature of
 * one connector (the validation forbids `,` and `+` in the categories and the names).
 *
 * [isEnabled] reads the feature and its connector variants with one query, cached for
 * `tock_dynamodb_feature_cache_ttl_in_ms` (30s, 0 to disable): there is no change stream to invalidate the cache,
 * and a feature is checked several times by turn. The writes of this process invalidate it at once.
 */
object FeatureDynamoDAO : FeatureDAO {
    private val logger = KotlinLogging.logger {}

    private val cacheTtl = longProperty("tock_dynamodb_feature_cache_ttl_in_ms", 30_000)

    /** The document of a feature: the fields of the Mongo one. */
    internal data class Feature(
        val key: String,
        val enabled: Boolean,
        val botId: String,
        val namespace: String,
        val startDate: ZonedDateTime? = null,
        val endDate: ZonedDateTime? = null,
        val graduation: Int? = null,
    )

    private class CachedFeatures(
        val features: Map<String, Feature>,
        val expiration: Long,
    )

    private val cache = ConcurrentHashMap<String, CachedFeatures>()

    private fun pk(
        botId: String,
        namespace: String,
    ) = "FEAT#$botId,$namespace"

    private fun sk(
        category: String,
        name: String,
        applicationId: String?,
    ) = "$category,$name${applicationId?.let { "+$it" } ?: ""}"

    private fun cacheKey(
        botId: String,
        namespace: String,
        category: String,
        name: String,
    ) = "${pk(botId, namespace)}|${sk(category, name, null)}"

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    /** The feature and its connector variants, by sort key. */
    private fun features(
        botId: String,
        namespace: String,
        category: String,
        name: String,
    ): Map<String, Feature> {
        val key = cacheKey(botId, namespace, category, name)
        val now = System.currentTimeMillis()
        cache[key]?.takeIf { it.expiration > now }?.let { return it.features }
        val global = sk(category, name, null)
        val features =
            DynamoStore
                .queryPartition(configTable, pk(botId, namespace), skPrefix = global)
                .filter { it.str(SK).let { sk -> sk == global || sk?.startsWith("$global+") == true } }
                .associate { it.str(SK)!! to DocumentCodec.decode<Feature>(it.bytes(DOC)) }
        if (cacheTtl > 0) {
            cache[key] = CachedFeatures(features, now + cacheTtl)
        }
        return features
    }

    override suspend fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?,
        default: Boolean,
        userId: String?,
    ): Boolean {
        val features = io { features(botId, namespace, category, name) }
        val feature = applicationId?.let { features[sk(category, name, it)] } ?: features[sk(category, name, null)]
        return if (feature == null) {
            default.also {
                // registers the feature, so that the admin shows it; an existing one is kept
                io {
                    DynamoStore.putIfAbsent(configTable, item(botId, namespace, category, name, null, Feature("$category,$name", default, botId, namespace)))
                }
                invalidate(botId, namespace, category, name)
            }
        } else {
            isEnabled(feature, userId)
        }
    }

    private fun isEnabled(
        feature: Feature,
        userId: String?,
    ): Boolean = feature.enabled && isEnabledForDate(feature, ZonedDateTime.now()) && isEnabledForUser(feature, userId)

    private fun isEnabledForUser(
        feature: Feature,
        userId: String?,
    ): Boolean {
        userId ?: return true
        return when (feature.graduation) {
            null -> true
            0 -> false
            100 -> true
            else -> (abs(userId.hashCode()) % 100 < feature.graduation)
        }
    }

    // the same rules as the Mongo DAO: an end date without a start date is ignored
    private fun isEnabledForDate(
        feature: Feature,
        now: ZonedDateTime,
    ): Boolean =
        when {
            feature.startDate != null && feature.endDate == null -> now.isAfter(feature.startDate)
            feature.startDate != null && feature.endDate != null -> now.isAfter(feature.startDate) && now.isBefore(feature.endDate)
            else -> true
        }

    private fun invalidate(
        botId: String,
        namespace: String,
        category: String,
        name: String,
    ) {
        cache.remove(cacheKey(botId, namespace, category, name))
    }

    private fun item(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?,
        feature: Feature,
    ) = DynamoStore.key(pk(botId, namespace), sk(category, name, applicationId)) + (DOC to b(DocumentCodec.encode(feature)))

    private suspend fun put(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?,
        feature: Feature,
    ) {
        validateFeatureCategory(category)
        validateFeatureName(name)
        io { DynamoStore.putItem(configTable, item(botId, namespace, category, name, applicationId, feature)) }
        invalidate(botId, namespace, category, name)
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
    ) = put(botId, namespace, category, name, applicationId, Feature("$category,$name", true, botId, namespace, startDate, endDate, graduation))

    override suspend fun disable(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?,
    ) = put(botId, namespace, category, name, applicationId, Feature("$category,$name", false, botId, namespace))

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
    ) = put(botId, namespace, category, name, applicationId, Feature("$category,$name", enabled, botId, namespace, startDate, endDate, graduation))

    override suspend fun deleteFeature(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?,
    ) {
        io { DynamoStore.deleteItem(configTable, pk(botId, namespace), sk(category, name, applicationId)) }
        invalidate(botId, namespace, category, name)
    }

    override suspend fun getFeatures(
        botId: String,
        namespace: String,
    ): List<FeatureState> =
        io { DynamoStore.queryPartition(configTable, pk(botId, namespace), consistentRead = true) }
            .mapNotNull { item ->
                try {
                    featureOf(item).third
                } catch (e: Exception) {
                    logger.error(e) { "invalid feature ${item.str(SK)}" }
                    null
                }
            }

    /** True if [item] (an item of the configuration table) is a feature. */
    fun isFeature(item: Map<String, AttributeValue>): Boolean = item.str(PK)?.startsWith("FEAT#") == true && item.containsKey(DOC)

    /** The bot id, the namespace and the state of a feature item: the replication to MongoDB reads the stream items. */
    fun featureOf(item: Map<String, AttributeValue>): Triple<String, String, FeatureState> {
        val feature = DocumentCodec.decode<Feature>(item.bytes(DOC))
        val sk = item.str(SK)!!
        val index = feature.key.lastIndexOf(',')
        val applicationIndex = sk.lastIndexOf('+')
        return Triple(
            feature.botId,
            feature.namespace,
            FeatureState(
                category = if (index == -1) "" else feature.key.substring(0, index),
                name = if (index == -1) feature.key else feature.key.substring(index + 1),
                enabled = feature.enabled,
                startDate = feature.startDate,
                endDate = feature.endDate,
                applicationId = if (applicationIndex == -1) null else sk.substring(applicationIndex + 1),
                graduation = feature.graduation,
            ),
        )
    }
}
