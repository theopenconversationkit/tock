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

package ai.tock.storage.dynamodb.publication

import ai.tock.storage.dynamodb.DynamoStore
import ai.tock.storage.dynamodb.PK
import ai.tock.storage.dynamodb.SK
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryRequest

/**
 * The keys published by the admin, by kind, in the partition `PUB` of the configuration table.
 *
 * Only these keys are deleted from DynamoDB when they disappear from MongoDB: the runtime also writes in DynamoDB
 * (the configurations created at the installation of a bot, the built-in stories, the registered features, the
 * created labels), and these writes must not be deleted because MongoDB does not know them yet.
 */
internal object PublicationManifest {
    private const val PARTITION = "PUB"
    private const val REF = "ref"

    /** The published keys of [kind], with the reference needed to delete each of them. */
    fun published(kind: String): Map<String, Map<String, String>> {
        val items = mutableListOf<Map<String, AttributeValue>>()
        var startKey: Map<String, AttributeValue>? = null
        do {
            val response =
                DynamoStore.client.query(
                    QueryRequest
                        .builder()
                        .tableName(DynamoStore.configTable)
                        .consistentRead(true)
                        .keyConditionExpression("#pk = :pk AND begins_with(#sk, :sk)")
                        .expressionAttributeNames(mapOf("#pk" to PK, "#sk" to SK))
                        .expressionAttributeValues(mapOf(":pk" to AttributeValue.fromS(PARTITION), ":sk" to AttributeValue.fromS("$kind#")))
                        .apply { if (startKey != null) exclusiveStartKey(startKey) }
                        .build(),
                )
            items += response.items()
            startKey = response.lastEvaluatedKey().takeIf { response.hasLastEvaluatedKey() && it.isNotEmpty() }
        } while (startKey != null)
        return items.associate { item ->
            item.getValue(SK).s().removePrefix("$kind#") to (item[REF]?.m()?.mapValues { it.value.s() } ?: emptyMap())
        }
    }

    fun add(
        kind: String,
        key: String,
        ref: Map<String, String>,
    ) {
        DynamoStore.client.putItem {
            it.tableName(DynamoStore.configTable).item(
                mapOf(
                    PK to AttributeValue.fromS(PARTITION),
                    SK to AttributeValue.fromS("$kind#$key"),
                    REF to AttributeValue.fromM(ref.mapValues { (_, value) -> AttributeValue.fromS(value) }),
                ),
            )
        }
    }

    fun remove(
        kind: String,
        key: String,
    ) {
        DynamoStore.client.deleteItem {
            it.tableName(DynamoStore.configTable).key(mapOf(PK to AttributeValue.fromS(PARTITION), SK to AttributeValue.fromS("$kind#$key")))
        }
    }
}
