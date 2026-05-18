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

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BotBusinessRulesConfigurationMongoDAOTest : AbstractTest() {
    @BeforeEach
    fun cleanup() {
        BotBusinessRulesConfigurationMongoDAO.col.drop()
    }

    @Test
    fun `save business rules configuration`() {
        val config =
            BotBusinessRulesConfiguration(
                newId(),
                "namespace1",
                "botId1",
                businessLexicon = "Term: definition",
                coveredTopics = listOf("payments", "subscriptions"),
                excludedTopics = listOf("legal advice"),
            )

        BotBusinessRulesConfigurationMongoDAO.save(config)
        val configBDD = BotBusinessRulesConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertEquals(config, configBDD)
    }

    @Test
    fun `update business rules configuration`() {
        val config =
            BotBusinessRulesConfiguration(
                newId(),
                "namespace1",
                "botId1",
                businessLexicon = "Term: definition",
                coveredTopics = listOf("payments"),
                excludedTopics = listOf("legal advice"),
            )

        val updatedConfig =
            config.copy(
                businessLexicon = "Updated term: updated definition",
                coveredTopics = listOf("payments", "subscriptions"),
            )

        BotBusinessRulesConfigurationMongoDAO.save(config)
        BotBusinessRulesConfigurationMongoDAO.save(updatedConfig)

        val configBDD = BotBusinessRulesConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertEquals(updatedConfig, configBDD)
    }

    @Test
    fun `delete business rules configuration`() {
        val config =
            BotBusinessRulesConfiguration(
                newId(),
                "namespace1",
                "botId1",
                businessLexicon = "Term: definition",
                coveredTopics = listOf("payments"),
                excludedTopics = listOf("legal advice"),
            )

        BotBusinessRulesConfigurationMongoDAO.save(config)
        BotBusinessRulesConfigurationMongoDAO.delete(config._id)
        val configBDD = BotBusinessRulesConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertNull(configBDD)
    }
}
