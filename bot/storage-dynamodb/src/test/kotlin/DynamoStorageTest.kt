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

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.story
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.shared.bytecode.BytecodeDependencies
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A custom message of a connector, stored apart from the dialog. */
data class TestConnectorMessage(
    val payload: String,
) : ConnectorMessage {
    override val connectorType: ConnectorType = ConnectorType("test")
}

/**
 * The DynamoDB storage, on DynamoDB Local, on a classpath without MongoDB, Vert.x nor tock-shared.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoStorageTest {
    private val dynamo = GenericContainer("amazon/dynamodb-local:2.6.1").withExposedPorts(8000).waitingFor(Wait.forListeningPort())

    private val user = PlayerId("user", PlayerType.user)
    private val bot = PlayerId("bot", PlayerType.bot)
    private val greetings: StoryDefinition = story("greetings") {}
    private val botDefinition: BotDefinition = mockk(relaxed = true) { every { namespace } returns "app" }

    @BeforeAll
    fun start() {
        dynamo.start()
        System.setProperty("tock_dynamodb_endpoint", "http://${dynamo.host}:${dynamo.getMappedPort(8000)}")
        System.setProperty("aws.accessKeyId", "x")
        System.setProperty("aws.secretAccessKey", "x")
        System.setProperty("tock_dynamodb_table_prefix", "test_")
    }

    @AfterAll
    fun stop() {
        dynamo.stop()
    }

    private fun timelineWithDialog(player: PlayerId = user): UserTimeline {
        val timeline = UserTimeline(player)
        val dialog = Dialog(setOf(player, bot))
        val story = Story(greetings, greetings.mainIntent())
        story.actions += SendSentence(player, "connector", bot, "hello")
        story.actions += SendSentence(bot, "connector", player, null, mutableListOf<ConnectorMessage>(TestConnectorMessage("custom")))
        dialog.stories += story
        timeline.dialogs += dialog
        return timeline
    }

    @Test
    fun `the storage runs without MongoDB, Vert_x nor tock-shared`() {
        assertFailsWith<ClassNotFoundException> { Class.forName("com.mongodb.client.MongoClient") }
        assertFailsWith<ClassNotFoundException> { Class.forName("io.vertx.core.Vertx") }
        assertFailsWith<ClassNotFoundException> { Class.forName("ai.tock.shared.vertx.WebVerticle") }
        val violations =
            BytecodeDependencies.violations(BytecodeDependencies.locationOf(UserTimelineDynamoDAO::class.java), forbidden = {
                it.startsWith("com/mongodb/") || it.startsWith("org/bson/") || it.startsWith("io/vertx/")
            })
        assertEquals("", violations.joinToString("\n"))
    }

    @Test
    fun `GIVEN a timeline with a dialog WHEN it is saved and loaded THEN the dialog and the custom messages are back`() {
        val timeline = timelineWithDialog()

        runBlocking { UserTimelineDynamoDAO.save(timeline, botDefinition) }
        val loaded = runBlocking { UserTimelineDynamoDAO.loadWithLastValidDialog("app", user) { greetings } }

        val dialog = assertNotNull(loaded.currentDialog)
        assertEquals(timeline.currentDialog!!.id, dialog.id)
        val actions = dialog.stories.single().actions
        assertEquals("hello", (actions[0] as SendSentence).stringText)
        assertEquals(listOf<ConnectorMessage>(TestConnectorMessage("custom")), (actions[1] as SendSentence).messages)
        assertTrue(runBlocking { UserTimelineDynamoDAO.getSnapshots(dialog.id) }.isNotEmpty())
    }

    @Test
    fun `GIVEN a saved timeline WHEN it is removed THEN a new timeline is loaded`() {
        val player = PlayerId("removed", PlayerType.user)
        val timeline = timelineWithDialog(player).apply { userState.setFlag("f", 60, "v") }
        runBlocking { UserTimelineDynamoDAO.save(timeline, botDefinition) }
        assertEquals("v", runBlocking { UserTimelineDynamoDAO.loadWithoutDialogs("app", player) }.userState.getFlag("f"))

        runBlocking { UserTimelineDynamoDAO.remove("app", player) }

        val reloaded = runBlocking { UserTimelineDynamoDAO.loadWithLastValidDialog("app", player) { greetings } }
        assertEquals(null, reloaded.userState.getFlag("f"))
        assertTrue(reloaded.dialogs.isEmpty())
    }

    @Test
    fun `GIVEN a saved timeline WHEN the player id changes THEN its timeline and dialogs follow`() {
        val old = PlayerId("old", PlayerType.user)
        val new = PlayerId("new", PlayerType.user)
        val timeline = timelineWithDialog(old)
        runBlocking { UserTimelineDynamoDAO.save(timeline, botDefinition) }

        runBlocking { UserTimelineDynamoDAO.updatePlayerId("app", old, new) }

        val moved = runBlocking { UserTimelineDynamoDAO.loadWithLastValidDialog("app", new) { greetings } }
        val dialog = assertNotNull(moved.currentDialog)
        assertEquals(timeline.currentDialog!!.id, dialog.id)
        assertTrue(new in dialog.playerIds && old !in dialog.playerIds)
        assertEquals(
            new,
            dialog.stories
                .single()
                .actions
                .first()
                .playerId,
        )
    }

    @Test
    fun `GIVEN bot configurations WHEN they are saved THEN the runtime reads them`() {
        val configuration =
            BotApplicationConfiguration("whatsapp", "slice", "app", "slice", ConnectorType("whatsapp_cloud"), path = "/whatsapp")
        val botConfiguration = BotConfiguration("slice", "slice", "app", "slice")

        BotApplicationConfigurationDynamoDAO.save(configuration)
        BotApplicationConfigurationDynamoDAO.save(botConfiguration)

        assertEquals(listOf(configuration), BotApplicationConfigurationDynamoDAO.getConfigurations().filter { it.applicationId == "whatsapp" })
        assertEquals(botConfiguration, BotApplicationConfigurationDynamoDAO.getBotConfigurationsByNamespaceAndNameAndBotId("app", "slice", "slice"))
    }
}
