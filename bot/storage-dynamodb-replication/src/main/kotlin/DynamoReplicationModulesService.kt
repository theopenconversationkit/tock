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

import ai.tock.bot.mongo.MONGO_DATABASE
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.ParseRequestLogDAO
import ai.tock.shared.booleanProperty
import ai.tock.shared.longProperty
import ai.tock.shared.service.BotAdditionalModulesService
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.instanceOrNull
import mu.KotlinLogging
import java.time.Duration

/**
 * Starts the [DynamoReplication] in the admin, once its modules are injected, when
 * `tock_dynamodb_replication_enabled` is true: add this module to the classpath of the admin, with the
 * properties of the DynamoDB storage (`tock_dynamodb_region`, `tock_dynamodb_table_prefix`...).
 *
 * `tock_dynamodb_replication_interval_in_ms` (5000) is the interval between two reads of the streams: each read is
 * a few requests by table, the first 2.5 million of the month are free.
 */
class DynamoReplicationModulesService : BotAdditionalModulesService {
    private val logger = KotlinLogging.logger {}

    override fun customModules(): Set<Kodein.Module> =
        if (booleanProperty("tock_dynamodb_replication_enabled", false)) {
            setOf(
                Kodein.Module {
                    onReady {
                        try {
                            replication =
                                DynamoReplication(
                                    database = instance(MONGO_DATABASE),
                                    applications = instance(),
                                    stories = instance(),
                                    features = instance(),
                                    labels = instance(),
                                    indicators = instanceOrNull(),
                                    nlp =
                                        instanceOrNull<ClassifiedSentenceDAO>()?.let { sentences ->
                                            instanceOrNull<ParseRequestLogDAO>()?.let { NlpTargets(sentences, it) }
                                        },
                                    interval = Duration.ofMillis(longProperty("tock_dynamodb_replication_interval_in_ms", 5000)),
                                ).also { it.start() }
                        } catch (e: Exception) {
                            logger.error(e) { "the replication from DynamoDB could not start" }
                        }
                    }
                },
            )
        } else {
            emptySet()
        }

    companion object {
        /** The replication started in this process, if any. */
        @Volatile
        var replication: DynamoReplication? = null
            private set
    }
}
