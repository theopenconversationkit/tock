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

package ai.tock.shared.security

import ai.tock.shared.http.HttpRequest

/**
 * Filters the requests received by a connector, independently of the server: the counterpart of [RequestFilter],
 * which reads a Vert.x request.
 */
fun interface HttpRequestFilter {
    /**
     * Returns true if the request is accepted.
     */
    fun accept(request: HttpRequest): Boolean

    companion object {
        /** Accepts all the requests. */
        val acceptAll: HttpRequestFilter = HttpRequestFilter { true }
    }
}

/**
 * Creates a filter that accepts the requests from [allowedIps] (when set) carrying [xAuthToken] (when set):
 * the same rules as [createRequestFilter].
 */
fun createHttpRequestFilter(
    allowedIps: Set<String>?,
    xAuthToken: String?,
): HttpRequestFilter {
    val filters =
        listOfNotNull(
            allowedIps
                ?.mapNotNull { ip -> ip.trim().takeUnless { it.isEmpty() } }
                ?.toSet()
                ?.takeUnless { it.isEmpty() }
                ?.let { ips ->
                    HttpRequestFilter { request ->
                        val forwardedIp =
                            request
                                .header("X-Forwarded-For")
                                ?.split(",")
                                ?.lastOrNull()
                                ?.trim()
                        ips.contains(request.remoteAddress) || ips.contains(forwardedIp)
                    }
                },
            xAuthToken
                ?.takeUnless { it.isBlank() }
                ?.trim()
                ?.let { token -> HttpRequestFilter { request -> token == request.header("X-Auth-Token") } },
        )
    return HttpRequestFilter { request -> filters.all { it.accept(request) } }
}
