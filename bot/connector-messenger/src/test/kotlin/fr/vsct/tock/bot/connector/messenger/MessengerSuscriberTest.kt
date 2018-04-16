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

package fr.vsct.tock.bot.connector.messenger

import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.engine.ConnectorConfigurationRepository
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

/**
 *
 */
class MessengerSuscriberTest {

    @Test
    fun `subscribe with property file is ok`() {
        val botDefinition: BotDefinition = mockk(relaxed = true)

        botDefinition.addMessengerConnector()

        val conf: ConnectorConfiguration =
            ConnectorConfigurationRepository::class
                .declaredMemberProperties
                .first { it.name == "configurations" }
                .run {
                    isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    call() as List<ConnectorConfiguration>
                }
                .first()

        assertEquals("id", conf.connectorId)
        assertEquals("pToken", conf.parameters["token"])
        assertEquals("secret", conf.parameters["secret"])
        assertEquals("vToken", conf.parameters["verifyToken"])
    }
}