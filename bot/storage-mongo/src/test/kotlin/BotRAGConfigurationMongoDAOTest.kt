/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotRAGConfiguration
import ai.tock.bot.admin.bot.RAGConfiguration
import ai.tock.bot.admin.user.UserReportQuery
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.shared.defaultNamespace
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.deleteMany
import org.litote.kmongo.newId
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 *
 */
internal class BotRAGConfigurationMongoDAOTest : AbstractTest() {

    @BeforeEach
    fun cleanup() {
        BotRAGConfigurationMongoDAO.col.drop()
    }

    @Test
    fun `save rag configuration`() {
        val config = BotRAGConfiguration(
            newId(),
            "namespace1",
            "botId1",
            false,
            "engine",
            "embeddingengine",
            "1",
            "prompt",
            mapOf(
                "param1" to "data",
                "param2" to "data2"
            ),
            "storyId"
        )

        BotRAGConfigurationMongoDAO.save(config)
        val configBDD = BotRAGConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertEquals(config, configBDD)
    }

    @Test
    fun `update rag configuration`() {
        val config1 = BotRAGConfiguration(
            newId(),
            "namespace1",
            "botId1",
            false,
            "engine",
            "embeddingengine",
            "1",
            "prompt",
            mapOf(
                "param1" to "data",
                "param2" to "data2"
            ),
            "storyId1"
        )

        val config2 = BotRAGConfiguration(
            newId(),
            "namespace1",
            "botId2",
            false,
            "engine2",
            "embeddingengine",
            "0",
            "prompt2",
            mapOf(
                "param1" to "data"
            ),
            "storyId2"
        )

        assertNotEquals(config1, config2)

        BotRAGConfigurationMongoDAO.save(config1)
        BotRAGConfigurationMongoDAO.save(config2)

        BotRAGConfigurationMongoDAO.save(config1.copy(prompt = "New prompt"))

        val configBDD = BotRAGConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertEquals(config1.copy(prompt = "New prompt"), configBDD)
    }

    @Test
    fun `delete rag configuration`() {
        val config = BotRAGConfiguration(
            newId(),
            "namespace1",
            "botId1",
            false,
            "engine",
            "embeddingengine",
            "1",
            "prompt",
            mapOf(
                "param1" to "data",
                "param2" to "data2"
            ),
            "storyId"
        )

        BotRAGConfigurationMongoDAO.save(config)
        BotRAGConfigurationMongoDAO.delete(config._id)
        val configBDD = BotRAGConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertNull(configBDD)


    }

}
