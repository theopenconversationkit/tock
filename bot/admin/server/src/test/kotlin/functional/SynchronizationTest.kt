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
@file:Suppress("ktlint:standard:property-naming", "ktlint:standard:max-line-length")

package ai.tock.bot.admin.functional

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.model.BotSynchronization
import ai.tock.bot.admin.model.BotSynchronizationConfig
import ai.tock.bot.connector.ConnectorType
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.admin.model.ParseQuery
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.front.shared.codec.DumpType
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.user.UserNamespace
import ai.tock.nlp.model.ModelNotInitializedException
import ai.tock.shared.jackson.mapper
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.litote.kmongo.Id
import java.time.Instant
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import ai.tock.nlp.front.client.FrontClient as front

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled()
class SynchronizationTest : AbstractIntegrationTest() {
    private val sourceApplicationId: Id<ApplicationDefinition> = sourceAppDef._id
    private val targetApplicationId: Id<ApplicationDefinition> = targetAppDef._id

    @Test
    @Disabled("JVM Crash")
    fun `run base scenario`(context: VertxTestContext) {
        assertEquals(1, front.getSentences(language = Locale.FRENCH).size)
        webClient.post("/rest/admin/configuration/synchronization").putHeader("Cookie", authCookie).sendJsonObject(
            JsonObject.mapFrom(
                BotSynchronization(
                    source = BotSynchronizationConfig(sourceNamespaceName, sourceAppName, sourceApplicationId.toString()),
                    target = BotSynchronizationConfig(targetNamespaceName, targetAppName, targetApplicationId.toString()),
                ),
            ),
        )
            .onComplete { result ->
                assertEquals(200, result.result().statusCode())
                val sentences = front.getSentences(language = Locale.FRENCH)
                assertEquals(2, sentences.size)
                val firstSentence = sentences[0]
                val secondSentence = sentences[1]
                assertEquals(firstSentence.text, secondSentence.text)
                assertNotEquals(firstSentence.applicationId.toString(), secondSentence.applicationId.toString())
                context.completeNow()
            }.onFailure {
                context.failNow(it)
            }
    }

    @Test
    @Disabled("JVM Crash")
    fun `synchronize process clear all stories on a target bot`(context: VertxTestContext) {
        webClient.post("/rest/admin/namespace/select/$targetNamespaceName").putHeader("Cookie", authCookie)
            .send().onComplete {
                webClient.post("/rest/admin/bot/story/new").putHeader("Cookie", authCookie)
                    .sendBuffer(Buffer.buffer(targetStory))
                    .onComplete {
                        assertEquals(1, BotAdminService.exportStories(targetNamespaceName, targetAppName).size)
                        webClient.post("/rest/admin/configuration/synchronization").putHeader("Cookie", authCookie)
                            .sendJsonObject(
                                JsonObject.mapFrom(
                                    BotSynchronization(
                                        source =
                                            BotSynchronizationConfig(
                                                sourceNamespaceName,
                                                sourceAppName,
                                                sourceApplicationId.toString(),
                                            ),
                                        target =
                                            BotSynchronizationConfig(
                                                targetNamespaceName,
                                                targetAppName,
                                                targetApplicationId.toString(),
                                            ),
                                    ),
                                ),
                            )
                            .onComplete { result ->
                                assertEquals(200, result.result().statusCode())
                                assertEquals(0, BotAdminService.exportStories(targetNamespaceName, targetAppName).size)
                                context.completeNow()
                            }.onFailure {
                                context.failNow(it)
                            }
                    }.onFailure {
                        context.failNow(it)
                    }
            }
    }

    @Test
    @Disabled("JVM Crash")
    fun `synchronize process will merge user sentences from the both bots`(context: VertxTestContext) {
        try {
            AdminService.parseSentence(mapper.readValue(targetUserSentence, ParseQuery::class.java))
        } catch (ignored: ModelNotInitializedException) {
        } // If intent model not found you still have an opportunity to save sentence
        assertEquals(1, front.exportSentences(targetApplicationId, DumpType.full).sentences.size)
        assertEquals(1, front.exportSentences(sourceApplicationId, DumpType.full).sentences.size)
        webClient.post("/rest/admin/configuration/synchronization").putHeader("Cookie", authCookie).sendJsonObject(
            JsonObject.mapFrom(
                BotSynchronization(
                    source = BotSynchronizationConfig(sourceNamespaceName, sourceAppName, sourceApplicationId.toString()),
                    target = BotSynchronizationConfig(targetNamespaceName, targetAppName, targetApplicationId.toString()),
                ),
            ),
        )
            .onComplete { result ->
                assertEquals(200, result.result().statusCode())
                val sentences = front.exportSentences(targetApplicationId, DumpType.full).sentences
                assertEquals(2, sentences.size)
                assertTrue { sentences.map { it.text }.contains("Salut") }
                assertTrue { sentences.map { it.text }.contains("bonjour") }
                context.completeNow()
            }.onFailure {
                context.failNow(it)
            }
    }

