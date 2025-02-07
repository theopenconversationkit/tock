/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.connector.web.security


import ai.tock.shared.*
import ai.tock.shared.security.auth.spi.TOCK_USER_ID
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import io.vertx.core.http.Cookie
import io.vertx.core.http.CookieSameSite
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import java.util.*

private val cookieAuthMaxAge = longProperty("tock_web_cookie_auth_max_age", -1)
private val cookieAuthPath = propertyOrNull("tock_web_cookie_auth_path")


class WebSecurityCookiesHandler : WebSecurityHandler {
    private val logger = KotlinLogging.logger {}
    private val executor: Executor get() = injector.provide()

    override fun handle(routingContext: RoutingContext) {
        try {
            executor.executeBlocking {
                routingContext.put(TOCK_USER_ID, getOrCreateUserIdCookie(routingContext))
                routingContext.next()
            }
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
fun getOrCreateUserIdCookie(context: RoutingContext): String {
    val tockUserId = context.request().getCookie(TOCK_USER_ID)?.value

    return if (tockUserId != null && cookieAuthMaxAge < 0) {
        tockUserId // no need to refresh an existing session cookie, it would be a waste of bandwidth
    } else {
        val cookieValue = tockUserId ?: UUID.randomUUID().toString()

        val cookie = Cookie.cookie(TOCK_USER_ID, cookieValue)
            .setHttpOnly(true)
            .setSecure(true)
            .setSameSite(CookieSameSite.NONE)   // bot backend may not be on the same domain as the website frontend

        if (cookieAuthMaxAge >= 0) {
            cookie.setMaxAge(cookieAuthMaxAge)
        }

        if (cookieAuthPath != null) {
            cookie.setPath(cookieAuthPath)
        }

        context.response().addCookie(cookie)

        cookieValue
    }
}
