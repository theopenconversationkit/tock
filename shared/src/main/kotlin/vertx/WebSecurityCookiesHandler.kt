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

import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.propertyOrNull
import ai.tock.shared.provide
import ai.tock.shared.security.auth.spi.TOCK_USER_ID
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityHandler.Companion.CONNECTOR_PUBLIC_PATH_CONTEXT_KEY
import io.vertx.core.http.Cookie
import io.vertx.core.http.CookieSameSite
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.UUID

open class WebSecurityCookiesHandler : WebSecurityHandler {
    protected val logger = KotlinLogging.logger {}
    protected val executor: Executor get() = injector.provide()
    protected val cookieAuthMaxAge = longProperty("tock_web_cookie_auth_max_age", -1)
    protected val cookieAuthPath = propertyOrNull("tock_web_cookie_auth_path")

    protected open val cookieName = TOCK_USER_ID
    protected open val cookieSameSite = CookieSameSite.NONE // bot backend may not be on the same domain as the website frontend

    override fun handle(routingContext: RoutingContext) {
        logger.debug { "Get or create a cookie for the user ID." }
        executor.executeBlocking {
            try {
                routingContext.put(TOCK_USER_ID, getOrCreateUserIdCookie(routingContext))
                routingContext.next()
            } catch (e: Throwable) {
                logger.error(e.message, e)
                routingContext.fail(e)
            }
        }
    }

    /**
     * Retrieves the value of the tock_user_id cookie or generates it if the user agent did not send such a cookie
     *
     * If the user agent does not have the cookie, or if a cookie Max-Age is specified, this method also instructs
     * the user agent to create/refresh it.
     */
    protected open fun getOrCreateUserIdCookie(context: RoutingContext): String {
        val tockUserId = readCookie(context)

        return if (tockUserId != null && cookieAuthMaxAge < 0) {
            tockUserId // no need to refresh an existing session cookie, it would be a waste of bandwidth
        } else {
            val cookieValue = tockUserId ?: generateUserId()
            writeCookie(context, cookieValue)
            cookieValue
        }
    }

    protected open fun generateUserId(): String = UUID.randomUUID().toString()

    protected open fun readCookie(context: RoutingContext): String? = context.request().getCookie(cookieName)?.value

    protected open fun writeCookie(
        context: RoutingContext,
        cookieValue: String,
    ): Cookie {
        val cookiePath: String? = cookieAuthPath ?: context[CONNECTOR_PUBLIC_PATH_CONTEXT_KEY]

        val cookie =
            Cookie.cookie(cookieName, cookieValue).apply {
                isHttpOnly = true
                isSecure = true
                sameSite = cookieSameSite

                if (cookieAuthMaxAge >= 0) {
                    maxAge = cookieAuthMaxAge
                }

                if (cookiePath != null) {
                    path = cookiePath
                }
            }

        context.response().addCookie(cookie)
        return cookie
    }
}
