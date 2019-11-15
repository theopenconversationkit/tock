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

package ai.tock.shared

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310StringParsableDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer
import com.mongodb.ConnectionString
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.connection.netty.NettyStreamFactoryFactory
import com.mongodb.reactivestreams.client.MongoCollection
import de.undercouch.bson4jackson.types.Decimal128
import ai.tock.shared.jackson.addDeserializer
import ai.tock.shared.jackson.addSerializer
import ai.tock.shared.jackson.jacksonAdditionalModules
import ai.tock.shared.security.getNoopSslContext
import ai.tock.shared.security.setSslProperties
import mu.KotlinLogging
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.KMongo
import org.litote.kmongo.ascending
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.reactivestreams.watchIndefinitely
import org.litote.kmongo.util.CollectionNameFormatter
import org.litote.kmongo.util.KMongoConfiguration
import org.litote.kmongo.util.KMongoConfiguration.registerBsonModule
import org.litote.kmongo.util.KMongoUtil
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
            if (isDocumentDB()) {
                addSerializer(Duration::class, object : StdScalarSerializer<Duration>(Duration::class.java) {
                    override fun serialize(duration: Duration?, generator: JsonGenerator?, provider: SerializerProvider?) {
                        generator?.writeString(duration?.toString())
                    }
                })
            } else {
                addSerializer(Duration::class, DurationSerializer.INSTANCE)
            }
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

    fun configure(async: Boolean = false) {
        logger.debug("KMongo Tock ${if (async) "async" else ""} configuration loaded")
    }
}

private val mongoUrl = ConnectionString(
    property(
        "tock_mongo_url",
        "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=tock&retryWrites=true"
    )
)

/**
 * PEM mongo certificate name, only if mongo is used in SSL.
 * The certificate should be accessible from the classpath.
 */
private val mongoCaCert = property(
    "tock_mongo_ssl_cert",
    ""
)

/**
 * The sync [MongoClient] of Tock.
 */
internal val mongoClient: MongoClient by lazy {
    TockKMongoConfiguration.configure()
    return@lazy if (devEnvironment) {
        val uri = com.mongodb.MongoClientURI(mongoUrl.toString())
        KMongo.createClient(com.mongodb.MongoClientURI(uri.uri, MongoClientOptions.builder(uri.options).sslContext(getNoopSslContext())))
    } else {
        if (mongoUrl.sslEnabled == true) {
            setSslProperties(mongoCaCert)
        }
        KMongo.createClient(mongoUrl)
    }
}

/**
 * The async [MongoClient] of Tock.
 */
internal val asyncMongoClient: com.mongodb.reactivestreams.client.MongoClient by lazy {
    TockKMongoConfiguration.configure(true)
    org.litote.kmongo.reactivestreams.KMongo.createClient(
        MongoClientSettings.builder()
            .applyConnectionString(mongoUrl)
            .applyToSslSettings {
                if (devEnvironment && mongoUrl.sslEnabled == true && mongoUrl.sslInvalidHostnameAllowed == true) {
                    it.context(getNoopSslContext())
                }
            }
            .apply {
                if (mongoUrl.sslEnabled == true) {
                    setSslProperties(mongoCaCert)
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

inline fun <reified T : Any> MongoCollection<T>.watch(
    fullDocument: FullDocument = FullDocument.DEFAULT,
    noinline listener: (ChangeStreamDocument<T>) -> Unit
) {
    // TODO: Change streams are not supported in DocumentDB - Implement an alternative method for database triggers in DocumentDB
    if (!isDocumentDB()) {
        val loggerBis = KotlinLogging.logger {}
        loggerBis.info("Watch ")
        watchIndefinitely(
            fullDocument = fullDocument,
            subscribeListener = { (KotlinLogging.logger {}).info { "Subscribe stream" } },
            errorListener = { (KotlinLogging.logger {}).error(it) },
            reopenListener = { (KotlinLogging.logger {}).warn { "Reopen stream" } },
            listener = listener
        )
    }
}

fun <T> com.mongodb.client.MongoCollection<T>.ensureIndex(vararg properties: kotlin.reflect.KProperty<*>,
                                                          indexOptions: com.mongodb.client.model.IndexOptions = IndexOptions()): kotlin.String {
    generateIndex(ascending(*properties), indexOptions = indexOptions)?.let { indexOptions.name(it) }
    return ensureIndex(*properties, indexOptions = indexOptions)
}

fun <T> com.mongodb.client.MongoCollection<T>.ensureUniqueIndex(vararg properties: kotlin.reflect.KProperty<*>,
                                                                indexOptions: com.mongodb.client.model.IndexOptions = IndexOptions()): kotlin.String {
    generateIndex(ascending(*properties), indexOptions = indexOptions)?.let { indexOptions.name(it) }
    return ensureUniqueIndex(*properties, indexOptions = indexOptions)
}

fun <T> com.mongodb.client.MongoCollection<T>.ensureIndex(keys: Bson,
                                                          indexOptions: IndexOptions = IndexOptions()): String {
    generateIndex(keys, indexOptions = indexOptions)?.let { indexOptions.name(it) }
    return ensureIndex(keys, indexOptions = indexOptions)
}

fun <T> com.mongodb.client.MongoCollection<T>.ensureIndex(keys: String,
                                                          indexOptions: IndexOptions = IndexOptions()): String {
    generateIndex(KMongoUtil.toBson(keys), indexOptions = indexOptions)?.let { indexOptions.name(it) }
    return ensureIndex(keys, indexOptions = indexOptions)
}

fun isDocumentDB(): Boolean {
    return (booleanProperty("tock_document_db_on", false))
}

/**
 * Generate and return an index matching DocumentDB limits (32 characters maximum in a compound index) with the given document keys and options
 */
private fun generateIndex(document: Bson, indexOptions: com.mongodb.client.model.IndexOptions): String? {
    // Don't generate an index if the database isn't DocumentDB
    if (!isDocumentDB()) {
        return null
    }

    if (indexOptions.name?.let { it.length > DocumentDBIndexLimitSize } != false) {
        var index = ""
        var reducedIndex = ""

        for ((key, value) in (document as BsonDocument).entries) {
            val sort: String = value.takeIf { it.isInt32 }?.asInt32()?.value.toString()
            index += key + sort
            reducedIndex += key.let { if (it.length > DocumentDBIndexReducedSize) it.substring(0, DocumentDBIndexReducedSize) else it } + sort
        }

        if (index.length <= DocumentDBIndexLimitSize) {
            return index
        }
        if (reducedIndex.length <= DocumentDBIndexLimitSize) {
            return reducedIndex
        } else {
            logger.error("Generated reduced index too long : $index")
        }
        return index
    }
    return null
}

private const val DocumentDBIndexLimitSize = 32

private const val DocumentDBIndexReducedSize = 3