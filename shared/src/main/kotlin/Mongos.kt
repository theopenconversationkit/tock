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

package ai.tock.shared

import ai.tock.shared.jackson.addDeserializer
import ai.tock.shared.jackson.addSerializer
import ai.tock.shared.jackson.jacksonAdditionalModules
import ai.tock.shared.security.mongo.MongoCredentialsProvider
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310StringParsableDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.AggregateIterable
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Collation
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.connection.TransportSettings
import com.mongodb.reactivestreams.client.MongoCollection
import de.undercouch.bson4jackson.types.Decimal128
import mu.KotlinLogging
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.KMongo
import org.litote.kmongo.ascending
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.reactivestreams.watchIndefinitely
import org.litote.kmongo.runCommand
import org.litote.kmongo.util.CollectionNameFormatter
import org.litote.kmongo.util.KMongoConfiguration
import org.litote.kmongo.util.KMongoConfiguration.registerBsonModule
import org.litote.kmongo.util.KMongoUtil
import java.math.BigDecimal
import java.time.Duration
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

internal val collectionBuilder: (KClass<*>) -> String = {
    it.simpleName!!
        .replace("storage", "", true)
        .toCharArray()
        .fold("") { s, t ->
            if (s.isEmpty()) t.lowercase()
            else if (t.isUpperCase()) "${s}_${t.lowercase()}"
            else "$s$t"
        }
}

private val ONE_BILLION = BigDecimal(1000000000L)

