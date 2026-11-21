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

import ai.tock.shared.InternalTockApi
import ai.tock.shared.booleanProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import mu.KotlinLogging
import org.litote.jackson.registerModulesFromServiceLoader
import org.litote.kmongo.id.jackson.IdJacksonModule
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.ConsumedCapacity
import software.amazon.awssdk.services.dynamodb.model.Delete
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.Projection
import software.amazon.awssdk.services.dynamodb.model.ProjectionType
import software.amazon.awssdk.services.dynamodb.model.Put
import software.amazon.awssdk.services.dynamodb.model.PutRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import software.amazon.awssdk.services.dynamodb.model.StreamSpecification
import software.amazon.awssdk.services.dynamodb.model.StreamViewType
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException
import software.amazon.awssdk.services.dynamodb.model.WriteRequest
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClient
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JsonNode
import tools.jackson.databind.PropertyName
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated
import tools.jackson.databind.introspect.AnnotationIntrospectorPair
import tools.jackson.databind.introspect.NopAnnotationIntrospector
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.concurrent.atomic.DoubleAdder
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private val logger = KotlinLogging.logger {}

/** Generic attribute names: every table has a `pk`/`sk` primary key. */
const val PK = "pk"
const val SK = "sk"

/** The attribute of the documents (JSON, gzip). */
const val DOC = "doc"

/** The expiration of an item, in epoch seconds: the attribute of the DynamoDB TTL of every table. */
const val TTL = "ttl"

/**
 * DynamoDB access shared by the DAOs of the bots and of the NLP: client, tables, document codec and capacity metrics.
 *
 * Properties:
 * - `tock_dynamodb_endpoint`: endpoint override (DynamoDB Local), unset on AWS
 * - `tock_dynamodb_region`: region (default eu-west-3)
 * - `tock_dynamodb_table_prefix`: table name prefix (default `tock_`)
 * - `tock_dynamodb_create_tables`: create the tables at startup (default true)
 * - `tock_dynamodb_streams_enabled`: enable the DynamoDB Streams of the tables (default true), read by the
 *   replication of the runtime writes to the MongoDB of the admin
 */
object DynamoStore {
    val tablePrefix: String = property("tock_dynamodb_table_prefix", "tock_")

    val configTable = "${tablePrefix}config"
    val runtimeTable = "${tablePrefix}runtime"
    val sentenceTable = "${tablePrefix}nlp_sentence"

    const val GSI1 = "gsi1"
    const val GSI2 = "gsi2"
    const val GSI1_PK = "gsi1pk"
    const val GSI1_SK = "gsi1sk"
    const val GSI2_PK = "gsi2pk"
    const val GSI2_SK = "gsi2sk"

    val client: DynamoDbClient by lazy {
        DynamoDbClient
            .builder()
            .httpClient(UrlConnectionHttpClient.create())
            .region(Region.of(property("tock_dynamodb_region", "eu-west-3")))
            .apply { propertyOrNull("tock_dynamodb_endpoint")?.let { endpointOverride(URI(it)) } }
            .build()
            .also { if (booleanProperty("tock_dynamodb_create_tables", true)) createTables(it) }
    }

    private val streamsEnabled = booleanProperty("tock_dynamodb_streams_enabled", true)

    /** The client of the DynamoDB Streams of the tables. */
    val streamsClient: DynamoDbStreamsClient by lazy {
        // the tables, and their streams, exist once the client has created them
        client
        DynamoDbStreamsClient
            .builder()
            .httpClient(UrlConnectionHttpClient.create())
            .region(Region.of(property("tock_dynamodb_region", "eu-west-3")))
            .apply { propertyOrNull("tock_dynamodb_endpoint")?.let { endpointOverride(URI(it)) } }
            .build()
    }

    private val streamSpecification: StreamSpecification =
        StreamSpecification
            .builder()
            .streamEnabled(true)
            .streamViewType(StreamViewType.NEW_AND_OLD_IMAGES)
            .build()

    /** Capacity units consumed since the last [resetMetrics] (read, write), to measure the cost. */
    private val readUnits = DoubleAdder()
    private val writeUnits = DoubleAdder()

    fun resetMetrics() {
        readUnits.reset()
        writeUnits.reset()
    }

    fun consumedReadUnits(): Double = readUnits.sum()

    fun consumedWriteUnits(): Double = writeUnits.sum()

    fun recordRead(capacity: ConsumedCapacity?) {
        capacity?.capacityUnits()?.let { readUnits.add(it) }
    }

    fun recordRead(capacities: List<ConsumedCapacity>?) {
        capacities?.forEach { recordRead(it) }
    }

