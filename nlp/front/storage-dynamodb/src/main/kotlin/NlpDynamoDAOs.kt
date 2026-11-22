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

package ai.tock.nlp.front.storage.dynamodb

import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.front.service.storage.ApplicationDefinitionRuntimeDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceRuntimeDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionRuntimeDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionRuntimeDAO
import ai.tock.nlp.front.service.storage.NamespaceConfigurationRuntimeDAO
import ai.tock.nlp.front.service.storage.ParseRequestLogRuntimeDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.shared.longProperty
import ai.tock.shared.normalize
import ai.tock.storage.dynamodb.DOC
import ai.tock.storage.dynamodb.DocumentCodec
import ai.tock.storage.dynamodb.DynamoStore
import ai.tock.storage.dynamodb.DynamoStore.GSI1
import ai.tock.storage.dynamodb.DynamoStore.GSI1_PK
import ai.tock.storage.dynamodb.DynamoStore.GSI1_SK
import ai.tock.storage.dynamodb.DynamoStore.configTable
import ai.tock.storage.dynamodb.DynamoStore.runtimeTable
import ai.tock.storage.dynamodb.DynamoStore.sentenceTable
import ai.tock.storage.dynamodb.PK
import ai.tock.storage.dynamodb.SK
import ai.tock.storage.dynamodb.TTL
import ai.tock.storage.dynamodb.b
import ai.tock.storage.dynamodb.bytes
import ai.tock.storage.dynamodb.n
import ai.tock.storage.dynamodb.s
import ai.tock.storage.dynamodb.str
import mu.KotlinLogging
import org.litote.kmongo.Id
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.time.Instant
import java.util.Locale
import java.util.UUID
import kotlin.math.min

private val logger = KotlinLogging.logger {}

/**
 * Documents of the configuration table in one partition, read at once: the NLP configuration has a few hundred
 * documents, cached by the `ConfigurationRepository` of the NLP.
 */
class NlpDocuments<T : Any>(
    private val partition: String,
    private val type: Class<T>,
    private val keyOf: (T) -> String,
) {
    fun all(): List<T> = DynamoStore.queryPartition(configTable, partition, consistentRead = true).map { DocumentCodec.decode(it.bytes(DOC), type) }

    fun get(key: String): T? = DynamoStore.getItem(configTable, partition, key)?.let { DocumentCodec.decode(it.bytes(DOC), type) }

    fun save(document: T) {
        DynamoStore.putItem(configTable, DynamoStore.key(partition, keyOf(document)) + (DOC to b(DocumentCodec.encode(document))))
    }

    fun delete(key: String) {
        DynamoStore.deleteItem(configTable, partition, key)
    }

    fun listen(name: String) {
        logger.info { "no change stream on DynamoDB: the $name changes are not pushed" }
    }
}

object ApplicationDefinitionDynamoDAO : ApplicationDefinitionRuntimeDAO {
    val documents = NlpDocuments("NLPAPP", ApplicationDefinition::class.java) { it._id.toString() }

    override fun listenApplicationDefinitionChanges(listener: () -> Unit) = documents.listen("application")

    override fun getApplications(): List<ApplicationDefinition> = documents.all()

    fun getApplicationByNamespaceAndName(
        namespace: String,
        name: String,
    ): ApplicationDefinition? = getApplications().firstOrNull { it.namespace == namespace && it.name == name }
}

object IntentDefinitionDynamoDAO : IntentDefinitionRuntimeDAO {
    val documents = NlpDocuments("NLPINT", IntentDefinition::class.java) { it._id.toString() }

    override fun listenIntentDefinitionChanges(listener: () -> Unit) = documents.listen("intent")

    override fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition> = documents.all().filter { applicationId in it.applications }

    fun getIntentById(id: Id<IntentDefinition>): IntentDefinition? = documents.get(id.toString())

    fun getIntentByNamespaceAndName(
        namespace: String,
        name: String,
    ): IntentDefinition? = documents.all().firstOrNull { it.namespace == namespace && it.name == name }
}

object EntityTypeDefinitionDynamoDAO : EntityTypeDefinitionRuntimeDAO {
    val entityTypes = NlpDocuments("NLPENT", EntityTypeDefinition::class.java) { it.name }
    val dictionaries = NlpDocuments("NLPDICT", DictionaryData::class.java) { "${it.namespace}:${it.entityName}" }

