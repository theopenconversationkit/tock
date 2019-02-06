/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter

import com.github.salomonbrys.kodein.Kodein
import fr.vsct.tock.bot.connector.twitter.model.AccessToken
import fr.vsct.tock.bot.connector.twitter.model.RequestToken
import fr.vsct.tock.bot.connector.twitter.model.Webhook
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.sharedModule
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import java.util.*

class TwitterConnectorTest {

    private val applicationId = "applicationId"
    private val consumerKey = "consumerKey"
    private val consumerSecret = "consumerSecret"
    private val token = "token"
    private val secret = "secret"

    private val baseUrl = "https://bot.oui.sncf"

    private val webhookId = "webhookId"
    private val webhookUrl = "$baseUrl/twitter"

    private val requestToken = RequestToken("requestToken", "requestSecret", false)
    private val accessToken = AccessToken(token, secret, "userId", "screenName")

    private val webhook = Webhook(webhookId, webhookUrl, true, Date())

    private val twitterClient = mockk<TwitterClient>()
    private val twitterConnector =
        TwitterConnector(applicationId, baseUrl, "path", twitterClient)

    @BeforeEach
    fun setUp() {
        injector.inject(Kodein {
            import(sharedModule)
        })
        every { twitterClient.requestToken() } returns requestToken
        every { twitterClient.authorizationUrl(any()) } returns "https://twitter.com/oauth/authorize?oauth_token=Z6eEdO8MOmk394WozF5oKyuAv855l4Mlqo7hhlSLik"
        every { twitterClient.accessToken(requestToken, any()) } returns accessToken

        every { twitterClient.registerWebhook(any()) } returns webhook
        every { twitterClient.subscribe() } returns true
        every { twitterClient.subscriptions() } returns true
        every { twitterClient.unregisterWebhook(webhookId) } returns true
        every { twitterClient.subscribe() } returns true
        every { twitterClient.webhooks() } returns listOf(webhook)

    }

}