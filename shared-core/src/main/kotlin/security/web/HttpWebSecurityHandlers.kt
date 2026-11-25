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

package ai.tock.shared.security.web

import ai.tock.shared.http.HttpRequest
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.propertyOrNull
import ai.tock.shared.security.AesGcmCipher
import ai.tock.shared.security.aesGcmCipher
import ai.tock.shared.security.auth.spi.HttpWebSecurity
import ai.tock.shared.security.auth.spi.HttpWebSecurityContext
import ai.tock.shared.security.auth.spi.HttpWebSecurityResult
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** Nothing to do: the user id of the request is kept (WebSecurityPassthroughHandler). */
object HttpWebSecurityPassthrough : HttpWebSecurity {
    override fun authenticate(
        request: HttpRequest,
        context: HttpWebSecurityContext,
    ): HttpWebSecurityResult = HttpWebSecurityResult()
}

/** The SameSite attribute of a cookie. */
enum class HttpCookieSameSite(
    val value: String,
) {
    NONE("None"),
    LAX("Lax"),
    STRICT("Strict"),
}

/**
 * The user id is stored in a cookie, created when the user agent does not send it (WebSecurityCookiesHandler):
 * `tock_web_cookie_auth_max_age` (session cookie by default) and `tock_web_cookie_auth_path` (the public path of
 * the connector by default).
 */
open class HttpWebSecurityCookies : HttpWebSecurity {
    protected val cookieAuthMaxAge = longProperty("tock_web_cookie_auth_max_age", -1)
    protected val cookieAuthPath = propertyOrNull("tock_web_cookie_auth_path")

    // TOCK_USER_ID of tock-shared
    protected open val cookieName = "tock_user_id"

    // the bot backend may not be on the same domain as the website frontend
    protected open val cookieSameSite = HttpCookieSameSite.NONE

    override fun authenticate(
        request: HttpRequest,
        context: HttpWebSecurityContext,
    ): HttpWebSecurityResult {
        val tockUserId = readCookie(request.cookie(cookieName))
        return if (tockUserId != null && cookieAuthMaxAge < 0) {
            // no need to refresh an existing session cookie, it would be a waste of bandwidth
            HttpWebSecurityResult(tockUserId)
        } else {
            val userId = tockUserId ?: generateUserId()
            HttpWebSecurityResult(userId, setCookies = listOf(setCookie(cookieValue(userId), cookieAuthPath ?: context.publicPath)))
        }
    }

    protected open fun generateUserId(): String = UUID.randomUUID().toString()

    /** The user id of the cookie value, null if there is none or if it is not valid. */
    protected open fun readCookie(value: String?): String? = value

    /** The cookie value of [userId]. */
    protected open fun cookieValue(userId: String): String = userId

    /** The `Set-Cookie` header, as Vert.x (Netty) encodes it. */
    private fun setCookie(
        value: String,
        path: String?,
    ): String =
        buildList {
            add("$cookieName=$value")
            if (cookieAuthMaxAge >= 0) {
                add("Max-Age=$cookieAuthMaxAge")
                add("Expires=" + DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(cookieAuthMaxAge)))
            }
            if (path != null) add("Path=$path")
            add("Secure")
            add("HTTPOnly")
            add("SameSite=${cookieSameSite.value}")
        }.joinToString("; ")
}

/**
 * The user id is stored in an encrypted cookie with an expiration date (WebSecurityEncryptedCookiesHandler).
 */
open class HttpWebSecurityEncryptedCookies(
    private val cipher: AesGcmCipher = aesGcmCipher,
    private val clock: Clock = Clock.System,
) : HttpWebSecurityCookies() {
    override val cookieName: String = "__Http-tock_user_token"
    override val cookieSameSite: HttpCookieSameSite = HttpCookieSameSite.LAX

    override fun readCookie(value: String?): String? {
        val decrypted = value?.let { cipher.decryptOrNull(it) } ?: return null
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

    override fun cookieValue(userId: String): String {
        val expiresAt = if (cookieAuthMaxAge > 0) clock.now() + cookieAuthMaxAge.seconds else null
        return cipher.encrypt(mapper.writeValueAsString(SessionToken(userId, expiresAt)))
    }

    /** Stripped down JWT. */
    data class SessionToken(
        val sub: String,
        val exp: Instant? = null,
    )
}

/** The value of the cookie [name] of the `Cookie` headers. */
fun HttpRequest.cookie(name: String): String? =
    headers["Cookie"]
        .orEmpty()
        .flatMap { it.split(';') }
        .map { it.trim() }
        .firstOrNull { it.substringBefore('=') == name }
        ?.substringAfter('=')
