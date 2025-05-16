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

import ai.tock.bot.connector.messenger.model.subscription.SubscriptionsResponse
import ai.tock.bot.connector.messenger.model.subscription.SuccessResponse
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.Executor
import ai.tock.shared.SimpleExecutor
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.resourceAsStream
import ai.tock.shared.sharedModule
import ai.tock.shared.tockInternalInjector
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.Kodein
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

class MessengerConnectorSubscribedAppTest {

    companion object {

        @BeforeAll
        @JvmStatic
        fun injectExecutor() {
            tockInternalInjector = KodeinInjector().apply {
                inject(
                    Kodein {
                        import(
                            Kodein.Module {
                                bind<Executor>() with singleton { SimpleExecutor(2) }
                            }
                        )
                    }
                )
            }
        }

        @AfterAll
        @JvmStatic
        fun resetInjection() {
            tockInternalInjector = KodeinInjector()
        }
    }

    private val connectorId = "connectorId"
    private val appId = "appId"
    private val pageId = "pageId"
    private val appToken = "appToken"
    private val token = "token"
    private val verifyToken = "verifyToken"
    private val messengerClient = mockk<MessengerClient>()
    private val messengerConnector =
        MessengerConnector(connectorId, appId, "path", pageId, appToken, token, verifyToken, messengerClient, true)

    private val expectedFields = "messages,messaging_postbacks,messaging_optins,messaging_account_linking"
    private val expectedCallbackUrl = "https://bot.oui.sncf/messenger"

    @BeforeEach
    fun setUp() {
        injector.inject(
            Kodein {
                import(sharedModule)
                bind<UserTimelineDAO>() with singleton { mockk<UserTimelineDAO>() }
            }
        )
        every { messengerClient.deleteSubscribedApps(any(), any(), any()) } returns SuccessResponse(
            true
        )
        every { messengerClient.subscribedApps(any(), any(), any()) } returns SuccessResponse(
            true
        )
    }

    @Test
    fun `GIVEN already active webhook WHEN check subscription THEN do nothing`() {
        val response: SubscriptionsResponse =
            mapper.readValue(resourceAsStream("/get_subscribed_apps_filled_data_and_active.json"))
        every { messengerClient.getSubscriptions(any(), any()) } returns response

        messengerConnector.checkWebhookSubscription()

        verify(exactly = 0) { messengerClient.subscriptions(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { messengerClient.deleteSubscribedApps(any(), any(), any()) }
        verify(exactly = 0) { messengerClient.subscribedApps(any(), any(), any()) }
    }

    @Test
    fun `GIVEN not subscribed app webhook WHEN subscription success THEN call webhook and page subscriptions`() {
        val response: SubscriptionsResponse =
            mapper.readValue(resourceAsStream("/get_subscribed_apps_filled_data_and_disabled.json"))
        every { messengerClient.getSubscriptions(any(), any()) } returns response
        every { messengerClient.subscriptions(any(), any(), any(), any(), any()) } returns SuccessResponse(
            true
        )

        messengerConnector.checkWebhookSubscription()

        verify(exactly = 1) {
            messengerClient.subscriptions(
                eq(appId),
                eq(expectedCallbackUrl),
                eq(expectedFields),
                eq(verifyToken),
                eq(appToken)
            )
        }
        verify(exactly = 1) { messengerClient.deleteSubscribedApps(eq(pageId), eq(expectedFields), eq(token)) }
        verify(exactly = 1) { messengerClient.subscribedApps(eq(pageId), eq(expectedFields), eq(token)) }
    }

    @Test
    fun `GIVEN not subscribed app webhook WHEN subscription fails THEN not call page subscription`() {
        val response: SubscriptionsResponse =
            mapper.readValue(resourceAsStream("/get_subscribed_apps_filled_data_and_disabled.json"))
        every { messengerClient.getSubscriptions(any(), any()) } returns response
        every { messengerClient.subscriptions(any(), any(), any(), any(), any()) } returns SuccessResponse(
            false
        )

        messengerConnector.checkWebhookSubscription()

        verify(exactly = 1) {
            messengerClient.subscriptions(
                eq(appId),
                eq(expectedCallbackUrl),
                eq(expectedFields),
                eq(verifyToken),
                eq(appToken)
            )
        }
        verify(exactly = 0) { messengerClient.deleteSubscribedApps(any(), any(), any()) }
        verify(exactly = 0) { messengerClient.subscribedApps(any(), any(), any()) }
    }

    @Test
    fun `GIVEN empty data of subscribed app webhook WHEN subscription success THEN call webhook and page subscriptions`() {
        val response: SubscriptionsResponse = mapper.readValue(resourceAsStream("/get_subscribed_apps_empty_data.json"))
        every { messengerClient.getSubscriptions(any(), any()) } returns response
        every { messengerClient.subscriptions(any(), any(), any(), any(), any()) } returns SuccessResponse(
            true
        )

        messengerConnector.checkWebhookSubscription()

        verify(exactly = 1) {
            messengerClient.subscriptions(
                eq(appId),
                eq(""),
                eq(expectedFields),
                eq(verifyToken),
                eq(appToken)
            )
        }
        verify(exactly = 1) { messengerClient.deleteSubscribedApps(eq(pageId), eq(expectedFields), eq(token)) }
        verify(exactly = 1) { messengerClient.subscribedApps(eq(pageId), eq(expectedFields), eq(token)) }
    }
}