    @Test
    @Disabled("JVM Crash")
    fun `copy process will not remove user sentences in the target application if they were removed in the source`(context: VertxTestContext) {
        front.save(
            ClassifiedSentence(
                text = "bonne journée",
                language = Locale.FRENCH,
                applicationId = targetAppDef._id,
                creationDate = Instant.now(),
                updateDate = Instant.now(),
                status = ClassifiedSentenceStatus.validated,
                classification = Classification(targetIntentDef._id, listOf()),
                lastEntityProbability = 0.0,
                lastIntentProbability = 1.0,
            ),
        )
        assertEquals(1, front.exportSentences(sourceApplicationId, DumpType.full).sentences.size)
        assertEquals(1, front.exportSentences(targetApplicationId, DumpType.full).sentences.size)
        // copy from "prod" bot to "pre-prod" bot
        webClient.post("/rest/admin/configuration/synchronization").putHeader("Cookie", authCookie).sendJsonObject(
            JsonObject.mapFrom(
                BotSynchronization(
                    target = BotSynchronizationConfig(sourceNamespaceName, sourceAppName, sourceApplicationId.toString()),
                    source = BotSynchronizationConfig(targetNamespaceName, targetAppName, targetApplicationId.toString()),
                ),
            ),
        )
            .onComplete { result ->
                assertEquals(200, result.result().statusCode())
                var sentenceDump = front.exportSentences(sourceApplicationId, DumpType.full)
                val sentences = sentenceDump.sentences
                Assertions.assertEquals(2, sentences.size)
                Assertions.assertTrue(sentences.map { it.text }.contains("bonjour"))
                assertTrue { sentences.map { it.text }.contains("bonne journée") }
                // delete sentence
                val deletingSentence = sentences.first { it.text == "bonne journée" }.copy(status = ClassifiedSentenceStatus.deleted)
                val sentenceList = listOf(deletingSentence, sentences.first { it.text != "bonne journée" })
                sentenceDump = sentenceDump.copy(sentences = sentenceList)
                front.importSentences(sourceNamespaceName, sentenceDump)
                front.deleteSentencesByStatus(ClassifiedSentenceStatus.deleted)

                // copy data back to "prod"
                webClient.post("/rest/admin/configuration/synchronization").putHeader("Cookie", authCookie).sendJsonObject(
                    JsonObject.mapFrom(
                        BotSynchronization(
                            source = BotSynchronizationConfig(sourceNamespaceName, sourceAppName, sourceApplicationId.toString()),
                            target = BotSynchronizationConfig(targetNamespaceName, targetAppName, targetApplicationId.toString()),
                        ),
                    ),
                )
                    .onComplete { result2 ->
                        assertEquals(200, result2.result().statusCode())

                        val dump = front.exportSentences(targetApplicationId, DumpType.full)
                        Assertions.assertTrue(dump.sentences.size == 2)
                        val sentence = dump.sentences.first { it.text == "bonne journée" }
                        Assertions.assertTrue(sentence.status == ClassifiedSentenceStatus.validated)

                        context.completeNow()
                    }
            }.onFailure {
                context.failNow(it)
            }
    }

    @BeforeEach
    fun prepareContext() {
        front.saveNamespace(UserNamespace(userLogin, sourceNamespaceName))
        var sourceAppDef = front.save(sourceAppDef)
        front.save(sourceEntityDef)
        val entity = front.getEntityTypeByName(sourceEntityDef.name)
        front.save(
            sourceIntentDef.copy(
                applications = setOf(sourceAppDef._id),
                entities =
                    setOf(
                        EntityDefinition(
                            entity!!,
                            "greet",
                        ),
                    ),
            ),
        )
        val intentDef = front.getIntentByNamespaceAndName(sourceNamespaceName, sourceIntentDef.name)
        sourceAppDef =
            sourceAppDef.copy(
                intents =
                    when (intentDef) {
                        null -> emptySet()
                        else -> setOf(intentDef._id)
                    },
            )
        front.save(sourceAppDef)
        front.save(
            ClassifiedSentence(
                text = "bonjour",
                language = Locale.FRENCH,
                applicationId = sourceAppDef._id,
                creationDate = Instant.now(),
                updateDate = Instant.now(),
                status = ClassifiedSentenceStatus.validated,
                classification = Classification(intentDef!!._id, listOf()),
                lastEntityProbability = 0.0,
                lastIntentProbability = 1.0,
            ),
        )
        BotAdminService.saveApplicationConfiguration(sourceBotConfiguration)
        front.saveNamespace(UserNamespace(userLogin, targetNamespaceName))
        var targetAppDef = front.save(targetAppDef)
        front.save(
            targetIntentDef.copy(
                applications = setOf(targetAppDef._id),
            ),
        )
        val targetIntentDef = front.getIntentByNamespaceAndName(targetNamespaceName, targetIntentDef.name)
        targetAppDef = front.getApplicationById(targetApplicationId)!!
        front.save(
            targetAppDef.copy(
                intents =
                    when (targetIntentDef) {
                        null -> emptySet()
                        else -> setOf(targetIntentDef._id)
                    },
            ),
        )
        BotAdminService.saveApplicationConfiguration(targetBotConfiguration)
    }