    fun recordWrite(capacity: ConsumedCapacity?) {
        capacity?.capacityUnits()?.let { writeUnits.add(it) }
    }

    fun recordWrite(capacities: List<ConsumedCapacity>?) {
        capacities?.forEach { recordWrite(it) }
    }

    private fun createTables(client: DynamoDbClient) {
        createTable(client, configTable)
        // the runtime index only gives dialog ids: an ALL projection would copy each dialog header at each turn
        createTable(client, runtimeTable, gsi1 = true, projection = ProjectionType.KEYS_ONLY)
        createTable(client, sentenceTable, gsi1 = true, gsi2 = true)
    }

    private fun createTable(
        client: DynamoDbClient,
        name: String,
        gsi1: Boolean = false,
        gsi2: Boolean = false,
        projection: ProjectionType = ProjectionType.ALL,
    ) {
        fun key(
            attribute: String,
            type: KeyType,
        ) = KeySchemaElement
            .builder()
            .attributeName(attribute)
            .keyType(type)
            .build()

        fun index(
            indexName: String,
            pk: String,
            sk: String,
        ) = GlobalSecondaryIndex
            .builder()
            .indexName(indexName)
            .keySchema(key(pk, KeyType.HASH), key(sk, KeyType.RANGE))
            .projection(Projection.builder().projectionType(projection).build())
            .build()

        val attributes =
            listOfNotNull(PK, SK, GSI1_PK.takeIf { gsi1 }, GSI1_SK.takeIf { gsi1 }, GSI2_PK.takeIf { gsi2 }, GSI2_SK.takeIf { gsi2 })
                .map {
                    AttributeDefinition
                        .builder()
                        .attributeName(it)
                        .attributeType(ScalarAttributeType.S)
                        .build()
                }
        val indexes = listOfNotNull(index(GSI1, GSI1_PK, GSI1_SK).takeIf { gsi1 }, index(GSI2, GSI2_PK, GSI2_SK).takeIf { gsi2 })
        try {
            client.createTable {
                it
                    .tableName(name)
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(attributes)
                    .keySchema(key(PK, KeyType.HASH), key(SK, KeyType.RANGE))
                    .apply { if (indexes.isNotEmpty()) globalSecondaryIndexes(indexes) }
                    .apply { if (streamsEnabled) streamSpecification(streamSpecification) }
            }
            client.waiter().waitUntilTableExists { it.tableName(name) }
            client.updateTimeToLive {
                it.tableName(name).timeToLiveSpecification(
                    TimeToLiveSpecification
                        .builder()
                        .attributeName(TTL)
                        .enabled(true)
                        .build(),
                )
            }
            logger.info { "DynamoDB table $name created" }
        } catch (_: ResourceInUseException) {
            logger.debug { "DynamoDB table $name already exists" }
            if (streamsEnabled && client
                    .describeTable { it.tableName(name) }
                    .table()
                    .streamSpecification()
                    ?.streamEnabled() != true
            ) {
                client.updateTable { it.tableName(name).streamSpecification(streamSpecification) }
                logger.info { "DynamoDB stream of the table $name enabled" }
            }
        }
    }

    /** Runs a query and follows the pagination until the end. */
    fun queryAll(request: QueryRequest): List<Map<String, AttributeValue>> {
        val items = mutableListOf<Map<String, AttributeValue>>()
        var startKey: Map<String, AttributeValue>? = null
        do {
            val response =
                client.query(
                    request
                        .toBuilder()
                        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                        .apply { if (startKey != null) exclusiveStartKey(startKey) }
                        .build(),
                )
            recordRead(response.consumedCapacity())
            items.addAll(response.items())
            startKey = response.lastEvaluatedKey().takeIf { response.hasLastEvaluatedKey() && it.isNotEmpty() }
        } while (startKey != null)
        return items
    }

    fun queryPartition(
        table: String,
        pk: String,
        skPrefix: String? = null,
        consistentRead: Boolean = false,
    ): List<Map<String, AttributeValue>> =
        queryAll(
            QueryRequest
                .builder()
                .tableName(table)
                .consistentRead(consistentRead)
                .apply {
                    if (skPrefix == null) {
                        keyConditionExpression("#pk = :pk")
                            .expressionAttributeNames(mapOf("#pk" to PK))
                            .expressionAttributeValues(mapOf(":pk" to s(pk)))
                    } else {
                        keyConditionExpression("#pk = :pk AND begins_with(#sk, :sk)")
                            .expressionAttributeNames(mapOf("#pk" to PK, "#sk" to SK))
                            .expressionAttributeValues(mapOf(":pk" to s(pk), ":sk" to s(skPrefix)))
                    }
                }.build(),
        )

