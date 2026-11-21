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

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.story
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.bot.mongo.DialogCol
import ai.tock.bot.mongo.MONGO_DATABASE
import ai.tock.bot.mongo.botMongoModule
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import ai.tock.storage.dynamodb.FeatureDynamoDAO
import ai.tock.storage.dynamodb.I18nDynamoDAO
import ai.tock.storage.dynamodb.StoryDefinitionConfigurationDynamoDAO
import ai.tock.storage.dynamodb.UserTimelineDynamoDAO
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelStat
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType.textChat
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.mongodb.kotlin.client.MongoDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A custom message of a connector, stored apart from the dialog. */
data class ReplicatedConnectorMessage(
    val payload: String,
) : ConnectorMessage {
    override val connectorType: ConnectorType = ConnectorType("test")
}

/**
 * The replication of the writes of the serverless runtime (DynamoDB Local) to the MongoDB of the admin.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoReplicationTest {
    private val dynamo = GenericContainer("amazon/dynamodb-local:2.6.1").withExposedPorts(8000).waitingFor(Wait.forListeningPort())

    private val bot = PlayerId("bot", PlayerType.bot)
    private val greetings: StoryDefinition = story("greetings") {}
    private val botDefinition: BotDefinition = mockk(relaxed = true) { every { namespace } returns "app" }

    private lateinit var replication: DynamoReplication

    private val database: MongoDatabase get() = injector.provide(MONGO_DATABASE)
    private val timelines: UserTimelineDAO get() = injector.provide()
    private val labels: I18nDAO get() = injector.provide()
    private val stories: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val features: FeatureDAO get() = injector.provide()

    @BeforeAll
    fun start() {
        dynamo.start()
        System.setProperty("tock_dynamodb_endpoint", "http://${dynamo.host}:${dynamo.getMappedPort(8000)}")
        System.setProperty("aws.accessKeyId", "x")
        System.setProperty("aws.secretAccessKey", "x")
        System.setProperty("tock_bot_encrypted_flags", "test1,test2")
        System.setProperty("tock_encrypt_pass", "dev")
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                import(sharedTestModule)
                import(botMongoModule)
            },
        )
        replication = newReplication()
    }

    private fun newReplication() = DynamoReplication(database, injector.provide<BotApplicationConfigurationDAO>(), stories, features, labels, Duration.ofHours(1))

    private fun newUser() = PlayerId("user_${UUID.randomUUID()}", PlayerType.user)

    private fun timelineWithDialog(player: PlayerId): UserTimeline {
        val timeline = UserTimeline(player)
        val dialog = Dialog(setOf(player, bot))
        val story = Story(greetings, greetings.mainIntent())
        story.actions += SendSentence(player, "connector", bot, "hello")
        story.actions += SendSentence(bot, "connector", player, null, mutableListOf<ConnectorMessage>(ReplicatedConnectorMessage("custom")))
        dialog.stories += story
        timeline.dialogs += dialog
        return timeline
    }

    @Test
    fun `GIVEN a dialog of the runtime WHEN it is replicated THEN the Studio reads it from MongoDB, with its custom messages, snapshots and text`() {
        val user = newUser()
        val timeline = timelineWithDialog(user)
        runBlocking { UserTimelineDynamoDAO.save(timeline, botDefinition) }

        assertTrue(replication.replicate() > 0)

        val dialogId = timeline.currentDialog!!.id
        val loaded = runBlocking { timelines.loadWithLastValidDialog("app", user) { greetings } }
        val dialog = assertNotNull(loaded.currentDialog)
        assertEquals(dialogId, dialog.id)
        val actions = dialog.stories.single().actions
        assertEquals("hello", (actions[0] as SendSentence).stringText)
        assertEquals(listOf<ConnectorMessage>(ReplicatedConnectorMessage("custom")), (actions[1] as SendSentence).messages)
        assertEquals(1, runBlocking { timelines.getSnapshots(dialogId) }.size)
        assertNotNull(database.getCollection<DialogTextDocument>("dialog_text").findOne(DialogTextDocument::dialogId eq dialogId))
        // the document is the one of the runtime (MongoDB stores the dates in milliseconds)
        val runtimeDocument = assertNotNull(UserTimelineDynamoDAO.loadDialogDocument(dialogId.toString())).first
        assertEquals(
            runtimeDocument.copy(lastUpdateDate = runtimeDocument.lastUpdateDate.truncatedTo(ChronoUnit.MILLIS)),
            database.getCollection<DialogCol>("dialog").findOneById(dialogId),
        )
    }

    @Test
    fun `GIVEN replicated records WHEN they are given again THEN the snapshots are not duplicated`() {
        val user = newUser()
        val timeline = timelineWithDialog(user)
        runBlocking { UserTimelineDynamoDAO.save(timeline, botDefinition) }
        replication.replicate()

        // the checkpoints are lost: the records are given again
        database.getCollection<StreamCheckpoint>("dynamodb_stream_checkpoint").drop()
        replication = newReplication()
        assertTrue(replication.replicate() > 0)

        assertEquals(1, runBlocking { timelines.getSnapshots(timeline.currentDialog!!.id) }.size)
    }

    @Test
    fun `GIVEN a replication WHEN it restarts THEN it goes on from its checkpoints`() {
        replication.replicate()
        val restarted = newReplication()
        assertEquals(0, restarted.replicate())

        val user = newUser()
        runBlocking { UserTimelineDynamoDAO.save(timelineWithDialog(user), botDefinition) }
        assertTrue(restarted.replicate() > 0)
        assertNotNull(runBlocking { timelines.loadWithLastValidDialog("app", user) { greetings } }.currentDialog)
        replication = restarted
    }

    @Test
    fun `GIVEN a replicated user WHEN the runtime removes it THEN its dialogs are removed from MongoDB`() {
        val user = newUser()
        val timeline = timelineWithDialog(user)
        runBlocking { UserTimelineDynamoDAO.save(timeline, botDefinition) }
        replication.replicate()

        runBlocking { UserTimelineDynamoDAO.remove("app", user) }
        replication.replicate()

        assertNull(database.getCollection<DialogCol>("dialog").findOneById(timeline.currentDialog!!.id))
        assertTrue(runBlocking { timelines.loadWithLastValidDialog("app", user) { greetings } }.dialogs.isEmpty())
    }

    @Test
    fun `GIVEN the configuration created by the runtime WHEN it is replicated THEN the admin has it, without losing its own`() {
        val suffix = UUID.randomUUID().toString().take(8)
        val namespace = "ns_$suffix"
        val created = I18nLabel("${namespace}_created".toId(), namespace, "test", linkedSetOf(I18nLocalizedLabel(Locale.ENGLISH, textChat, "created", false)))
        val edited = I18nLabel("${namespace}_edited".toId(), namespace, "test", linkedSetOf(I18nLocalizedLabel(Locale.ENGLISH, textChat, "edited in the admin", true)))
        labels.save(edited)
        I18nDynamoDAO.save(created)
        I18nDynamoDAO.save(edited.copy(i18n = linkedSetOf(I18nLocalizedLabel(Locale.ENGLISH, textChat, "default of the runtime", false))))
        val builtin =
            StoryDefinitionConfiguration(
                storyId = "builtin_$suffix",
                botId = "bot_$suffix",
                intent = IntentWithoutNamespace("builtin"),
                currentType = AnswerConfigurationType.builtin,
                answers = emptyList(),
                namespace = namespace,
            )
        StoryDefinitionConfigurationDynamoDAO.createBuiltInStoriesIfNotExist(listOf(builtin))
        runBlocking { FeatureDynamoDAO.isEnabled("bot_$suffix", namespace, "category", "registered", default = true) }
        val labelId = newId<I18nLabel>()
        repeat(2) { I18nDynamoDAO.incrementLabelStat(I18nLabelStat(labelId, namespace, Locale.ENGLISH, textChat, null, count = 3)) }

        replication.replicate()

        assertEquals(
            "created",
            labels
                .getLabelById(created._id)
                ?.i18n
                ?.single()
                ?.label,
        )
        assertEquals(
            "edited in the admin",
            labels
                .getLabelById(edited._id)
                ?.i18n
                ?.single()
                ?.label,
        )
        assertEquals(builtin, stories.getStoryDefinitionById(builtin._id))
        assertEquals(listOf("registered" to true), runBlocking { features.getFeatures("bot_$suffix", namespace) }.map { it.name to it.enabled })
        assertEquals(6, labels.getLabelStats(namespace).single().count)
    }

    @Test
    fun `GIVEN the replication enabled WHEN the modules of the admin are injected THEN the replication starts`() {
        System.setProperty("tock_dynamodb_replication_enabled", "true")
        try {
            val service =
                java.util.ServiceLoader
                    .load(ai.tock.shared.service.BotAdditionalModulesService::class.java)
                    .filterIsInstance<DynamoReplicationModulesService>()
                    .single()
            Kodein {
                import(sharedTestModule)
                import(botMongoModule)
                service.customModules().forEach { import(it) }
            }
            assertNotNull(DynamoReplicationModulesService.replication).stop()
        } finally {
            System.clearProperty("tock_dynamodb_replication_enabled")
        }
        assertEquals(emptySet(), DynamoReplicationModulesService().customModules())
    }
}
