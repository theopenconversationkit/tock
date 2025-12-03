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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.messenger.model.Recipient
import ai.tock.bot.connector.messenger.model.Sender
import ai.tock.bot.connector.messenger.model.webhook.CallbackRequest
import ai.tock.bot.connector.messenger.model.webhook.Entry
import ai.tock.bot.connector.messenger.model.webhook.Message
import ai.tock.bot.connector.messenger.model.webhook.MessageWebhook
import ai.tock.bot.connector.messenger.model.webhook.PostbackWebhook
import ai.tock.bot.connector.messenger.model.webhook.UserActionPayload
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.monitoring.RequestTimerData
import ai.tock.shared.Executor
import ai.tock.shared.SimpleExecutor
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.Kodein.Module
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MultiMessengerConnectorsTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun injectExecutor() {
            tockInternalInjector =
                KodeinInjector().apply {
                    inject(
                        Kodein {
                            import(
                                Module {
                                    bind<Executor>() with singleton { SimpleExecutor(2) }
                                },
                            )
                        },
                    )
                }
        }

        @AfterAll
        @JvmStatic
        fun resetInjection() {
            tockInternalInjector = KodeinInjector()
        }
    }

    // APP A (pathA) Page A, B et C
    // APP B (pathB) Page A et B

    // first app linked to three pages
    val connectorId1 = "connectorId1"
    val path1 = "pathA"
    val appId1 = "appA"
    val pageId1 = "pageA"
    val appToken1 = "appToken"
    val token1 = "token"
    val verifyToken1 = "verifyToken"

    val connectorId2 = "connectorId2"
    val path2 = "pathA"
    val appId2 = "appA"
    val pageId2 = "pageB"
    val appToken2 = "appToken"
    val token2 = "token"
    val verifyToken2 = "verifyToken"

    val connectorId3 = "connectorId3"
    val path3 = "pathA"
    val appId3 = "appA"
    val pageId3 = "pageC"
    val appToken3 = "appToken"
    val token3 = "token"
    val verifyToken3 = "verifyToken"

    // second app linked to two pages
    val connectorId4 = "connectorId4"
    val path4 = "pathB"
    val appId4 = "appB"
    val pageId4 = "pageA"
    val appToken4 = "appToken"
    val token4 = "token"
    val verifyToken4 = "verifyToken"

    val connectorId5 = "connectorId5"
    val path5 = "pathB"
    val appId5 = "appB"
    val pageId5 = "pageB"
    val appToken5 = "appToken"
    val token5 = "token"
    val verifyToken5 = "verifyToken"

    val messengerClient = mockk<MessengerClient>()

    val messengerConnector1 =
        MessengerConnector(connectorId1, appId1, path1, pageId1, appToken1, token1, verifyToken1, messengerClient)
    val controller1 =
        mockk<ConnectorController>(relaxed = true).apply {
            every { connector } returns messengerConnector1
        }
    val messengerConnector2 =
        MessengerConnector(connectorId2, appId2, path2, pageId2, appToken2, token2, verifyToken2, messengerClient)
    val controller2 =
        mockk<ConnectorController>(relaxed = true).apply {
            every { connector } returns messengerConnector2
        }
    val messengerConnector3 =
        MessengerConnector(connectorId3, appId3, path3, pageId3, appToken3, token3, verifyToken3, messengerClient)
    val controller3: ConnectorController =
        mockk<ConnectorController>(relaxed = true).apply {
            every { connector } returns messengerConnector3
        }
    val messengerConnector4 =
        MessengerConnector(connectorId4, appId4, path4, pageId4, appToken4, token4, verifyToken4, messengerClient)
    val controller4: ConnectorController =
        mockk<ConnectorController>(relaxed = true).apply {
            every { connector } returns messengerConnector4
        }
    val messengerConnector5 =
        MessengerConnector(connectorId5, appId5, path5, pageId5, appToken5, token5, verifyToken5, messengerClient)
    val controller5 =
        mockk<ConnectorController>(relaxed = true).apply {
            every { connector } returns messengerConnector5
        }

    @BeforeEach
    fun before() {
        messengerConnector1.register(controller1)
        messengerConnector2.register(controller2)
        messengerConnector3.register(controller3)
        messengerConnector4.register(controller4)
        messengerConnector5.register(controller5)
    }

    fun request(page: String) =
        CallbackRequest(
            "page",
            listOf(
                Entry(
                    page,
                    0,
                    messaging =
                        listOf(
                            MessageWebhook(
                                Sender("1"),
                                Recipient(page),
                                1L,
                                Message("aa", "text"),
                            ),
                        ),
                ),
            ),
        )

    fun choice(
        page: String,
        appId: String,
    ) = CallbackRequest(
        "page",
        listOf(
            Entry(
                page,
                0,
                messaging =
                    listOf(
                        PostbackWebhook(
                            Sender("1"),
                            Recipient(page),
                            1L,
                            UserActionPayload(SendChoice.encodeChoiceId(Intent.unknown, sourceAppId = appId)),
                        ),
                    ),
            ),
        ),
    )

    fun standby(page: String) =
        request(page).run {
            copy(
                entry = entry.map { it.copy(messaging = null, standby = it.messaging) },
            )
        }

    @Test
    fun `GIVEN 5 messenger connectors WHEN app A is called for the page A THEN the right controller is called`() {
        val handler =
            MessengerConnectorHandler(
                "appA",
                controller1,
                request("pageA"),
                RequestTimerData("messenger"),
            )

        handler.handleRequest()

        verify { controller1.handle(match { it.applicationId == connectorId1 }, any()) }
    }

    @Test
    fun `GIVEN 5 messenger connectors WHEN app A is called for the page B THEN the right controller is called`() {
        val handler =
            MessengerConnectorHandler(
                "appA",
                controller1,
                request("pageB"),
                RequestTimerData("messenger"),
            )

        handler.handleRequest()

        verify { controller2.handle(match { it.applicationId == connectorId2 }, any()) }
    }

    @Test
    fun `GIVEN 5 messenger connectors WHEN app A is called for the page C THEN the right controller is called`() {
        val handler =
            MessengerConnectorHandler(
                "appA",
                controller1,
                request("pageC"),
                RequestTimerData("messenger"),
            )

        handler.handleRequest()

        verify { controller3.handle(match { it.applicationId == connectorId3 }, any()) }
    }

    @Test
    fun `GIVEN 5 messenger connectors WHEN app B is called for the page A THEN the right controller is called`() {
        val handler =
            MessengerConnectorHandler(
                "appB",
                controller4,
                request("pageA"),
                RequestTimerData("messenger"),
            )

        handler.handleRequest()

        verify { controller4.handle(match { it.applicationId == connectorId4 }, any()) }
    }

    @Test
    fun `GIVEN 5 messenger connectors WHEN app B is called for the page B THEN the right controller is called`() {
        val handler =
            MessengerConnectorHandler(
                "appB",
                controller4,
                request("pageB"),
                RequestTimerData("messenger"),
            )

        handler.handleRequest()

        verify { controller5.handle(match { it.applicationId == connectorId5 }, any()) }
    }

    @Test
    fun `GIVEN 5 messenger connectors WHEN app A is notified of the page B call by app B THEN notifyOnly is true AND sourceId is ok`() {
        every { messengerClient.getThreadOwnerId(token2, "1") } returns appId5
        val handler =
            MessengerConnectorHandler(
                "appA",
                controller1,
                standby("pageB"),
                RequestTimerData("messenger"),
            )

        handler.handleRequest()

        verify { controller2.handle(match { it.applicationId == connectorId2 && it.state.notification && it.state.sourceApplicationId == connectorId5 }, any()) }
    }

    @Test
    fun `GIVEN 5 messenger connectors WHEN app A is notified of the page B call by app B with thread owner is an unknown app THEN notifyOnly is true AND sourceId is the unknown app id`() {
        every { messengerClient.getThreadOwnerId(token2, "1") } returns "other_messenger_app_id"
        val handler =
            MessengerConnectorHandler(
                "appA",
                controller1,
                standby("pageB"),
                RequestTimerData("messenger"),
            )

        handler.handleRequest()

        verify { controller2.handle(match { it.applicationId == connectorId2 && it.state.notification && it.state.sourceApplicationId == "other_messenger_app_id" }, any()) }
    }

    @Test
    fun `GIVEN 5 messenger connectors WHEN a send choice of a different app is retrieved THEN notifyOnly is true AND sourceId is the choice source app id`() {
        val handler =
            MessengerConnectorHandler(
                "appA",
                controller1,
                choice("pageB", connectorId5),
                RequestTimerData("messenger"),
            )

        handler.handleRequest()

        verify { controller2.handle(match { it.applicationId == connectorId2 && it.state.notification && it.state.sourceApplicationId == connectorId5 }, any()) }
    }
}
