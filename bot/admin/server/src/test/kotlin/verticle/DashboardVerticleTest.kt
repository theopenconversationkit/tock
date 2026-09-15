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

package ai.tock.bot.admin.verticle

import ai.tock.bot.admin.AbstractTest
import ai.tock.bot.admin.dashboard.BotDashboardDAO
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.auth.TockAuthProvider
import ai.tock.shared.tockInternalInjector
import ai.tock.shared.vertx.WebVerticle
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.impl.UserContextInternal
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DashboardVerticleTest {
    private class TestWebVerticle : WebVerticle() {
        override fun configure() {}

        override fun authProvider(): TockAuthProvider =
            mockk {
                every { toTockUser(any()) } answers { firstArg<RoutingContext>().user() as TockUser }
            }
    }

    @Test
    fun `HTTP routes enforce roles namespace and serialize dashboard contracts`() {
        val originalInjector = tockInternalInjector
        val dao = mockk<BotDashboardDAO>(relaxed = true)
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                import(AbstractTest.defaultModulesBinding())
                bind<BotDashboardDAO>() with singleton { dao }
            },
        )
        mockkObject(FrontClient)
        every { FrontClient.getApplicationByNamespaceAndName("ns", "bot") } returns ApplicationDefinition("bot", namespace = "ns")
        every { FrontClient.getApplicationByNamespaceAndName("ns", "foreign") } returns ApplicationDefinition("foreign", namespace = "other")
        every { dao.metadata(any(), any()) } returns null
        every { dao.note(any(), any(), any()) } returns null
        every { dao.history(any(), any(), any(), any()) } returns emptyList()
        val vertx = Vertx.vertx()
        val web = TestWebVerticle()
        web.router.route().handler(BodyHandler.create()).handler { context ->
            (context.userContext() as UserContextInternal).setUser(TockUser("alice", "ns", setOf(context.request().getHeader("Test-Role") ?: "admin")))
            context.next()
        }
        DashboardVerticle().configure(web)
        val server =
            vertx
                .createHttpServer()
                .requestHandler(web.router)
                .listen(0, "127.0.0.1")
                .toCompletionStage()
                .toCompletableFuture()
                .get(10, TimeUnit.SECONDS)
        try {
            val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

            fun request(
                path: String,
                body: String? = null,
                role: String = "admin",
            ): HttpResponse<String> {
                val builder = HttpRequest.newBuilder(URI("http://127.0.0.1:${server.actualPort()}$path")).header("Test-Role", role)
                if (body != null) builder.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body))
                return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
            }
            val identity = request("/bots/bot/identity", """{"displayName":"Lea","notes":"test"}""")
            assertEquals(200, identity.statusCode())
            assertEquals("alice", JsonObject(identity.body()).getString("updatedBy"))
            assertNotNull(JsonObject(identity.body()).getString("updatedAt"))
            assertEquals(200, request("/bots/bot/identity", role = "botUser").statusCode())
            assertEquals(401, request("/bots/bot/identity", """{"displayName":"forbidden"}""", "botUser").statusCode())
            assertEquals(401, request("/bots/foreign/identity").statusCode())
            assertEquals(200, request("/bots/bot/contacts", """[{"role":"owner","name":"Alice"}]""").statusCode())
            verify { dao.saveContacts("ns", "bot", match { it.single().id != null }) }
            assertEquals(200, request("/bots/bot/index-sessions/purged/note", """{"text":"retained"}""").statusCode())
            verify { dao.saveNote(match { it.namespace == "ns" && it.botId == "bot" && it.updatedBy == "alice" }) }
            val history = request("/bots/bot/history")
            assertEquals(200, history.statusCode())
            assertEquals(false, JsonObject(history.body()).getBoolean("hasMore"))
            assertEquals(400, request("/bots/bot/history?before=invalid").statusCode())
            assertEquals(400, request("/bots/bot/history?limit=0").statusCode())
        } finally {
            server
                .close()
                .toCompletionStage()
                .toCompletableFuture()
                .get(10, TimeUnit.SECONDS)
            vertx
                .close()
                .toCompletionStage()
                .toCompletableFuture()
                .get(10, TimeUnit.SECONDS)
            unmockkObject(FrontClient)
            tockInternalInjector = originalInjector
        }
    }
}
