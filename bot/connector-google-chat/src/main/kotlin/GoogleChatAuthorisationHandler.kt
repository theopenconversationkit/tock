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

package ai.tock.bot.connector.googlechat

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import org.apache.http.HttpStatus

/** Also keeps the file facade class of the former versions. */
internal const val BEARER_PREFIX = "Bearer "

enum class VerificationFailure {
    NO_BEARER_AUTHORISATION,
    CANNOT_BE_PARSED,
    GLOBAL_VERIFICATION_FAILED,
    AUDIENCE_VERIFICATION_FAILED,
}

/**
 * The Vert.x handler of the verification: the connector uses [GoogleChatTokenVerifier], which needs no server.
 */
class GoogleChatAuthorisationHandler(
    audience: String,
) : Handler<RoutingContext> {
    private val logger = KotlinLogging.logger {}

    /** The verification of the tokens. */
    val verifier: GoogleChatTokenVerifier = GoogleChatTokenVerifier(audience)

    override fun handle(routingContext: RoutingContext) {
        val authorization = routingContext.request().getHeader("Authorization")
        if (verifier.verify(authorization)?.also { logger.error { "Token ($authorization) verification failed. Cause : $it" } } != null) {
            routingContext.response().setStatusCode(HttpStatus.SC_FORBIDDEN).end()
        } else {
            routingContext.next()
        }
    }
}
