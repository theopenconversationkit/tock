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

package ai.tock.shared.cache.mongo

import ai.tock.shared.TOCK_CACHE_DATABASE
import ai.tock.shared.cache.TockCache
import ai.tock.shared.cache.mongo.MongoCacheData_.Companion.Type
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.error
import ai.tock.shared.getDatabase
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.ReplaceOptions
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.deleteOne
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

/**
 *
 */
internal object MongoCache : TockCache {
    private const val MONGO_DATABASE: String = TOCK_CACHE_DATABASE

    private val logger = KotlinLogging.logger {}

    private val col: MongoCollection<MongoCacheData> by lazy {
        val database: MongoDatabase = getDatabase(MONGO_DATABASE)
        val c = database.getCollection<MongoCacheData>("cache")
        c.ensureUniqueIndex(MongoCacheData_.Id, Type)
        c.ensureIndex(Type)
        c
    }

    override fun <T> getAll(type: String): Map<Id<T>, Any> {
        @Suppress("UNCHECKED_CAST")
        return col
            .find(Type eq type)
            .asSequence()
            .associateBy { it.id }
            .mapValues { it.value.toValue() }
            as Map<Id<T>, Any>
    }

    override fun <T> get(
        id: Id<T>,
        type: String,
    ): T? {
        @Suppress("UNCHECKED_CAST")
        return try {
            col.findOne(MongoCacheData_.Id eq id, Type eq type)?.toValue() as T?
        } catch (e: Exception) {
            logger.error(e)
            remove(id, type)
            null
        }
    }

    override fun <T : Any> put(
        id: Id<T>,
        type: String,
        data: T,
    ) {
        col.replaceOne(
            and(MongoCacheData_.Id eq id, Type eq type),
            MongoCacheData.fromValue(id, type, data),
            ReplaceOptions().upsert(true),
        )
    }

    override fun <T> remove(
        id: Id<T>,
        type: String,
    ) {
        col.deleteOne(MongoCacheData_.Id eq id, Type eq type)
    }
}
