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
package fr.vsct.tock.bot.connector.rocketchat

import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.SendSentence
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test


/**
 *
 */
class RocketChatIntegrationTest {

    private val connectorController: ConnectorController = mockk()
    private val connector = RocketChatConnector(
        "appId",
        RocketChatClient(
            "http://localhost:3000",
            "test",
            "a",
            "https://avatars2.githubusercontent.com/u/224255?s=88&v=4"
        ),
        "GENERAL"
    )


    @Test
    fun testRocketChatIntegration() {
        every { connectorController.handle(any(), any()) } answers {
            val s = args.first() as SendSentence
            val data = args[1] as ConnectorData
            connector.send(
                SendSentence(
                    s.recipientId,
                    s.applicationId,
                    s.playerId,
                    "sentence received: ${s.text}"
                ),
                data.callback
            )
        }
        connector.register(connectorController)
        Thread.sleep(100000000L)

    }
}