internal object TockKMongoConfiguration {
    init {
        CollectionNameFormatter.defaultCollectionNameBuilder = collectionBuilder
        IdGenerator.defaultGenerator = ObjectIdToStringGenerator

        val tockModule = SimpleModule().apply {
            addSerializer(ZoneId::class, ToStringSerializer(ZoneId::class.java))
            addDeserializer(ZoneId::class, JSR310StringParsableDeserializer.ZONE_ID)
            addSerializer(ZoneOffset::class, ToStringSerializer(ZoneOffset::class.java))
            addDeserializer(ZoneOffset::class, JSR310StringParsableDeserializer.ZONE_OFFSET)
            addDeserializer(Period::class, JSR310StringParsableDeserializer.PERIOD)
            addSerializer(Period::class, ToStringSerializer(Period::class.java))
            if (isDocumentDB()) {
                addSerializer(
                    Duration::class,
                    object : StdScalarSerializer<Duration>(Duration::class.java) {
                        override fun serialize(
                            duration: Duration?,
                            generator: JsonGenerator?,
                            provider: SerializerProvider?
                        ) {
                            generator?.writeString(duration?.toString())
                        }
                    }
                )
            } else {
                addSerializer(Duration::class, DurationSerializer.INSTANCE)
            }
            addDeserializer(
                Duration::class,
                object : StdScalarDeserializer<Duration>(Duration::class.java) {

                    override fun deserialize(parser: JsonParser, context: DeserializationContext): Duration? {
                        return if (parser.currentTokenId() == JsonTokenId.ID_EMBEDDED_OBJECT) {
                            val e = parser.embeddedObject
                            when (e) {
                                is Decimal128 -> {
                                    val b = e.bigDecimalValue()
                                    val seconds = b.toLong()
                                    val nanoseconds = b.subtract(BigDecimal.valueOf(seconds)).multiply(ONE_BILLION).toInt()
                                    Duration.ofSeconds(seconds, nanoseconds.toLong())
                                }

                                is Duration -> e
                                else -> error("unsupported duration $e")
                            }
                        } else {
                            DurationDeserializer.INSTANCE.deserialize(parser, context)
                        }
                    }
                }
            )
        }

        registerBsonModule(tockModule)
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

private val defaultMongoUrl = property(
    "tock_mongo_url",
    "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=tock&retryWrites=true"
)

private val mongoUrl = ConnectionString(defaultMongoUrl)

private val asyncMongoUrl = ConnectionString(
    property(
        "tock_async_mongo_url",
        defaultMongoUrl
    )
)

private val credentialsProvider = injector.provide<MongoCredentialsProvider>()

/**
 * The sync [MongoClient] of Tock.
 */
internal val mongoClient: MongoClient by lazy {
    TockKMongoConfiguration.configure()
    if (mongoUrl.credential == null) {
        val connectionString = mongoUrl
        val settings = MongoClientSettings
            .builder()
            .applyConnectionString(connectionString)
            .run { credentialsProvider.getCredentials()?.let { credential(it) } ?: this }
            .build()

        KMongo.createClient(settings)
    } else {
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
            .applyConnectionString(asyncMongoUrl)
            .apply {
                if (asyncMongoUrl.credential == null) {
                    credentialsProvider.getCredentials()?.let {
                        this.credential(it)
                    }
                }
                if (asyncMongoUrl.sslEnabled == true) {
                    transportSettings(TransportSettings.nettyBuilder().build())
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
    watchIndefinitely(
        fullDocument = fullDocument,
        subscribeListener = { (KotlinLogging.logger {}).info { "Subscribe stream" } },
        errorListener = { (KotlinLogging.logger {}).error(it) },
        reopenListener = { (KotlinLogging.logger {}).warn { "Reopen stream" } },
        listener = listener
    )
}

fun <T> com.mongodb.client.MongoCollection<T>.ensureIndex(
    vararg properties: kotlin.reflect.KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): String {
    if (indexOptions.name == null) {
        generateIndexName(ascending(*properties), indexOptions = indexOptions)?.also { indexOptions.name(it) }
    }
    return ensureIndex(*properties, indexOptions = indexOptions)
}

fun <T> com.mongodb.client.MongoCollection<T>.ensureUniqueIndex(
    vararg properties: kotlin.reflect.KProperty<*>,
    indexOptions: IndexOptions = IndexOptions()
): String {
    if (indexOptions.name == null) {
        generateIndexName(ascending(*properties), indexOptions = indexOptions)?.also { indexOptions.name(it) }
    }
    return ensureUniqueIndex(*properties, indexOptions = indexOptions)
}

fun <T> com.mongodb.client.MongoCollection<T>.ensureIndex(
    keys: Bson,
    indexOptions: IndexOptions = IndexOptions()
): String {
    if (indexOptions.name == null) {
        generateIndexName(keys, indexOptions = indexOptions)?.also { indexOptions.name(it) }
    }
    return ensureIndex(keys, indexOptions = indexOptions)
}

fun <T> com.mongodb.client.MongoCollection<T>.ensureIndex(
    keys: String,
    indexOptions: IndexOptions = IndexOptions()
): String {
    if (indexOptions.name == null) {
        generateIndexName(KMongoUtil.toBson(keys), indexOptions = indexOptions)?.also { indexOptions.name(it) }
    }
    return ensureIndex(keys, indexOptions = indexOptions)
}

private val isDocumentDB = booleanProperty("tock_document_db_on", false)

fun isDocumentDB(): Boolean = isDocumentDB

/**
 * Transform json data to prevent AWS DocumentDB field name restrictions
 * Amazon DocumentDB does not support dots “.” in a document field name
 */
fun transformData(data: Any?): Any? {
    return if (isDocumentDB())
        data?.let {
            when (it) {
                is Map<*, *> -> {
                    it.mapKeys { (key, _) -> key.toString().replace(".", "_DOT_") }
                        .mapValues { (_, value) -> transformData(value) }
                }

                is List<*> -> it.map { elem -> transformData(elem) }
                else -> it
            }
        }
    else data
}

/**
 * Generate and return an index matching DocumentDB limits (32 characters maximum in a compound index) with the given document keys and options
 */
private fun generateIndexName(document: Bson, indexOptions: IndexOptions): String? {
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
            reducedIndex += key.let {
                if (it.length > DocumentDBIndexReducedSize) it.substring(
                    0,
                    DocumentDBIndexReducedSize
                ) else it
            } + sort
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

fun pingMongoDatabase(databaseName: String): Boolean {
    val database = getDatabase(databaseName)
    val result = database.runCommand<Document>("{ ping: 1 }")
    return result["ok"] == 1.0
}

/**
 * Set collation if database supports it (ie not with DocumentDB)
 */
fun <T> FindIterable<T>.safeCollation(collation: Collation): FindIterable<T> =
    if (isDocumentDB()) {
        this
    } else {
        collation(collation)
    }

fun <T> AggregateIterable<T>.safeCollation(collation: Collation): AggregateIterable<T> =
    if (isDocumentDB()) {
        this
    } else {
        collation(collation)
    }

private const val DocumentDBIndexLimitSize = 32

private const val DocumentDBIndexReducedSize = 3

/**
 * By default, do not count more than 1000000 documents (for large databases)
 */
val defaultCountOptions: CountOptions = CountOptions().limit(1000000)
