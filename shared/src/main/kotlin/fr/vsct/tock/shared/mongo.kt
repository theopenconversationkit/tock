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

package fr.vsct.tock.shared

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

internal val collectionBuilder: (KClass<*>) -> String = {
    it.simpleName!!
            .replace("storage", "", true)
            .toCharArray()
            .fold("", { s, t ->
                if (s.isEmpty()) t.toLowerCase().toString()
                else if (t.isUpperCase()) "${s}_${t.toLowerCase()}"
                else "$s$t"
            })
}

val mongoClient: MongoClient by lazy {
    KMongoConfiguration.defaultCollectionNameBuilder = collectionBuilder
    KMongo.createClient(MongoClientURI("mongodb://localhost:27017"))
}

fun getDatabase(databaseNameProperty: String): MongoDatabase {
    val databaseName = property(databaseNameProperty, databaseNameProperty)
    logger.info("get database $databaseName")
    return mongoClient.getDatabase(databaseName)
}
