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
import ai.tock.shared.cache.TockCache
import ai.tock.shared.jackson.AnyValueWrapper
import ai.tock.storage.dynamodb.DynamoStore.runtimeTable
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

/**
 * The [TockCache] of the serverless runtime (the `cache` collection of MongoDB): one partition by type
 * (`CACHE#<type>`), one item by id, in the runtime table. The strings and the bytes are stored as they are, the
 * other values as an [AnyValueWrapper], as MongoDB does.
 *
 * A DynamoDB item has at most 400 KB: the uploaded files need an S3 storage (`tock_bot_files_storage=s3`).
 * The cache is not replicated to MongoDB.
 */
@OptIn(InternalTockApi::class)
object DynamoTockCache : TockCache {
    private val logger = KotlinLogging.logger {}

    private const val STRING = "s"
    private const val BYTES = "b"

    private fun pk(type: String) = "CACHE#$type"

    override fun <T> get(
        id: Id<T>,
        type: String,
    ): T? =
        try {
            @Suppress("UNCHECKED_CAST")
            DynamoStore.getItem(runtimeTable, pk(type), id.toString())?.let { value(it) } as T?
        } catch (e: Exception) {
            logger.error(e) { "the cache item $type $id cannot be read: it is removed" }
            remove(id, type)
            null
        }

    override fun <T : Any> put(
        id: Id<T>,
        type: String,
        data: T,
    ) {
        val value =
            when (data) {
                is String -> STRING to s(data)
                is ByteArray -> BYTES to b(data)
                else -> DOC to b(DocumentCodec.encode(AnyValueWrapper(data)))
            }
        DynamoStore.putItem(runtimeTable, DynamoStore.key(pk(type), id.toString()) + value)
    }

    override fun <T> getAll(type: String): Map<Id<T>, Any> =
        DynamoStore
            .queryPartition(runtimeTable, pk(type))
            .associate { item -> item.getValue(SK).s().toId<T>() to value(item) }

    override fun <T> remove(
        id: Id<T>,
        type: String,
    ) {
        DynamoStore.deleteItem(runtimeTable, pk(type), id.toString())
    }

    private fun value(item: Map<String, AttributeValue>): Any =
        item.str(STRING)
            ?: item[BYTES]?.b()?.asByteArray()
            ?: checkNotNull(DocumentCodec.decode<AnyValueWrapper>(item.bytes(DOC)).value) { "no value in the cache item ${item[PK]?.s()} ${item[SK]?.s()}" }
}
