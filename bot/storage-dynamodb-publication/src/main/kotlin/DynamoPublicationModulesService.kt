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

import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.service.storage.NamespaceConfigurationDAO
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
 * `tock_dynamodb_publication_sentences_complete_interval_in_min` (60) is the interval of the complete
 * publications of the validated sentences: between them, only the modified sentences are read.
 *
 * The NLP configuration is published when the NLP DAOs of the admin are injected.
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
                                    nlp = nlpSources(),
                                    interval = Duration.ofSeconds(longProperty("tock_dynamodb_publication_interval_in_s", 300)),
                                    sentenceCompleteInterval = Duration.ofMinutes(longProperty("tock_dynamodb_publication_sentences_complete_interval_in_min", 60)),
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

    private fun Kodein.nlpSources(): NlpSources? {
        val applications = instanceOrNull<ApplicationDefinitionDAO>() ?: return null
        val intents = instanceOrNull<IntentDefinitionDAO>() ?: return null
        val entityTypes = instanceOrNull<EntityTypeDefinitionDAO>() ?: return null
        val sentences = instanceOrNull<ClassifiedSentenceDAO>() ?: return null
        return NlpSources(applications, intents, entityTypes, sentences, instanceOrNull<NamespaceConfigurationDAO>())
    }

    companion object {
        /** The publication started in this process, if any. */
        @Volatile
        var publication: DynamoPublication? = null
            private set
    }
}