    fun queryIndex(
        table: String,
        index: String,
        pkName: String,
        pk: String,
        skName: String? = null,
        skPrefix: String? = null,
    ): List<Map<String, AttributeValue>> =
        queryAll(
            QueryRequest
                .builder()
                .tableName(table)
                .indexName(index)
                .apply {
                    if (skPrefix == null || skName == null) {
                        keyConditionExpression("#pk = :pk")
                            .expressionAttributeNames(mapOf("#pk" to pkName))
                            .expressionAttributeValues(mapOf(":pk" to s(pk)))
                    } else {
                        keyConditionExpression("#pk = :pk AND begins_with(#sk, :sk)")
                            .expressionAttributeNames(mapOf("#pk" to pkName, "#sk" to skName))
                            .expressionAttributeValues(mapOf(":pk" to s(pk), ":sk" to s(skPrefix)))
                    }
                }.build(),
        )

    fun getItem(
        table: String,
        pk: String,
        sk: String,
    ): Map<String, AttributeValue>? =
        client
            .getItem {
                it
                    .tableName(table)
                    .key(key(pk, sk))
                    .consistentRead(true)
                    .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
            }.let { response ->
                recordRead(response.consumedCapacity())
                response.item().takeIf { response.hasItem() && it.isNotEmpty() }
            }

    fun putItem(
        table: String,
        item: Map<String, AttributeValue>,
    ) {
        client
            .putItem { it.tableName(table).item(item).returnConsumedCapacity(ReturnConsumedCapacity.TOTAL) }
            .also { recordWrite(it.consumedCapacity()) }
    }

    fun deleteItem(
        table: String,
        pk: String,
        sk: String,
    ) {
        client
            .deleteItem { it.tableName(table).key(key(pk, sk)).returnConsumedCapacity(ReturnConsumedCapacity.TOTAL) }
            .also { recordWrite(it.consumedCapacity()) }
    }

    /** Puts [item] only if no item has its key: false if one exists. */
    fun putIfAbsent(
        table: String,
        item: Map<String, AttributeValue>,
    ): Boolean =
        try {
            client
                .putItem {
                    it
                        .tableName(table)
                        .item(item)
                        .conditionExpression("attribute_not_exists(#pk)")
                        .expressionAttributeNames(mapOf("#pk" to PK))
                        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                }.also { recordWrite(it.consumedCapacity()) }
            true
        } catch (_: ConditionalCheckFailedException) {
            false
        }

    /** Writes [items] in one transaction (100 items at most): false if a condition failed. */
    fun transactWrite(items: List<TransactWriteItem>): Boolean =
        try {
            client
                .transactWriteItems { it.transactItems(items).returnConsumedCapacity(ReturnConsumedCapacity.TOTAL) }
                .also { recordWrite(it.consumedCapacity()) }
            true
        } catch (e: TransactionCanceledException) {
            if (e.cancellationReasons().none { it.code() == "ConditionalCheckFailed" }) throw e
            false
        }

    fun transactPut(
        table: String,
        item: Map<String, AttributeValue>,
        condition: String? = null,
        names: Map<String, String> = emptyMap(),
        values: Map<String, AttributeValue> = emptyMap(),
    ): TransactWriteItem =
        TransactWriteItem
            .builder()
            .put(
                Put
                    .builder()
                    .tableName(table)
                    .item(item)
                    .apply {
                        if (condition != null) {
                            conditionExpression(condition)
                            if (names.isNotEmpty()) expressionAttributeNames(names)
                            if (values.isNotEmpty()) expressionAttributeValues(values)
                        }
                    }.build(),
            ).build()

    fun transactDelete(
        table: String,
        key: Map<String, AttributeValue>,
    ): TransactWriteItem =
        TransactWriteItem
            .builder()
            .delete(
                Delete
                    .builder()
                    .tableName(table)
                    .key(key)
                    .build(),
            ).build()

    /** Batch writes (25 items per request), retrying the unprocessed items. */
    fun batchWrite(
        table: String,
        puts: List<Map<String, AttributeValue>> = emptyList(),
        deleteKeys: List<Map<String, AttributeValue>> = emptyList(),
    ) {
        val requests =
            puts.map { WriteRequest.builder().putRequest(PutRequest.builder().item(it).build()).build() } +
                deleteKeys.map { WriteRequest.builder().deleteRequest(DeleteRequest.builder().key(it).build()).build() }
        requests.chunked(25).forEach { chunk ->
            var pending: Map<String, List<WriteRequest>> = mapOf(table to chunk)
            var attempt = 0
            while (pending.isNotEmpty()) {
                val response =
                    client.batchWriteItem(
                        BatchWriteItemRequest
                            .builder()
                            .requestItems(pending)
                            .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                            .build(),
                    )
                recordWrite(response.consumedCapacity())
                pending = response.unprocessedItems().filterValues { it.isNotEmpty() }
                if (pending.isNotEmpty()) {
                    check(++attempt < 10) { "too many unprocessed items for $table" }
                    Thread.sleep(50L shl attempt.coerceAtMost(6))
                }
            }
        }
    }

