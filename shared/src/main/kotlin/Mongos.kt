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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310StringParsableDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import de.undercouch.bson4jackson.types.Decimal128
import fr.vsct.tock.shared.jackson.addDeserializer
import fr.vsct.tock.shared.jackson.addSerializer
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.util.CollectionNameFormatter
import org.litote.kmongo.util.KMongoConfiguration
import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

internal val collectionBuilder: (KClass<*>) -> String = {
    it.simpleName!!
        .replace("storage", "", true)
        .toCharArray()
        .fold("") { s, t ->
            if (s.isEmpty()) t.toLowerCase().toString()
            else if (t.isUpperCase()) "${s}_${t.toLowerCase()}"
            else "$s$t"
        }
}

/**
 * The additional jackson modules for KMongo serialization/deserialization.
 */
val mongoJacksonModules = mutableListOf<Module>()

internal fun configureKMongo() {
    logger.info { "init mongo jackson mapper with additional modules $mongoJacksonModules" }
    CollectionNameFormatter.defaultCollectionNameBuilder = collectionBuilder
    IdGenerator.defaultGenerator = ObjectIdToStringGenerator

    val tockModule = SimpleModule().apply {
        addSerializer(ZoneId::class, ToStringSerializer(ZoneId::class.java))
        addDeserializer(ZoneId::class, JSR310StringParsableDeserializer.ZONE_ID)
        addSerializer(ZoneOffset::class, ToStringSerializer(ZoneOffset::class.java))
        addDeserializer(ZoneOffset::class, JSR310StringParsableDeserializer.ZONE_OFFSET)
        addSerializer(Duration::class, DurationSerializer.INSTANCE)
        addDeserializer(Duration::class, object : StdScalarDeserializer<Duration>(Duration::class.java) {

            override fun deserialize(parser: JsonParser, context: DeserializationContext): Duration? {
                return if (parser.currentTokenId() == JsonTokenId.ID_EMBEDDED_OBJECT) {
                    val e = parser.embeddedObject
                    when (e) {
                        is Decimal128 -> {
                            val b = e.bigDecimalValue()
                            val seconds = b.toLong()
                            val nanoseconds = DecimalUtils.extractNanosecondDecimal(b, seconds)
                            Duration.ofSeconds(seconds, nanoseconds.toLong())
                        }
                        is Duration -> e
                        else -> error("unsupported duration $e")
                    }
                } else {
                    DurationDeserializer.INSTANCE.deserialize(parser, context)
                }
            }
        })
    }

    KMongoConfiguration.registerBsonModule(tockModule)
    mongoJacksonModules.forEach {
        KMongoConfiguration.registerBsonModule(it)
    }
}

/**
 * The [MongoClient] of Tock.
 */
internal val mongoClient: MongoClient by lazy {
    configureKMongo()
    KMongo.createClient(
        MongoClientURI(
            property("tock_mongo_url", "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=tock")
        )
    )
}

/**
 * Return the database specified in the [databaseNameProperty].
 * if the env or system property is not found, use the [databaseNameProperty] as database name (remove "_mongo_db" string is present).
 */
fun getDatabase(databaseNameProperty: String): MongoDatabase {
    val databaseName = property(databaseNameProperty, databaseNameProperty).replace("_mongo_db", "")
    logger.info("get database $databaseName")
    return injector.provide<MongoClient>().getDatabase(databaseName)
}
