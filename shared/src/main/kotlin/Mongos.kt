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
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310StringParsableDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer
import com.mongodb.ConnectionString
import com.mongodb.MongoClient
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoDatabase
import com.mongodb.connection.netty.NettyStreamFactoryFactory
import de.undercouch.bson4jackson.types.Decimal128
import fr.vsct.tock.shared.jackson.addDeserializer
import fr.vsct.tock.shared.jackson.addSerializer
import fr.vsct.tock.shared.jackson.jacksonAdditionalModules
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.util.CollectionNameFormatter
import org.litote.kmongo.util.KMongoConfiguration
import org.litote.kmongo.util.KMongoConfiguration.registerBsonModule
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

internal object TockKMongoConfiguration {
    init {
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
        KMongoConfiguration.extendedJsonMapper.registerModule(tockModule)
        jacksonAdditionalModules.forEach {
            registerBsonModule(it)
            KMongoConfiguration.extendedJsonMapper.registerModule(it)
        }
    }

    fun configure() {
        logger.debug("KMongo Tock configuration loaded")
    }
}

private val mongoUrl = ConnectionString(
    property(
        "tock_mongo_url",
        "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=tock&retryWrites=true"
    )
)

/**
 * The sync [MongoClient] of Tock.
 */
internal val mongoClient: MongoClient by lazy {
    TockKMongoConfiguration.configure()
    KMongo.createClient(mongoUrl)
}

/**
 * The async [MongoClient] of Tock.
 */
internal val asyncMongoClient: com.mongodb.reactivestreams.client.MongoClient by lazy {
    TockKMongoConfiguration.configure()
    org.litote.kmongo.reactivestreams.KMongo.createClient(
        MongoClientSettings.builder()
            .applyConnectionString(mongoUrl)
            .apply {
                if (mongoUrl.sslEnabled == true) {
                    streamFactoryFactory(NettyStreamFactoryFactory.builder().build())
                }
            }
            .build()
    )
}

/**
 * Return the sync database specified in the [databaseNameProperty].
 * if the env or system property is not found, use the [databaseNameProperty] as database name (remove "_mongo_db" string is present).
 */
fun getDatabase(databaseNameProperty: String): MongoDatabase {
    val databaseName = formatDatabase(databaseNameProperty)
    logger.info("get database $databaseName")
    return injector.provide<MongoClient>().getDatabase(databaseName)
}

/**
 * Return the async database specified in the [databaseNameProperty].
 * if the env or system property is not found, use the [databaseNameProperty] as database name (remove "_mongo_db" string is present).
 */
fun getAsyncDatabase(databaseNameProperty: String): com.mongodb.reactivestreams.client.MongoDatabase {
    val databaseName = formatDatabase(databaseNameProperty)
    logger.info("get database $databaseName")
    return injector.provide<com.mongodb.reactivestreams.client.MongoClient>().getDatabase(databaseName)
}

private fun formatDatabase(databaseNameProperty: String): String =
    property(databaseNameProperty, databaseNameProperty).replace("_mongo_db", "")
