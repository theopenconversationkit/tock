/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.MongoBotConfiguration.database
import ai.tock.shared.error
import ai.tock.shared.internalDefaultZoneId
import ai.tock.shared.watch
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument.UPDATE_LOOKUP
import mu.KotlinLogging
import org.bson.BsonString
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.reactivestreams.forEach
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.save
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

@Data(internal = true)
@JacksonData(internal = true)
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Feature(
    val _id: String,
    val key: String,
    val enabled: Boolean,
    val botId: String,
    val namespace: String,
    val startDate: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null
)

/**
 *
 */
internal object FeatureMongoDAO : FeatureDAO {

    private val logger = KotlinLogging.logger {}
    private val col = database.getCollection<Feature>()
    private val asyncCol = asyncDatabase.getCollection<Feature>()
    private val features = ConcurrentHashMap<String, Boolean>()

    /**
     * Watch listener.
     */
    private val listener: (ChangeStreamDocument<Feature>) -> Unit = { c ->
        //cleanup cache
        (c.documentKey?.get(_id.name) as? BsonString)?.value?.also { features.remove(it) }
    }

    init {
        try {
            asyncCol.find().forEach { it, t ->
                if (t != null) logger.error(t)
                else if (it != null) features[it._id] = it.enabled
            }
            asyncCol.watch(UPDATE_LOOKUP, listener = listener)
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    private fun calculateId(botId: String, namespace: String, category: String, name: String) =
        "$botId,$namespace,$category,$name"


    override fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        default: Boolean
    ): Boolean {
        val id = calculateId(botId, namespace, category, name)
        val now = ZonedDateTime.now(internalDefaultZoneId)

        return col.findOne(_id eq id)
            .let { f ->
                if (f == null) {
                    default.also {
                        addFeature(botId, namespace, default, category, name, null, null)
                    }
                } else {
                    features[id] = f.enabled
                    when {
                        f.startDate != null && f.endDate == null -> f.enabled && now.isAfter(f.startDate)
                        f.startDate != null && f.endDate != null -> f.enabled && now.isAfter(f.startDate)
                                && now.isBefore(f.endDate)
                        else -> f.enabled
                    }
                }
            }
    }

    override fun enable(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        startDate: ZonedDateTime?,
        endDate: ZonedDateTime?
    ) {
        val id = calculateId(botId, namespace, category, name)
        features[id] = true

        col.replaceOne(
            _id eq id,
            Feature(id, "$category,$name", true, botId, namespace, startDate, endDate),
            ReplaceOptions().upsert(true)
        )
    }

    override fun disable(botId: String, namespace: String, category: String, name: String) {
        val id = calculateId(botId, namespace, category, name)
        features[id] = false
        col.replaceOne(
            _id eq id,
            Feature(id, "$category,$name", false, botId, namespace),
            ReplaceOptions().upsert(true)
        )
    }

    override fun getFeatures(botId: String, namespace: String): List<FeatureState> =
        col.find(BotId eq botId, Namespace eq namespace)
            .mapNotNull {
                try {
                    val index = it.key.lastIndexOf(',')
                    val category = if (index == -1) "" else it.key.substring(0, index)
                    val name = if (index == -1) it.key else it.key.substring(index + 1, it.key.length)
                    FeatureState(category, name, it.enabled, it.startDate, it.endDate)
                } catch (e: Exception) {
                    logger.error(e)
                    null
                }
            }

    override fun addFeature(
        botId: String,
        namespace: String,
        enabled: Boolean,
        category: String,
        name: String,
        startDate: ZonedDateTime?,
        endDate: ZonedDateTime?
    ) {
        val id = calculateId(botId, namespace, category, name)
        features[id] = enabled
        col.save(Feature(id, "$category,$name", enabled, botId, namespace, startDate, endDate))
    }

    override fun deleteFeature(botId: String, namespace: String, category: String, name: String) {
        val id = calculateId(botId, namespace, category, name)
        features.remove(id)
        col.deleteOneById(id)
    }
}