    override fun listenEntityTypeChanges(listener: () -> Unit) = entityTypes.listen("entity type")

    override fun save(entityType: EntityTypeDefinition) = entityTypes.save(entityType)

    override fun getEntityTypes(): List<EntityTypeDefinition> = entityTypes.all()

    override fun save(data: DictionaryData) = dictionaries.save(data)

    override fun getAllDictionaryData(): List<DictionaryData> = dictionaries.all()

    override fun listenDictionaryDataChanges(listener: () -> Unit) = dictionaries.listen("dictionary")
}

object NamespaceConfigurationDynamoDAO : NamespaceConfigurationRuntimeDAO {
    override fun listenNamespaceConfigurationChanges(listener: () -> Unit) {
        logger.info { "no change stream on DynamoDB: the namespace configuration changes are not pushed" }
    }
}

/**
 * The sentences of an application and a language, in the partition `SENT#<applicationId>#<language>` of the
 * sentence table, by text (the Mongo unique index is text + language + application). The normalized text is the
 * key of the index `gsi1`, for the exact search of the applications that normalize the texts.
 *
 * Only the exact search of the parser runs on DynamoDB: the search of the Studio stays in the admin, on MongoDB.
 * The unknown counter is an attribute incremented by an atomic `ADD`, replicated as increments.
 *
 * The admin publishes its validated sentences ([publish]), with the fingerprint of their published content in
 * the attribute [FINGERPRINT]; the runtime writes the sentences of the inbox ([save]), and never replaces a
 * published sentence.
 */
object ClassifiedSentenceDynamoDAO : ClassifiedSentenceRuntimeDAO {
    const val STATUS = "status"
    const val UNKNOWN_COUNT = "unknownCount"
    const val FINGERPRINT = "fp"

    /** The key of a sentence in the sentence table. */
    data class SentenceKey(
        val pk: String,
        val sk: String,
    )

    private fun textKey(text: String): String = if (text.length > 512) text.substring(0, min(512, text.length)) else text

    private fun pk(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
    ) = "SENT#$applicationId#$language"

    private fun normalizedPk(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
        text: String,
    ) = "SENTN#$applicationId#$language#${text.normalize(language)}"

    fun keyOf(sentence: ClassifiedSentence): SentenceKey = SentenceKey(pk(sentence.applicationId, sentence.language), textKey(sentence.text))

    private fun item(sentence: ClassifiedSentence): Map<String, AttributeValue> =
        keyOf(sentence).let { DynamoStore.key(it.pk, it.sk) } +
            mapOf(
                DOC to b(DocumentCodec.encode(sentence)),
                STATUS to s(sentence.status.name),
                GSI1_PK to s(normalizedPk(sentence.applicationId, sentence.language, sentence.text)),
                GSI1_SK to s(textKey(sentence.text)),
            )

    /**
     * Saves a sentence of the runtime (the inbox), unless the admin has published a sentence with the same text:
     * the parser has read the sentence before the publication, the published one is kept.
     */
    fun save(sentence: ClassifiedSentence) {
        try {
            DynamoStore.client
                .putItem {
                    it
                        .tableName(sentenceTable)
                        .item(item(sentence))
                        .conditionExpression("attribute_not_exists(#fp)")
                        .expressionAttributeNames(mapOf("#fp" to FINGERPRINT))
                        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                }.also { DynamoStore.recordWrite(it.consumedCapacity()) }
        } catch (_: ConditionalCheckFailedException) {
            logger.debug { "the sentence ${sentence.text} is published by the admin: not replaced" }
        }
    }

    fun delete(sentence: ClassifiedSentence) {
        DynamoStore.deleteItem(sentenceTable, pk(sentence.applicationId, sentence.language), textKey(sentence.text))
    }

    /** Publishes sentences of the admin, with the fingerprints of their content, in batches. */
    fun publish(sentences: List<Pair<ClassifiedSentence, String>>) {
        DynamoStore.batchWrite(sentenceTable, puts = sentences.map { (sentence, fingerprint) -> item(sentence) + (FINGERPRINT to s(fingerprint)) })
    }

