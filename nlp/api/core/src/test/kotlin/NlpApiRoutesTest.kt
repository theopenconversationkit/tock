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

package ai.tock.nlp.api

import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.service.UnknownApplicationException
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.ApplicationMonitor
import ai.tock.nlp.front.shared.Parser
import ai.tock.nlp.front.shared.codec.CreateApplicationQuery
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.monitoring.MarkAsUnknownQuery
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.nlp.front.shared.parser.QueryContext
import ai.tock.shared.bytecode.BytecodeDependencies
import ai.tock.shared.http.HttpMethod
import ai.tock.shared.http.HttpRequest
import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.RequestFilter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class NlpApiRoutesTest {
    private val parser: Parser = mockk()
    private val configuration: ApplicationConfiguration = mockk()
    private val monitor: ApplicationMonitor = mockk()
    private val routes = NlpApiRoutes.routes("/rest/nlp", parser, configuration, monitor, NlpApiSecurity.Unprotected)

    // the caller authenticated by its Authorization header is in the namespace of the same name
    private val protectedRoutes =
        NlpApiRoutes.routes(
            "/rest/nlp",
            parser,
            configuration,
            monitor,
            NlpApiSecurity.Namespace { request -> request.header("Authorization") },
        )

    private fun post(
        path: String,
        body: Any,
    ) = runBlocking { routes.handle(HttpRequest(HttpMethod.POST, "/rest/nlp$path", body = mapper.writeValueAsBytes(body))) }

    private fun protectedPost(
        path: String,
        body: Any,
        namespace: String?,
    ) = runBlocking {
        protectedRoutes.handle(
            HttpRequest(
                HttpMethod.POST,
                "/rest/nlp$path",
                headers = namespace?.let { mapOf("Authorization" to listOf(it)) } ?: emptyMap(),
                body = mapper.writeValueAsBytes(body),
            ),
        )
    }

    private fun get(
        path: String,
        parameters: Map<String, String>,
    ) = runBlocking {
        routes.handle(HttpRequest(HttpMethod.GET, "/rest/nlp$path", queryParameters = parameters.mapValues { listOf(it.value) }))
    }

    private fun query(queries: List<String> = listOf("hello")) = ParseQuery(queries, "ns", "app", QueryContext(Locale.FRENCH))

    @Test
    fun `GIVEN a query WHEN it is parsed THEN the result is answered as JSON`() {
        val result: ParseResult = mockk(relaxed = true)
        every { parser.parse(any()) } returns result
        every { result.intent } returns "greetings"

        val response = post("/parse", query())

        assertEquals(200, response.status)
        assertEquals("application/json; charset=utf-8", response.headers["Content-Type"]?.single())
        verify { parser.parse(match { it.queries == listOf("hello") && it.namespace == "ns" }) }
    }

    @Test
    fun `GIVEN no query or an unknown application WHEN it is parsed THEN the answer is a 400 with the message`() {
        assertEquals(400, post("/parse", query(emptyList())).status)

        every { parser.parse(any()) } throws UnknownApplicationException("ns", "app")
        val response = post("/parse", query())

        assertEquals(400, response.status)
        assertEquals("""{"message":"unknown application ns:app"}""", String(response.body))
    }

    @Test
    fun `GIVEN a handler that returns Unit WHEN it answers THEN the body is the JSON of Unit, as WebVerticle_endJson`() {
        every { parser.incrementUnknown(any()) } returns Unit

        val response = post("/unknown", MarkAsUnknownQuery("ns", "app", Locale.FRENCH, "hello"))

        assertEquals(200, response.status)
        assertEquals(mapper.writeValueAsString(Unit), String(response.body))
    }

    @Test
    fun `GIVEN missing parameters or an unknown application WHEN the application is read THEN the answers are 400 and 204`() {
        assertEquals(400, get("/application", mapOf("namespace" to "ns")).status)

        every { configuration.getApplicationByNamespaceAndName("ns", "app") } returns null
        assertEquals(204, get("/application", mapOf("namespace" to "ns", "name" to "app")).status)
        assertEquals("[]", String(get("/intents", mapOf("namespace" to "ns", "name" to "app")).body))
    }

    @Test
    fun `GIVEN protected routes WHEN the caller is not authenticated or in another namespace THEN the answer is a 401`() {
        every { parser.parse(any()) } returns mockk(relaxed = true)

        assertEquals(401, protectedPost("/parse", query(), null).status)
        assertEquals(401, protectedPost("/parse", query(), "other").status)
        assertEquals(200, protectedPost("/parse", query(), "ns").status)

        assertEquals(401, protectedPost("/application/create", CreateApplicationQuery("app", null, "ns", Locale.FRENCH), "other").status)
        val intents =
            runBlocking {
                protectedRoutes.handle(
                    HttpRequest(
                        HttpMethod.GET,
                        "/rest/nlp/intents",
                        headers = mapOf("Authorization" to listOf("other")),
                        queryParameters = mapOf("namespace" to listOf("ns"), "name" to listOf("app")),
                    ),
                )
            }
        assertEquals(401, intents.status)
        verify(exactly = 0) { configuration.save(any<ApplicationDefinition>()) }
    }

    @Test
    fun `the NLP API core and the front modules it embeds do not reference Vert_x, MongoDB nor tock-shared`() {
        // RequestFilter, unlike WebVerticle, loads without Vert.x, which is not on this classpath
        val sharedOnly = BytecodeDependencies.classesOf(BytecodeDependencies.locationOf(RequestFilter::class.java))
        val forbidden = { to: String -> to.startsWith("io/vertx/") || to.startsWith("com/mongodb/") || to in sharedOnly }

        val violations =
            listOf(NlpApiRoutes::class.java, Parser::class.java, FrontClient::class.java)
                .flatMap { BytecodeDependencies.violations(BytecodeDependencies.locationOf(it), forbidden) }

        assertEquals("", violations.joinToString("\n"))
    }
}
