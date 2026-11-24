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

package ai.tock.storage.dynamodb.replication

import ai.tock.storage.dynamodb.DynamoStore
import com.mongodb.kotlin.client.MongoCollection
import mu.KotlinLogging
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import software.amazon.awssdk.services.dynamodb.model.DescribeStreamRequest
import software.amazon.awssdk.services.dynamodb.model.ExpiredIteratorException
import software.amazon.awssdk.services.dynamodb.model.Record
import software.amazon.awssdk.services.dynamodb.model.Shard
import software.amazon.awssdk.services.dynamodb.model.ShardIteratorType
import software.amazon.awssdk.services.dynamodb.model.TrimmedDataAccessException

/** The position of the replication in a shard of a DynamoDB stream, stored in MongoDB. */
data class StreamCheckpoint(
    val _id: String,
    val sequenceNumber: String? = null,
    val finished: Boolean = false,
)

/**
 * Reads the DynamoDB stream of [table], shard after shard, and gives each record to [handler], once its parent
 * shards are read: the records of an item are given in order.
 *
 * The position in each shard is stored in [checkpoints] after each batch: a record is given at least once
 * (again if the process stops before the checkpoint), so [handler] must be idempotent. A record that fails is
 * given again at the next poll, at most [maxAttempts] times, then skipped.
 */
internal class DynamoStreamReader(
    private val table: String,
    private val checkpoints: MongoCollection<StreamCheckpoint>,
    private val maxAttempts: Int = 5,
    private val handler: (Record) -> Unit,
) {
    private val logger = KotlinLogging.logger {}

    private val failures = mutableMapOf<String, Int>()

    private fun streamArn(): String? =
        DynamoStore.client
            .describeTable { it.tableName(table) }
            .table()
            .latestStreamArn()

    private fun shards(streamArn: String): List<Shard> {
        val shards = mutableListOf<Shard>()
        var start: String? = null
        do {
            val description =
                DynamoStore.streamsClient
                    .describeStream(
                        DescribeStreamRequest
                            .builder()
                            .streamArn(streamArn)
                            .apply { if (start != null) exclusiveStartShardId(start) }
                            .build(),
                    ).streamDescription()
            shards += description.shards()
            start = description.lastEvaluatedShardId()
        } while (start != null)
        return shards
    }

    /** Reads the available records once. Returns the number of records given to [handler]. */
    fun poll(): Int {
        val streamArn = streamArn() ?: return 0
        val shards = shards(streamArn)
        val ids = shards.map { it.shardId() }.toSet()

        fun checkpoint(shardId: String) = checkpoints.findOneById("$streamArn|$shardId")
        var count = 0
        var progress = true
        val done = mutableSetOf<String>()
        // a child shard is read once its parent is finished: loop while shards finish
        while (progress) {
            progress = false
            shards
                .filter { it.shardId() !in done }
                .filter { shard ->
                    val parent = shard.parentShardId()
                    parent == null || parent !in ids || checkpoint(parent)?.finished == true
                }.forEach { shard ->
                    done += shard.shardId()
                    val (read, finished) = readShard(streamArn, shard, checkpoint(shard.shardId()))
                    count += read
                    if (finished) progress = true
                }
        }
        return count
    }

    /** Reads [shard] from its checkpoint: the number of records read, and true if the shard is finished. */
    private fun readShard(
        streamArn: String,
        shard: Shard,
        checkpoint: StreamCheckpoint?,
    ): Pair<Int, Boolean> {
        if (checkpoint?.finished == true) return 0 to true
        val id = "$streamArn|${shard.shardId()}"
        var iterator =
            try {
                shardIterator(streamArn, shard.shardId(), checkpoint?.sequenceNumber)
            } catch (_: TrimmedDataAccessException) {
                logger.warn { "the records of $id after ${checkpoint?.sequenceNumber} have expired: they are not replicated" }
                shardIterator(streamArn, shard.shardId(), null)
            }
        var count = 0
        var sequenceNumber = checkpoint?.sequenceNumber
        while (iterator != null) {
            val response =
                try {
                    DynamoStore.streamsClient.getRecords { it.shardIterator(iterator).limit(1000) }
                } catch (_: ExpiredIteratorException) {
                    return count to false
                }
            for (record in response.records()) {
                val recordSequence = record.dynamodb().sequenceNumber()
                try {
                    handler(record)
                    failures.remove(recordSequence)
                } catch (e: Exception) {
                    val attempts = failures.merge(recordSequence, 1, Int::plus)!!
                    if (attempts < maxAttempts) {
                        logger.error(e) { "the record $recordSequence of $table could not be replicated (attempt $attempts): retried at the next poll" }
                        saveCheckpoint(id, sequenceNumber, false)
                        return count to false
                    }
                    logger.error(e) { "the record $recordSequence of $table could not be replicated after $attempts attempts: skipped" }
                    failures.remove(recordSequence)
                }
                sequenceNumber = recordSequence
                count++
            }
            iterator = response.nextShardIterator()
            if (response.records().isEmpty()) {
                // an open shard has no more records for now
                if (iterator != null) break
            } else {
                saveCheckpoint(id, sequenceNumber, false)
            }
        }
        val finished = iterator == null
        if (finished || sequenceNumber != checkpoint?.sequenceNumber) {
            saveCheckpoint(id, sequenceNumber, finished)
        }
        return count to finished
    }

    private fun shardIterator(
        streamArn: String,
        shardId: String,
        sequenceNumber: String?,
    ): String? =
        DynamoStore.streamsClient
            .getShardIterator {
                it.streamArn(streamArn).shardId(shardId)
                if (sequenceNumber == null) {
                    it.shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                } else {
                    it.shardIteratorType(ShardIteratorType.AFTER_SEQUENCE_NUMBER).sequenceNumber(sequenceNumber)
                }
            }.shardIterator()

    private fun saveCheckpoint(
        id: String,
        sequenceNumber: String?,
        finished: Boolean,
    ) {
        checkpoints.save(StreamCheckpoint(id, sequenceNumber, finished))
    }
}
