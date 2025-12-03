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

import io.vertx.core.http.HttpServerRequest

fun createRequestFilter(
    allowedIps: Set<String>?,
    xAuthToken: String?,
): RequestFilter =
    RequestFiltersComposer(
        listOfNotNull(
            allowedIps
                ?.mapNotNull { ip -> ip.trim().takeUnless { it.isEmpty() } }
                ?.toSet()
                ?.takeUnless { it.isEmpty() }
                ?.let { IPRequestFilter(it) },
            xAuthToken
                ?.takeUnless { it.isBlank() }
                ?.let { XAuthTokenRequestFilter(it.trim()) },
        ),
    )

/**
 * A request filter is used to filter an incoming request.
 */
interface RequestFilter {
    /**
     * Returns true is the request is accepted, false either.
     */
    fun accept(request: HttpServerRequest): Boolean
}

internal class RequestFiltersComposer(private val filters: List<RequestFilter>) : RequestFilter {
    override fun accept(request: HttpServerRequest): Boolean {
        return filters.all { it.accept(request) }
    }
}

internal class IPRequestFilter(private val allowedIps: Set<String>) : RequestFilter {
    override fun accept(request: HttpServerRequest): Boolean {
        val directIp = request.remoteAddress().host()
        val forwardedIp = request.getHeader("X-Forwarded-For")?.split(",")?.lastOrNull()?.trim()
        return allowedIps.contains(directIp) || allowedIps.contains(forwardedIp)
    }
}

internal class XAuthTokenRequestFilter(private val token: String) : RequestFilter {
    override fun accept(request: HttpServerRequest): Boolean {
        return token == request.getHeader("X-Auth-Token")
    }
}
