/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.shared.cache.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.UpdateOptions
import fr.vsct.tock.shared.cache.TockCache
import fr.vsct.tock.shared.getDatabase
import org.litote.kmongo.createIndex
import org.litote.kmongo.deleteOne
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.replaceOne

/**
 *
 */
internal object MongoCache : TockCache {

    private const val MONGO_DATABASE: String = "tock_cache_mongo_db"

    private val col: MongoCollection<MongoCacheData> by lazy {
        val database: MongoDatabase = getDatabase(MONGO_DATABASE)
        val c = database.getCollection<MongoCacheData>("cache")
        c.createIndex("{'id':1,'type':1}", IndexOptions().unique(true))
        c.createIndex("{'type':1}")
        c
    }

    override fun getAll(type: String): Map<String, Any> {
        return col.find("{'type':${type.json}}").map { it.id to it.toValue() }.toMap()
    }

    override fun get(id: String, type: String): Any? {
        return col.findOne("{'id':${id.json},'type':${type.json}}")?.toValue()
    }

    override fun put(id: String, type: String, data: Any) {
        col.replaceOne(
                "{id:${id.json}, type:${type.json}}",
                MongoCacheData.fromValue(id, type, data),
                UpdateOptions().upsert(true))
    }

    override fun remove(id: String, type: String) {
        col.deleteOne("{'id':${id.json},'type':${type.json}}")
    }
}