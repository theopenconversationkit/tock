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

/**
 * Verifies the bearer token that Google Chat sends with its events, independently of the server.
 */
open class GoogleChatTokenVerifier(
    private val audience: String,
) {
    private val jsonFactory: JsonFactory = JacksonFactory()
    private val verifier: GoogleIdTokenVerifier =
        GoogleIdTokenVerifier
            .Builder(
                GooglePublicKeysManager
                    .Builder(ApacheHttpTransport(), jsonFactory)
                    .build(),
            ).build()

    /** The failure of the verification of the `Authorization` header, null if the token is valid. */
    open fun verify(authorization: String?): VerificationFailure? {
        val token = authorization?.takeIf { it.startsWith(BEARER_PREFIX) }?.substringAfter(BEARER_PREFIX)
        val idToken: GoogleIdToken =
            GoogleIdToken.parse(
                jsonFactory,
                token ?: return VerificationFailure.NO_BEARER_AUTHORISATION,
            )
                ?: return VerificationFailure.CANNOT_BE_PARSED

        return when {
            !verifier.verify(idToken) -> VerificationFailure.GLOBAL_VERIFICATION_FAILED
            !idToken.verifyAudience(listOf(audience)) -> VerificationFailure.AUDIENCE_VERIFICATION_FAILED
            else -> null
        }
    }
}
