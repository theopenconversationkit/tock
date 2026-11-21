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

import ai.tock.shared.booleanProperty
import ai.tock.shared.longProperty
import ai.tock.shared.service.BotAdditionalModulesService
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.instanceOrNull
import mu.KotlinLogging
import java.time.Duration

/**
 * Starts the [DynamoPublication] in the admin, once its modules are injected, when
 * `tock_dynamodb_publication_enabled` is true: add this module to the classpath of the admin, with the
 * properties of the DynamoDB storage (`tock_dynamodb_region`, `tock_dynamodb_table_prefix`...).
 *
 * `tock_dynamodb_publication_interval_in_s` (300) is the interval of the complete publications.
 */
class DynamoPublicationModulesService : BotAdditionalModulesService {
    private val logger = KotlinLogging.logger {}

    override fun customModules(): Set<Kodein.Module> =
        if (booleanProperty("tock_dynamodb_publication_enabled", false)) {
            setOf(
                Kodein.Module {
                    onReady {
                        try {
                            publication =
                                DynamoPublication(
                                    applicationConfigurations = instance(),
                                    stories = instance(),
                                    features = instance(),
                                    labels = instance(),
                                    settings =
                                        BotSettingsSources(
                                            rag = instanceOrNull(),
                                            vectorStore = instanceOrNull(),
                                            observability = instanceOrNull(),
                                            compressor = instanceOrNull(),
                                            businessRules = instanceOrNull(),
                                        ),
                                    indicators = instanceOrNull(),
                                    interval = Duration.ofSeconds(longProperty("tock_dynamodb_publication_interval_in_s", 300)),
                                ).also { it.start() }
                        } catch (e: Exception) {
                            logger.error(e) { "the publication to DynamoDB could not start" }
                        }
                    }
                },
            )
        } else {
            emptySet()
        }

    companion object {
        /** The publication started in this process, if any. */
        @Volatile
        var publication: DynamoPublication? = null
            private set
    }
}
