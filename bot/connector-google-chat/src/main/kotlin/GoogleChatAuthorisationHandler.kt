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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import org.apache.http.HttpStatus

// Bearer Tokens received by bots will always specify this issuer.
private const val CHAT_ISSUER = "chat@system.gserviceaccount.com"

// Url to obtain the public certificate for the issuer.
private const val PUBLIC_CERT_URL_PREFIX = "https://www.googleapis.com/service_accounts/v1/metadata/x509/"

private const val BEARER_PREFIX = "Bearer "

enum class VerificationFailure {
    NO_BEARER_AUTHORISATION,
    CANNOT_BE_PARSED,
    GLOBAL_VERIFICATION_FAILED,
    AUDIENCE_VERIFICATION_FAILED,
    ISSUER_VERIFICATION_FAILED
}

class GoogleChatAuthorisationHandler(private val botProjectNumber: String) : Handler<RoutingContext> {
    private val logger = KotlinLogging.logger {}

    private val jsonFactory: JsonFactory
    private val verifier: GoogleIdTokenVerifier

    init {
        jsonFactory = JacksonFactory()
        verifier = GoogleIdTokenVerifier.Builder(
            GooglePublicKeysManager.Builder(ApacheHttpTransport(), jsonFactory)
                .setPublicCertsEncodedUrl(PUBLIC_CERT_URL_PREFIX + CHAT_ISSUER)
                .build()
        ).setIssuer(CHAT_ISSUER)
            .build()
    }

    override fun handle(routingContext: RoutingContext) {
        val token = routingContext.request().getHeader("Authorization")?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.substringAfter(BEARER_PREFIX)
        if (hasFailure(token)?.also { logger.error { "Token ($token) verification failed. Cause : $it" } } != null)
            routingContext.response().setStatusCode(HttpStatus.SC_FORBIDDEN).end()
        else
            routingContext.next()
    }

    private fun hasFailure(token: String?): VerificationFailure? {
        val idToken: GoogleIdToken = GoogleIdToken.parse(
            jsonFactory,
            token ?: return VerificationFailure.NO_BEARER_AUTHORISATION
        )
            ?: return VerificationFailure.CANNOT_BE_PARSED

        return when {
            !verifier.verify(idToken) -> VerificationFailure.GLOBAL_VERIFICATION_FAILED
            !idToken.verifyAudience(listOf(botProjectNumber)) -> VerificationFailure.AUDIENCE_VERIFICATION_FAILED
            !idToken.verifyIssuer(CHAT_ISSUER) -> VerificationFailure.ISSUER_VERIFICATION_FAILED
            else -> null
        }
    }
}