    @AfterEach
    fun clearContext() {
        val srcApp = front.getApplicationByNamespaceAndName(sourceNamespaceName, sourceAppName)
        front.deleteApplicationById(srcApp!!._id)
        front.deleteEntityTypeByName("$sourceNamespaceName:first-name")
        val targetApp = front.getApplicationByNamespaceAndName(targetNamespaceName, targetAppName)
        front.deleteApplicationById(targetApp!!._id)
        front.deleteNamespace(userLogin, sourceNamespaceName)
        front.deleteNamespace(userLogin, targetNamespaceName)
    }

    companion object {
        private const val sourceNamespaceName = "account-000-qa"
        private const val sourceAppName = "simpleApp"
        private const val targetNamespaceName = "account-000-stage"
        private const val targetAppName = "exampleApp"
        private val sourceAppDef =
            ApplicationDefinition(
                _id = newId(),
                name = sourceAppName,
                namespace = sourceNamespaceName,
                supportedLocales = setOf(Locale.FRENCH),
                nlpEngineType = NlpEngineType.opennlp,
            )
        private val sourceEntityDef =
            EntityTypeDefinition(
                name = "$sourceNamespaceName:first-name2",
                description = "Find a first name from the user response",
            )
        private val sourceIntentDef =
            IntentDefinition(
                name = "Greetings",
                namespace = sourceNamespaceName,
                setOf(),
                setOf(),
            )
        private val targetIntentDef = sourceIntentDef.copy(namespace = targetNamespaceName, _id = newId())
        private val sourceBotConfiguration =
            BotApplicationConfiguration(
                applicationId = sourceAppName,
                botId = sourceAppName,
                namespace = sourceNamespaceName,
                connectorType = ConnectorType.rest,
                nlpModel = sourceAppName,
                path = "/io/app/$sourceAppName/web",
            )

        private val targetAppDef =
            ApplicationDefinition(
                _id = newId(),
                name = targetAppName,
                namespace = targetNamespaceName,
                supportedLocales = setOf(Locale.FRENCH),
                nlpEngineType = NlpEngineType.opennlp,
            )
        private val targetBotConfiguration =
            BotApplicationConfiguration(
                applicationId = targetAppName,
                botId = targetAppName,
                namespace = targetNamespaceName,
                connectorType = ConnectorType.rest,
                nlpModel = targetAppName,
                path = "/io/app/$targetAppName/web",
            )

        private const val targetStory = """{"story":{"currentType":0,"answers":[{"answerType":0,"allowNoAnswer":false,"answers":[{"label":{"_id":"${targetNamespaceName}_build_Salut","namespace":"$targetNamespaceName","category":"build","i18n":[{"locale":"fr","interfaceType":0,"label":"Salut","validated":true,"alternatives":[],"stats":[]}],"defaultLabel":"Bonjour","defaultLocale":"fr","version":0,"unhandledLocaleStats":[],"lastUpdate":null},"delay":-1}]}],"category":"build","storyId":"Greetings","botId":"$targetAppName","intent":{"name":"salut"},"namespace":"$targetNamespaceName","name":"Salut","userSentence":"Salut","userSentenceLocale":"fr","features":[],"version":0,"mandatoryEntities":[],"steps":[],"description":"","tags":[],"configuredAnswers":[],"configuredSteps":[],"metricStory":false,"hideDetails":false,"selected":true},"language":"fr","firstSentences":["Salut"]}""""
        private const val targetUserSentence = """{"namespace":"$targetNamespaceName","applicationName":"$targetAppName","language":"fr","query":"Salut","checkExistingQuery":true,"state":null}"""
        private const val targetUserSentence2 = """{"namespace":"$targetNamespaceName","applicationName":"$targetAppName","language":"fr","query":"Bonne journée","checkExistingQuery":true,"state":null}"""
    }
}
