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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.ConnectorConfigurationRepository.addConfiguration
import org.junit.Test

/**
 *
 */
class BotRepositoryTest : BotEngineTest() {

    @Test(expected = IllegalStateException::class)
    fun `installBots with two configurations of the same id throws error`() {
        addConfiguration(
            ConnectorConfiguration(
                "id",
                "",
                ConnectorType.none,
                ConnectorType.none
            )
        )
        addConfiguration(
            ConnectorConfiguration(
                "id",
                "",
                ConnectorType.none,
                ConnectorType.none
            )
        )
        BotRepository.installBots(emptyList())
    }

    @Test
    fun `installBots with two configurations of different id should be ok`() {
        addConfiguration(
            ConnectorConfiguration(
                "id1",
                "",
                ConnectorType.none,
                ConnectorType.none
            )
        )
        addConfiguration(
            ConnectorConfiguration(
                "id2",
                "",
                ConnectorType.none,
                ConnectorType.none
            )
        )
        BotRepository.installBots(emptyList())
        //no exception
    }
}