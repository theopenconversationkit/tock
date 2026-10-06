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

package ai.tock.shared.vertx

import ai.tock.shared.jackson.mapper
import ai.tock.shared.security.AesGcmCipher
import ai.tock.shared.security.aesGcmCipher
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.http.Cookie
import io.vertx.core.http.CookieSameSite
import io.vertx.ext.web.RoutingContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

open class WebSecurityEncryptedCookiesHandler(
    private val cipher: AesGcmCipher = aesGcmCipher,
    private val clock: Clock = Clock.System,
) : WebSecurityCookiesHandler() {
    companion object {
        protected const val SECURE_COOKIE_PREFIX = "__Http-"
    }

    override val cookieName: String = SECURE_COOKIE_PREFIX + "tock_user_token"
    override val cookieSameSite: CookieSameSite = CookieSameSite.LAX

    override fun readCookie(context: RoutingContext): String? {
        val encryptedCookie = context.request().getCookie(cookieName) ?: return null
        val decrypted = cipher.decryptOrNull(encryptedCookie.value) ?: return null
        val (userId, expiresAt) =
            try {
                mapper.readValue<SessionToken>(decrypted)
            } catch (_: JacksonException) {
                return null
            }
        if (expiresAt != null && expiresAt <= clock.now()) {
            return null
        }
        return userId
    }

    override fun writeCookie(
        context: RoutingContext,
        cookieValue: String,
    ): Cookie {
        val expiresAt = if (cookieAuthMaxAge > 0) clock.now() + cookieAuthMaxAge.seconds else null
        return super.writeCookie(context, cipher.encrypt(mapper.writeValueAsString(SessionToken(cookieValue, expiresAt))))
    }

    /**
     * Stripped down JWT
     */
    data class SessionToken(
        val sub: String,
        val exp: Instant? = null,
    )
}
