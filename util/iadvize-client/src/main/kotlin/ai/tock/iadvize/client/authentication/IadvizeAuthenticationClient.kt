/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.iadvize.client.authentication

import ai.tock.iadvize.client.*
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.SecretMangerService
import ai.tock.shared.security.SecretManagerProviderType
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference


val iAdvizeCredentialsProvider: String = ai.tock.shared.property(
    "tock_iadvize_credentials_provider",
    SecretManagerProviderType.GCP_SECRET_MANAGER.name // TODO MASS: ENV
)

// tock_database_credentials_provider TODO MASS : pour la bdd

val iAdvizeCredentialsSecretName: String = ai.tock.shared.property(
    "tock_iadvize_credentials_secret_name",
    "LOCAL-TOCK-iadvize-credentials"
)

/**
 * Authentication client.
 */
class IadvizeAuthenticationClient {

    companion object {
        val logger = KotlinLogging.logger { }
        val token = AtomicReference<Token?>()
        const val DELAY_SECONDS = 5
    }

    private val secretMangerService: SecretMangerService by lazy { injector.provide(tag = iAdvizeCredentialsProvider) }

    internal var iadvizeApi: IadvizeApi = createApi(logger)

    private val credentials by lazy {
        secretMangerService.getCredentials(iAdvizeCredentialsSecretName)
    }

    /**
     * Get the stored access token.
     * if the access token is expired, a new one is requested and stored.
     */
    fun getAccessToken() : String {

        var t = token.get()

        if (t == null || (t.expireAt?.isBefore(LocalDateTime.now()) == true)) {
            t = getToken()
        }

        return t.value
    }

    /**
     * Request a new access token.
     */
    private fun getToken(): Token {
        return iadvizeApi.createToken(credentials.username, credentials.password, grantType = PASSWORD).execute().body()
            ?.let {
                val value = it.accessToken ?: authenticationFailedError()
                val time = it.expiresIn?.let { s -> LocalDateTime.now().plusSeconds(s.toLong() - DELAY_SECONDS) }

                Token(value, time).also { t -> token.set(t) }
            }
            ?: authenticationFailedError()
    }

    /**
     * Stored Token representation.
     */
    data class Token(val value: String, val expireAt: LocalDateTime?)
}