    /** Deletes published sentences, in batches. */
    fun unpublish(keys: Collection<SentenceKey>) {
        DynamoStore.batchWrite(sentenceTable, deleteKeys = keys.map { DynamoStore.key(it.pk, it.sk) })
    }

    /** The fingerprints of the published sentences, by key: a scan of the table, read when the admin starts. */
    fun publishedFingerprints(): Map<SentenceKey, String> {
        val fingerprints = mutableMapOf<SentenceKey, String>()
        var startKey: Map<String, AttributeValue>? = null
        do {
            val response =
                DynamoStore.client.scan(
                    ScanRequest
                        .builder()
                        .tableName(sentenceTable)
                        .consistentRead(true)
                        .filterExpression("attribute_exists(#fp)")
                        .projectionExpression("#pk, #sk, #fp")
                        .expressionAttributeNames(mapOf("#pk" to PK, "#sk" to SK, "#fp" to FINGERPRINT))
                        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                        .apply { if (startKey != null) exclusiveStartKey(startKey) }
                        .build(),
                )
            DynamoStore.recordRead(response.consumedCapacity())
            response.items().forEach { item -> fingerprints[SentenceKey(item.getValue(PK).s(), item.getValue(SK).s())] = item.getValue(FINGERPRINT).s() }
            startKey = response.lastEvaluatedKey().takeIf { response.hasLastEvaluatedKey() && it.isNotEmpty() }
        } while (startKey != null)
        return fingerprints
    }

    /** The sentence whose text is [text] (or whose normalized text is the one of [text]), if its status is in [status]. */
    fun findExact(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
        text: String,
        status: Set<ClassifiedSentenceStatus>,
        normalizeText: Boolean,
    ): ClassifiedSentence? {
        val items =
            if (normalizeText) {
                DynamoStore.queryIndex(sentenceTable, GSI1, GSI1_PK, normalizedPk(applicationId, language, text))
            } else {
                listOfNotNull(DynamoStore.getItem(sentenceTable, pk(applicationId, language), textKey(text)))
            }
        return items
            .filter { item -> status.isEmpty() || item.str(STATUS) in status.map { it.name } }
            .sortedBy { it.str(SK) }
            .firstOrNull()
            ?.let { DocumentCodec.decode<ClassifiedSentence>(it.bytes(DOC)) }
    }

    /** The sentences of an application and a language, for the publication. */
    fun getSentences(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
    ): List<ClassifiedSentence> =
        DynamoStore
            .queryPartition(sentenceTable, pk(applicationId, language), consistentRead = true)
            .map { DocumentCodec.decode<ClassifiedSentence>(it.bytes(DOC)) }

    override fun incrementUnknownStat(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
        text: String,
    ) {
        // as with MongoDB, only an existing sentence is counted
        try {
            DynamoStore.client.updateItem {
                it
                    .tableName(sentenceTable)
                    .key(DynamoStore.key(pk(applicationId, language), textKey(text)))
                    .updateExpression("ADD #count :one")
                    .conditionExpression("attribute_exists(#sk)")
                    .expressionAttributeNames(mapOf("#count" to UNKNOWN_COUNT, "#sk" to SK))
                    .expressionAttributeValues(mapOf(":one" to n(1)))
            }
        } catch (_: ConditionalCheckFailedException) {
            logger.debug { "no sentence for the unknown stat of $text" }
        }
    }
}

/**
 * The logs of the parse requests: the runtime only writes them, the Studio reads them in MongoDB once replicated.
 * They live in the runtime table, `PARSELOG#<applicationId>` / `<date>#<uuid>`, for `tock_dynamodb_parse_log_ttl_days` (7).
 */
object ParseRequestLogDynamoDAO : ParseRequestLogRuntimeDAO {
    private val ttlSeconds = longProperty("tock_dynamodb_parse_log_ttl_days", 7) * 24 * 3600

    override fun save(log: ParseRequestLog) {
        DynamoStore.putItem(
            runtimeTable,
            DynamoStore.key("PARSELOG#${log.applicationId}", "${log.date.toEpochMilli().toString().padStart(15, '0')}#${UUID.randomUUID()}") +
                mapOf(DOC to b(DocumentCodec.encode(log)), TTL to n(Instant.now().epochSecond + ttlSeconds)),
        )
    }
}
