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

package ai.tock.shared.http

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpRoutesTest {
    private val routes =
        HttpRoutes {
            get("/healthcheck") { HttpResponse.text("ok") }
            post("/webhook/:connector/:id") { request ->
                HttpResponse.text("${request.pathParameter("connector")} ${request.pathParameter("id")} ${request.bodyAsString}")
            }
            get("/files/*") { request -> HttpResponse.text(request.path) }
        }

    private fun request(
        method: HttpMethod,
        path: String,
        body: String = "",
    ) = HttpRequest(method, path, body = body.toByteArray())

    @Test
    fun `GIVEN a path with parameters WHEN it is handled THEN the handler gets them`() =
        runBlocking {
            val response = routes.handle(request(HttpMethod.POST, "/webhook/messenger/42", "hello"))

            assertEquals(200, response.status)
            assertEquals("messenger 42 hello", String(response.body))
        }

    @Test
    fun `GIVEN a trailing wildcard WHEN a deeper path is handled THEN it matches`() =
        runBlocking {
            assertEquals("/files/a/b.png", String(routes.handle(request(HttpMethod.GET, "/files/a/b.png")).body))
        }

    @Test
    fun `GIVEN no matching route WHEN the request is handled THEN the response is 404`() =
        runBlocking {
            assertEquals(404, routes.handle(request(HttpMethod.POST, "/webhook/messenger")).status)
            assertEquals(404, routes.handle(request(HttpMethod.GET, "/healthcheck/more")).status)
        }

    @Test
    fun `GIVEN a route WHEN the path has extra slashes THEN it still matches`() {
        assertEquals(emptyMap(), HttpRoute(HttpMethod.GET, "/healthcheck") { HttpResponse.status() }.match(HttpMethod.GET, "/healthcheck/"))
        assertNull(HttpRoute(HttpMethod.GET, "/healthcheck") { HttpResponse.status() }.match(HttpMethod.POST, "/healthcheck"))
    }

    @Test
    fun `GIVEN headers WHEN they are read THEN the name is case insensitive`() {
        val request = HttpRequest(HttpMethod.GET, "/", headers = mapOf("X-Hub-Signature" to listOf("sha1=abc")))

        assertEquals("sha1=abc", request.header("x-hub-signature"))
    }

    @Test
    fun `GIVEN a path that only matches routes of other methods WHEN it is handled THEN the response is 405 with the allowed methods`() =
        runBlocking {
            val response = routes.handle(request(HttpMethod.GET, "/webhook/messenger/42"))

            assertEquals(405, response.status)
            assertEquals("POST", response.headers["Allow"]?.single())
            assertEquals("GET, HEAD", routes.handle(request(HttpMethod.POST, "/healthcheck")).headers["Allow"]?.single())
        }

    @Test
    fun `GIVEN a GET route WHEN a HEAD request is handled THEN it gets the status and headers without body`() =
        runBlocking {
            val response = routes.handle(request(HttpMethod.HEAD, "/healthcheck"))

            assertEquals(200, response.status)
            assertEquals("text/plain; charset=utf-8", response.headers["Content-Type"]?.single())
            assertEquals(0, response.body.size)
        }

    @Test
    fun `GIVEN percent-encoded segments WHEN the path is matched THEN the path parameters are decoded, as with Vert_x`() =
        runBlocking {
            val response = routes.handle(request(HttpMethod.POST, "/webhook/zh%2DCN/op%20123+x", "hello"))

            assertEquals("zh-CN op 123+x hello", String(response.body))
            assertEquals("ok", String(routes.handle(request(HttpMethod.GET, "/health%63heck")).body))
        }
}
