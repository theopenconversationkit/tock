/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.dialogflow

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.mongo.botMongoModule
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.ioc.FrontIoc
import ai.tock.nlp.front.shared.codec.SentenceDump
import ai.tock.nlp.front.shared.codec.SentencesDump
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.defaultLocale
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.sharedModule
import ai.tock.shared.trace
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.Translator
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.util.Locale

/**
 * Intent Importer from Dialogflow
 */
object DialogflowIntentImporter {
    private val projectId = property("dialogflow_project_id", "please set a google project id")

    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO by injector.instance()

    private val logger = KotlinLogging.logger {}

    init {
        FrontIoc.setup(sharedModule, botMongoModule)
    }

    /**
     * Import intents in Tock model from a Dialogflow project - It doesn't import entities
     * Create a new story for each intent with text response message
     */
    fun importIntentsFromDialogflow(
        appName: String,
        appNamespace: String,
    ) {
        val application = FrontClient.getApplicationByNamespaceAndName(appNamespace, appName)
        if (application == null) {
            logger.error { "Can't find application $appNamespace:$appName" }
        } else {
            val agent = DialogflowService.getAgent(projectId)
            if (agent == null) {
                logger.error { "Can't find Dialogflow agent $projectId" }
            } else {
                val locale = Locale.forLanguageTag(agent.defaultLanguageCode)
                val sentences = mutableListOf<SentenceDump>()

                for (dialogflowIntent in DialogflowService.getIntents(projectId)) {
                    val intentName = dialogflowIntent.displayName.replace(" ", "_").lowercase(defaultLocale)

                    // Create intent if not exist
                    var intentDefinition = FrontClient.getIntentByNamespaceAndName(application.namespace, intentName)
                    if (intentDefinition == null) {
                        intentDefinition =
                            IntentDefinition(
                                intentName,
                                application.namespace,
                                setOf(application._id),
                                setOf(),
                                label = dialogflowIntent.displayName,
                            )
                        FrontClient.save(intentDefinition)
                    }

                    for (trainingPhrase in dialogflowIntent.trainingPhrasesList) {
                        var text = ""
                        for (part in trainingPhrase.partsList) {
                            text += part.text
                        }

                        sentences.add(
                            SentenceDump(
                                text = text,
                                intent = intentDefinition.qualifiedName,
                                language = locale,
                                status = ClassifiedSentenceStatus.model,
                            ),
                        )
                    }

                    // Create a new story with simple answers
                    val simpleAnswers =
                        createSimpleAnswers(dialogflowIntent.messagesList, locale, application.namespace)
                    if (simpleAnswers.isNotEmpty()) {
                        val storyDefinitionConfiguration =
                            StoryDefinitionConfiguration(
                                intentName,
                                application.name,
                                IntentWithoutNamespace(intentName),
                                AnswerConfigurationType.simple,
                                simpleAnswers,
                                namespace = application.namespace,
                            )

                        try {
                            storyDefinitionDAO.save(storyDefinitionConfiguration)
                        } catch (e: Exception) {
                            logger.trace(e)
                        }
                    }
                }

                // Import sentences of the imported intents
                val sentenceDump = SentencesDump(application.name, locale, sentences)
                FrontClient.importSentences(application.namespace, sentenceDump).let {
                    if (it.success) {
                        logger.info {
                            "${it.sentencesImported} imported sentences"
                        }
                    } else {
                        logger.error { "Intents import failed" }
                    }
                }
            }
        }
    }

    private fun createSimpleAnswers(
        messagesList: MutableList<com.google.cloud.dialogflow.v2.Intent.Message>,
        locale: Locale,
        namespace: String,
    ): List<SimpleAnswerConfiguration> {
        val answerConfigurations = mutableListOf<SimpleAnswerConfiguration>()
        for (message in messagesList) {
            if (message.text.textList.isNotEmpty()) {
                val alternatives = message.text.textList.subList(1, message.text.textList.size)
                val labelKey =
                    I18nKeyProvider
                        .simpleKeyProvider(namespace, "answer")
                        .i18n(message.text.textList[0])
                val label = Translator.create(labelKey, locale, alternatives)

                answerConfigurations.add(
                    SimpleAnswerConfiguration(
                        listOf(
                            SimpleAnswer(
                                I18nLabelValue(label),
                                -1,
                                null,
                            ),
                        ),
                    ),
                )
            }
        }
        return answerConfigurations
    }
}
