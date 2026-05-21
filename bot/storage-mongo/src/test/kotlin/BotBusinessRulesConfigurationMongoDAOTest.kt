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
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesLexiconGroup
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
                coveredTopics = listOf("payments", "subscriptions"),
                excludedTopics = listOf("legal advice"),
                lexiconGroups =
                    listOf(
                        BotBusinessRulesLexiconGroup(
                            id = 1,
                            terms = listOf("Checking account", "Current account"),
                        ),
                    ),
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
                coveredTopics = listOf("payments"),
                excludedTopics = listOf("legal advice"),
                lexiconGroups =
                    listOf(
                        BotBusinessRulesLexiconGroup(
                            id = 1,
                            terms = listOf("Checking account", "Current account"),
                        ),
                    ),
            )

        val updatedConfig =
            config.copy(
                coveredTopics = listOf("payments", "subscriptions"),
                lexiconGroups =
                    listOf(
                        BotBusinessRulesLexiconGroup(
                            id = 1,
                            terms = listOf("Checking account", "Current account", "Deposit account"),
                        ),
                        BotBusinessRulesLexiconGroup(
                            id = 2,
                            terms = listOf("LDDS", "Sustainable development savings account"),
                        ),
                    ),
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
                coveredTopics = listOf("payments"),
                excludedTopics = listOf("legal advice"),
                lexiconGroups =
                    listOf(
                        BotBusinessRulesLexiconGroup(
                            id = 1,
                            terms = listOf("Checking account", "Current account"),
                        ),
                    ),
            )

        BotBusinessRulesConfigurationMongoDAO.save(config)
        BotBusinessRulesConfigurationMongoDAO.delete(config._id)
        val configBDD = BotBusinessRulesConfigurationMongoDAO.findByNamespaceAndBotId("namespace1", "botId1")

        assertNull(configBDD)
    }
}
