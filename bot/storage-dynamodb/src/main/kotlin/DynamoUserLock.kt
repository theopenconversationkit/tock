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

import ai.tock.bot.engine.user.LeasedUserLock
import ai.tock.shared.longProperty
import ai.tock.storage.dynamodb.DynamoStore.client
import ai.tock.storage.dynamodb.DynamoStore.runtimeTable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity
import java.time.Instant

/**
 * The [LeasedUserLock] of DynamoDB: one item per user in the runtime table (`LOCK#<userId>` / `L`), taken, renewed
 * and released by conditional writes, so that two instances never hold the same lease.
 *
 * The lease starts at `date` (milliseconds) and lasts `tock_bot_lock_timeout_in_ms`. The DynamoDB TTL removes the
 * items `tock_dynamodb_user_lock_ttl_hours` (6) after their last write, as the MongoDB index does.
 */
object DynamoUserLock : LeasedUserLock() {
    private val logger = KotlinLogging.logger {}

    private val itemTtlSeconds = longProperty("tock_dynamodb_user_lock_ttl_hours", 6) * 3600

    private const val SORT_KEY = "L"
    private const val LOCKED = "locked"
    private const val DATE = "date"
    private const val LOCK_ID = "lockId"

    private fun key(userId: String) = DynamoStore.key("LOCK#$userId", SORT_KEY)

    private fun ttl(now: Instant): AttributeValue = n(now.epochSecond + itemTtlSeconds)

    private val names = mapOf("#pk" to PK, "#locked" to LOCKED, "#date" to DATE, "#lockId" to LOCK_ID, "#ttl" to TTL)

    /** The names of [expressions] only: DynamoDB rejects the unused ones. */
    private fun namesOf(vararg expressions: String): Map<String, String> = names.filterKeys { name -> expressions.any { name in it } }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    /** Runs a conditional write: false if the condition failed. */
    private suspend fun conditionalWrite(write: () -> Unit): Boolean =
        io {
            try {
                write()
                true
            } catch (_: ConditionalCheckFailedException) {
                false
            }
        }

    override suspend fun acquire(
        userId: String,
        lockId: String?,
    ): Boolean =
        try {
            val now = Instant.now()
            val condition = "attribute_not_exists(#pk) OR #locked = :false OR #date < :limit"
            conditionalWrite {
                client
                    .putItem {
                        it
                            .tableName(runtimeTable)
                            .item(
                                key(userId) +
                                    mapOf(LOCKED to bool(true), DATE to n(now.toEpochMilli()), TTL to ttl(now)) +
                                    listOfNotNull(lockId?.let { id -> LOCK_ID to s(id) }),
                            ).conditionExpression(condition)
                            .expressionAttributeNames(namesOf(condition))
                            .expressionAttributeValues(mapOf(":false" to bool(false), ":limit" to n(now.toEpochMilli() - lockTimeout)))
                            .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                    }.also { DynamoStore.recordWrite(it.consumedCapacity()) }
            }.also { if (!it) logger.debug { "lock for user $userId already taken" } }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "unable to take the lock of user $userId" }
            false
        }

    override suspend fun renew(
        userId: String,
        lockId: String,
    ): Boolean? =
        try {
            val now = Instant.now()
            val update = "SET #date = :now, #ttl = :ttl"
            val condition = "#lockId = :id"
            conditionalWrite {
                client
                    .updateItem {
                        it
                            .tableName(runtimeTable)
                            .key(key(userId))
                            .updateExpression(update)
                            .conditionExpression(condition)
                            .expressionAttributeNames(namesOf(update, condition))
                            .expressionAttributeValues(mapOf(":now" to n(now.toEpochMilli()), ":ttl" to ttl(now), ":id" to s(lockId)))
                            .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                    }.also { DynamoStore.recordWrite(it.consumedCapacity()) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "unable to renew the lock lease for user $userId" }
            null
        }

    override suspend fun isOwner(
        userId: String,
        lockId: String,
    ): Boolean? =
        try {
            io { DynamoStore.getItem(runtimeTable, "LOCK#$userId", SORT_KEY) }
                ?.let { it[LOCKED]?.bool() == true && it.str(LOCK_ID) == lockId }
                ?: false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "unable to check the lock ownership for user $userId" }
            null
        }

    override suspend fun release(
        userId: String,
        lockId: String?,
    ): Boolean {
        val update = "SET #locked = :false"
        val condition = if (lockId == null) "#locked = :true AND attribute_not_exists(#lockId)" else "#locked = :true AND #lockId = :id"
        return conditionalWrite {
            client
                .updateItem {
                    it
                        .tableName(runtimeTable)
                        .key(key(userId))
                        .updateExpression(update)
                        .conditionExpression(condition)
                        .expressionAttributeNames(namesOf(update, condition))
                        .expressionAttributeValues(
                            mapOf(":false" to bool(false), ":true" to bool(true)) + listOfNotNull(lockId?.let { id -> ":id" to s(id) }),
                        ).returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                }.also { DynamoStore.recordWrite(it.consumedCapacity()) }
        }
    }

    /** Removes the lock of [userId], whoever holds it. */
    suspend fun deleteLock(userId: String) {
        io { deleteLockItem(userId) }
    }

    internal fun deleteLockItem(userId: String) {
        DynamoStore.deleteItem(runtimeTable, "LOCK#$userId", SORT_KEY)
    }
}
