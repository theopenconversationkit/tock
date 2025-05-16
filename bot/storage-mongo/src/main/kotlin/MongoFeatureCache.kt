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

import ai.tock.bot.mongo.ai.tock.bot.mongo.FeatureCache
import ai.tock.shared.error
import ai.tock.shared.watch
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import mu.KotlinLogging
import org.bson.BsonString
import org.litote.kmongo.reactivestreams.getCollection
import java.util.concurrent.ConcurrentHashMap

internal class MongoFeatureCache : FeatureCache {
    private val logger = KotlinLogging.logger {}

    private val features = ConcurrentHashMap<String, Feature>()

    private val asyncCol = MongoBotConfiguration.asyncDatabase.getCollection<Feature>()

    private val invalidateListener: (ChangeStreamDocument<Feature>) -> Unit = { c ->
        // cleanup cache
        (c.documentKey?.get(Feature_._id.name) as? BsonString)?.value?.also { key ->
            invalidate(key)
        }
    }

    init {
        try {
            asyncCol.watch(FullDocument.UPDATE_LOOKUP, listener = invalidateListener)
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun invalidate(key: String) {
        val globalKey = key.split("+").first()

        removeGlobalFeature(globalKey)
        removeAllConnectorFeatures(globalKey)
    }

    private fun removeAllConnectorFeatures(globalKey: String) {
        features.keys.filter { it.startsWith("$globalKey+") }.forEach {
            features.remove(it)
        }
    }

    private fun removeGlobalFeature(globalKey: String) {
        features.remove(globalKey)
    }

    override fun stateOf(key: String): Feature? {
        return features[key]
    }

    override fun setState(key: String, value: Feature) {
        features[key] = value
    }
}
