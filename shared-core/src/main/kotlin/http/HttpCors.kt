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

/**
 * The CORS rules of routes, as the CorsHandler of Vert.x applies them:
 * - a preflight request (OPTIONS with `Access-Control-Request-Method`) is answered 204 with the allowed methods
 *   and headers;
 * - a request whose `Origin` does not match [originPattern] is refused (403);
 * - the response to an allowed origin gets `Access-Control-Allow-Origin` (the origin) and, with
 *   [allowCredentials], `Access-Control-Allow-Credentials`.
 */
class HttpCors(
    originPattern: String,
    private val allowedMethods: Set<HttpMethod>,
    private val allowedHeaders: Set<String>,
    private val allowCredentials: Boolean,
) {
    private val origin = Regex(originPattern)

    /** Applies the rules: the answer of a preflight or of a refused origin, or the response of [handler]. */
    suspend fun handle(
        request: HttpRequest,
        handler: suspend (HttpRequest) -> HttpResponse,
    ): HttpResponse {
        val requestOrigin = request.header("Origin") ?: return handler(request)
        if (!origin.matches(requestOrigin)) {
            return HttpResponse.text("CORS Rejected - Invalid origin", 403)
        }
        if (request.method == HttpMethod.OPTIONS && request.header("Access-Control-Request-Method") != null) {
            return allowOrigin(
                HttpResponse(204)
                    .withHeader("Access-Control-Allow-Methods", allowedMethods.joinToString(",") { it.name })
                    .let { if (allowedHeaders.isEmpty()) it else it.withHeader("Access-Control-Allow-Headers", allowedHeaders.joinToString(",")) },
                requestOrigin,
            )
        }
        return allowOrigin(handler(request), requestOrigin)
    }

    private fun allowOrigin(
        response: HttpResponse,
        requestOrigin: String,
    ): HttpResponse =
        response
            .withHeader("Access-Control-Allow-Origin", requestOrigin)
            .let { if (allowCredentials) it.withHeader("Access-Control-Allow-Credentials", "true") else it }
            .withHeader("Vary", "Origin")
}
