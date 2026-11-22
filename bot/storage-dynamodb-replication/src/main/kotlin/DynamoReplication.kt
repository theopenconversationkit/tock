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

import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.storage.dynamodb.DynamoStore
import ai.tock.translator.I18nDAO
import com.mongodb.kotlin.client.MongoDatabase
import mu.KotlinLogging
import org.litote.kmongo.getCollection
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Replicates the writes of the serverless runtime to the MongoDB of the admin, from the DynamoDB streams of the
 * runtime and configuration tables: the dialogs appear in the Studio, and the configuration created by the
 * runtime (built-in stories, registered features, created labels) in the admin; with [nlp], the parse logs, the
 * inbox and the unknown counters of the NLP, from the stream of the sentence table too.
 *
 * [start] polls the streams every [interval]. The positions are stored in the MongoDB collection
 * `dynamodb_stream_checkpoint`: a restarted admin goes on where it stopped, within the 24 hours that a stream keeps.
 */
class DynamoReplication(
    database: MongoDatabase,
    applications: BotApplicationConfigurationDAO,
    stories: StoryDefinitionConfigurationDAO,
    features: FeatureDAO,
    labels: I18nDAO,
    indicators: IndicatorDAO? = null,
    private val interval: Duration = Duration.ofSeconds(5),
    nlp: NlpTargets? = null,
) {
    private val logger = KotlinLogging.logger {}

    private val replication = MongoReplication(database, applications, stories, features, labels, indicators, nlp)
    private val checkpoints = database.getCollection<StreamCheckpoint>("dynamodb_stream_checkpoint")

    private val readers =
        listOf(
            DynamoStreamReader(DynamoStore.runtimeTable, checkpoints) { replication.applyRuntime(it) },
            DynamoStreamReader(DynamoStore.configTable, checkpoints) { replication.applyConfiguration(it) },
        ) +
            listOfNotNull(nlp?.let { DynamoStreamReader(DynamoStore.sentenceTable, checkpoints) { replication.applySentence(it) } })

    private var scheduler: ScheduledExecutorService? = null

    /** Replicates the available records now, in the calling thread. Returns the number of records read. */
    @Synchronized
    fun replicate(): Int =
        readers.sumOf { reader ->
            try {
                reader.poll()
            } catch (e: Exception) {
                logger.error(e) { "the DynamoDB stream could not be read: retried in $interval" }
                0
            }
        }

    fun start() {
        check(scheduler == null) { "already started" }
        scheduler =
            Executors
                .newSingleThreadScheduledExecutor { Thread(it, "tock-dynamodb-replication").apply { isDaemon = true } }
                .also { it.scheduleWithFixedDelay({ replicate() }, 0, interval.toMillis(), TimeUnit.MILLISECONDS) }
        logger.info { "replication of the DynamoDB streams to MongoDB started" }
    }

    fun stop() {
        scheduler?.shutdownNow()
        scheduler = null
    }
}
