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

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.Kodein
import fr.vsct.tock.bot.connector.messenger.model.webhook.SubscriptionsResponse
import fr.vsct.tock.bot.connector.messenger.model.webhook.SuccessResponse
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resourceAsStream
import fr.vsct.tock.shared.sharedModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MessengerConnectorTest {

    val appId = "appId"
    val pageId = "pageId"
    val appToken = "appToken"
    val token = "token"
    val verifyToken = "verifyToken"
    val messengerClient = mockk<MessengerClient>()
    val messengerConnector =
        MessengerConnector(appId, "path", pageId, appToken, token, verifyToken, messengerClient)

    val expectedFields = "messages,messaging_postbacks,messaging_optins,messaging_account_linking"
    val expectedCallbackUrl = "https://bot.oui.sncf/messenger"

    @BeforeEach
    internal fun setUp() {
        injector.inject(Kodein {
            import(sharedModule)
        })
        every { messengerClient.deleteSubscribedApps(any(), any(), any()) } returns SuccessResponse(true)
        every { messengerClient.subscribedApps(any(), any(), any()) } returns SuccessResponse(true)
    }

    @Test
    internal fun `GIVEN already active webhook WHEN check subscription THEN do nothing`() {
        val response: SubscriptionsResponse =
            mapper.readValue(resourceAsStream("/get_subscribed_apps_filled_data_and_active.json"))
        every { messengerClient.getSubscriptions(any(), any()) } returns response

        messengerConnector.checkWebhookSubscription()

        verify(exactly = 0) { messengerClient.subscriptions(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { messengerClient.deleteSubscribedApps(any(), any(), any()) }
        verify(exactly = 0) { messengerClient.subscribedApps(any(), any(), any()) }
    }

    @Test
    internal fun `GIVEN not subscribed app webhook WHEN subscription success THEN call webhook and page subscriptions`() {
        val response: SubscriptionsResponse =
            mapper.readValue(resourceAsStream("/get_subscribed_apps_filled_data_and_disabled.json"))
        every { messengerClient.getSubscriptions(any(), any()) } returns response
        every { messengerClient.subscriptions(any(), any(), any(), any(), any()) } returns SuccessResponse(true)

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
    internal fun `GIVEN not subscribed app webhook WHEN subscription fails THEN not call page subscription`() {
        val response: SubscriptionsResponse =
            mapper.readValue(resourceAsStream("/get_subscribed_apps_filled_data_and_disabled.json"))
        every { messengerClient.getSubscriptions(any(), any()) } returns response
        every { messengerClient.subscriptions(any(), any(), any(), any(), any()) } returns SuccessResponse(false)

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
    internal fun `GIVEN empty data of subscribed app webhook WHEN subscription success THEN call webhook and page subscriptions`() {
        val response: SubscriptionsResponse = mapper.readValue(resourceAsStream("/get_subscribed_apps_empty_data.json"))
        every { messengerClient.getSubscriptions(any(), any()) } returns response
        every { messengerClient.subscriptions(any(), any(), any(), any(), any()) } returns SuccessResponse(true)

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