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

package ai.tock.shared.security.auth.spi

import ai.tock.shared.propertyOrNull

enum class WebSecurityMode {
    /**
     * Uses the global default, set by runtime environment
     *
     * @see getDefault
     */
    DEFAULT,

    /**
     * Stores the user id in a plain HTTP cookie
     */
    COOKIES,

    /**
     * Hardened implementation of a cookie-based session handler
     *
     * Stores the user ID in an encrypted HTTP cookie with expiration date
     */
    COOKIES_ENCRYPTED,

    /**
     * Pass the interceptor without any changes
     */
    PASSTHROUGH,

    /**
     * Parse the JWT, to validate signature, check token revocation and manage authorization
     */
    JWT,

    ;

    companion object {
        private val cookieAuth = propertyOrNull("tock_web_cookie_auth")

        fun find(mode: String?): WebSecurityMode = mode?.let(::findByName)?.takeUnless { it == DEFAULT } ?: getDefault()

        fun findByName(mode: String): WebSecurityMode? = WebSecurityMode.entries.firstOrNull { it.name == mode }

        /**
         * If "env.tock_web_cookie_auth" is set, uses the COOKIES or COOKIES_ENCRYPTED mode, otherwise nothing (PASSTHROUGH mode)
         */
        fun getDefault(): WebSecurityMode =
            when (cookieAuth?.lowercase()) {
                "encrypted" -> COOKIES_ENCRYPTED
                "basic", "true" -> COOKIES
                else -> PASSTHROUGH
            }
    }
}
