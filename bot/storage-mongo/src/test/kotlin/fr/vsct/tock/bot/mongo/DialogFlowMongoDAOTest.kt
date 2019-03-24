/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityValue
import fr.vsct.tock.bot.engine.dialog.Snapshot
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType.bot
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.shared.defaultNamespace
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.deleteMany
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import kotlin.test.assertEquals

/**
 *
 */
class DialogFlowMongoDAOTest : AbstractTest() {

    val botApplicationConfiguration =
        BotApplicationConfiguration("appId", "botId", defaultNamespace, "botId", ConnectorType("test"))

    @BeforeEach
    fun cleanup() {
        DialogFlowMongoDAO.flowStateCol.deleteMany()
        DialogFlowMongoDAO.flowTransitionCol.deleteMany()
        DialogFlowMongoDAO.flowTransitionStatsCol.deleteMany()
        BotApplicationConfigurationMongoDAO.save(botApplicationConfiguration)
    }

    @AfterEach
    fun after() {
        BotApplicationConfigurationMongoDAO.delete(botApplicationConfiguration)
    }

    private fun setupData(vararg entities: String) {
        val entityList = entities.map {
            mockk<EntityValue>().apply {
                val e = mockk<Entity>()
                every { entity } returns e
                every { e.role } returns it
            }
        }
        val def = mockk<BotDefinition>()
        every { def.botId } returns "botId"
        every { def.namespace } returns defaultNamespace
        val dialog = mockk<Dialog>()
        every { dialog.id } returns "dialogId".toId()
        val snapshotCol = SnapshotCol(
            newId(), listOf(
                Snapshot(null, null, null, emptyList()),
                Snapshot("b", "b", null, entityList)
            )
        )
        val action = SendSentence(PlayerId("a"), "appId", PlayerId("appId", bot), "test")
        DialogFlowMongoDAO.addFlowStat(def, action, dialog, snapshotCol)
    }

    @Test
    fun `addFlowStat add a new state and transition and stat if not previous state and transition exists`() {
        setupData()
        assertEquals(1, DialogFlowMongoDAO.flowStateCol.countDocuments())
        assertEquals(1, DialogFlowMongoDAO.flowTransitionCol.countDocuments())
        assertEquals(1, DialogFlowMongoDAO.flowTransitionStatsCol.countDocuments())
    }

    @Test
    fun `addFlowStat add a stat only if previous state and transition exists`() {
        setupData()
        setupData()
        assertEquals(1, DialogFlowMongoDAO.flowStateCol.countDocuments())
        assertEquals(1, DialogFlowMongoDAO.flowTransitionCol.countDocuments())
        assertEquals(2, DialogFlowMongoDAO.flowTransitionStatsCol.countDocuments())
    }

    @Test
    fun `addFlowStat add a stat only if previous state and transition exists if 1 entity in state`() {
        setupData("a")
        setupData("a")
        assertEquals(1, DialogFlowMongoDAO.flowStateCol.countDocuments())
        assertEquals(1, DialogFlowMongoDAO.flowTransitionCol.countDocuments())
        assertEquals(2, DialogFlowMongoDAO.flowTransitionStatsCol.countDocuments())
    }

    @Test
    fun `addFlowStat add a stat only if previous state and transition exists if 2 entities in state`() {
        setupData("a", "b")
        setupData("a", "b")
        assertEquals(1, DialogFlowMongoDAO.flowStateCol.countDocuments())
        assertEquals(1, DialogFlowMongoDAO.flowTransitionCol.countDocuments())
        assertEquals(2, DialogFlowMongoDAO.flowTransitionStatsCol.countDocuments())
    }
}