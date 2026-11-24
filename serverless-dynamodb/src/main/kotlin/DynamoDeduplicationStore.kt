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

package ai.tock.serverless.dynamodb

import ai.tock.serverless.queue.DeduplicationStore
import ai.tock.storage.dynamodb.DynamoStore
import ai.tock.storage.dynamodb.PK
import ai.tock.storage.dynamodb.SK
import ai.tock.storage.dynamodb.TTL
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import java.time.Duration
import java.time.Instant

/**
 * The [DeduplicationStore] shared by the instances: one item per request in the runtime table of
 * [DynamoStore] (`DEDUP#<key>` / `D`), written only if it is absent or expired.
 *
 * The DynamoDB TTL removes the expired items, but late (up to a few days): the condition reads the expiration itself.
 */
class DynamoDeduplicationStore(
    private val table: String = DynamoStore.runtimeTable,
) : DeduplicationStore {
    private fun key(key: String) = mapOf(PK to AttributeValue.fromS("DEDUP#$key"), SK to AttributeValue.fromS("D"))

    override fun markIfAbsent(
        key: String,
        ttl: Duration,
    ): Boolean {
        val now = Instant.now()
        return try {
            DynamoStore.client.putItem {
                it
                    .tableName(table)
                    .item(key(key) + (TTL to AttributeValue.fromN((now + ttl).epochSecond.toString())))
                    .conditionExpression("attribute_not_exists(#pk) OR #ttl <= :now")
                    .expressionAttributeNames(mapOf("#pk" to PK, "#ttl" to TTL))
                    .expressionAttributeValues(mapOf(":now" to AttributeValue.fromN(now.epochSecond.toString())))
            }
            true
        } catch (_: ConditionalCheckFailedException) {
            false
        }
    }

    override fun unmark(key: String) {
        DynamoStore.client.deleteItem { it.tableName(table).key(key(key)) }
    }
}