    /** Deletes every item of a partition. */
    fun deletePartition(
        table: String,
        pk: String,
    ): Int {
        val keys = queryPartition(table, pk, consistentRead = true).map { keyOf(it) }
        batchWrite(table, deleteKeys = keys)
        return keys.size
    }

    fun keyOf(item: Map<String, AttributeValue>): Map<String, AttributeValue> = mapOf(PK to item.getValue(PK), SK to item.getValue(SK))

    fun key(
        pk: String,
        sk: String,
    ): Map<String, AttributeValue> = mapOf(PK to s(pk), SK to s(sk))
}

@InternalTockApi
fun s(value: String): AttributeValue = AttributeValue.fromS(value)

@InternalTockApi
fun n(value: Number): AttributeValue = AttributeValue.fromN(value.toString())

@InternalTockApi
fun b(value: ByteArray): AttributeValue = AttributeValue.fromB(SdkBytes.fromByteArray(value))

@InternalTockApi
fun bool(value: Boolean): AttributeValue = AttributeValue.fromBool(value)

@InternalTockApi
fun Map<String, AttributeValue>.str(name: String): String? = get(name)?.s()

@InternalTockApi
fun Map<String, AttributeValue>.long(name: String): Long? = get(name)?.n()?.toLong()

@InternalTockApi
fun Map<String, AttributeValue>.bytes(name: String): ByteArray = getValue(name).b().asByteArray()

/**
 * Names `_id` the property `_id`: Jackson 3 does not keep it, KMongo has the same introspector.
 */
private object MongoIdIntrospector : NopAnnotationIntrospector() {
    private val ID = PropertyName.construct("_id")

    private fun isId(a: Annotated): Boolean =
        when (val member = a.annotated) {
            is java.lang.reflect.Field -> member.name == "_id"
            is java.lang.reflect.Method -> member.name == "get_id" || member.name == "set_id"
            else -> false
        }

    override fun findNameForSerialization(
        config: MapperConfig<*>,
        a: Annotated,
    ): PropertyName? = if (isId(a)) ID else null

    override fun findNameForDeserialization(
        config: MapperConfig<*>,
        a: Annotated,
    ): PropertyName? = if (isId(a)) ID else null
}

/**
 * Serializes the storage model (the *Col classes of tock-bot-storage-model) in JSON, then gzip.
 *
 * The (de)serializers are the ones the MongoDB storage uses, generated from the same classes: only the
 * container changes, JSON instead of BSON, so that the serverless runtime needs neither the MongoDB driver nor bson.
 */
object DocumentCodec {
    val mapper: JsonMapper =
        JsonMapper
            .builder()
            .registerModulesFromServiceLoader()
            .addModule(KotlinModule.Builder().build())
            .addModule(IdJacksonModule())
            // the classes without generated (de)serializers keep their _id, as with KMongo
            .annotationIntrospector(
                AnnotationIntrospectorPair(
                    MongoIdIntrospector,
                    JsonMapper
                        .builder()
                        .build()
                        .serializationConfig()
                        .annotationIntrospector,
                ),
            ).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

    fun encode(value: Any): ByteArray = gzip(mapper.writeValueAsBytes(value))

    fun <T : Any> decode(
        bytes: ByteArray,
        type: Class<T>,
    ): T = mapper.readValue(gunzip(bytes), type)

    inline fun <reified T : Any> decode(bytes: ByteArray): T = decode(bytes, T::class.java)

    /** The JSON tree of a document, to update it without converting it to the domain model. */
    fun tree(bytes: ByteArray): JsonNode = mapper.readTree(gunzip(bytes))

    fun <T : Any> fromTree(
        node: JsonNode,
        type: Class<T>,
    ): T = mapper.treeToValue(node, type)

    private fun gzip(bytes: ByteArray): ByteArray =
        ByteArrayOutputStream().use { out ->
            GZIPOutputStream(out).use { it.write(bytes) }
            out.toByteArray()
        }

    private fun gunzip(bytes: ByteArray): ByteArray = GZIPInputStream(bytes.inputStream()).use { it.readBytes() }
}
