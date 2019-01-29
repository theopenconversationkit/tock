/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.shared.security.auth

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.io.BaseEncoding
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.security.TockUser
import fr.vsct.tock.shared.security.TockUserRole
import fr.vsct.tock.shared.vertx.WebVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.auth.jwt.impl.JWTUser
import io.vertx.ext.jwt.JWTOptions
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler

internal class AWSJWTAuthProvider(val vertx: Vertx) : JWTAuth, TockAuthProvider {

    companion object {
        private val AWS_PUBLIC_KEY_LABEL = "awsPublicKey"
        private val jwtAlgorithm = property("jwt_algorithm", "ES256")
        private val publicKeyCache: Cache<String, String> =
            CacheBuilder
                .newBuilder()
                .maximumSize(1)
                .build()
        private var jwtAuthProvider: JWTAuth? = null
    }

    private class ExceptPathHandler(
        val healthcheckPath: String?,
        val handler: Handler<RoutingContext>
    ) : Handler<RoutingContext> {

        override fun handle(c: RoutingContext) {
            if (c.request().path() == healthcheckPath) {
                c.next()
            } else {
                handler.handle(c)
            }
        }
    }

    override val sessionCookieName: String get() = "tock-sso-session"

    override fun protectPaths(
        verticle: WebVerticle,
        pathsToProtect: Set<String>,
        cookieHandler: CookieHandler,
        sessionHandler: SessionHandler,
        userSessionHandler: UserSessionHandler
    ) {
        val authHandler = AWSJWTAuthHandler(this, null)
        with(verticle) {
            router.route("/*").handler(ExceptPathHandler(healthcheckPath, cookieHandler))
            router.route("/*").handler(ExceptPathHandler(healthcheckPath, sessionHandler))
            router.route("/*").handler(ExceptPathHandler(healthcheckPath, userSessionHandler))
            router.route("/*").handler(ExceptPathHandler(healthcheckPath, authHandler))

            router.get("$basePath/user").handler { it.response().end(mapper.writeValueAsString(it.user())) }
        }
    }

    override fun authenticate(authInfo: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
        jwtAuthProvider = getJwtAuthProvider(authInfo)

        jwtAuthProvider?.authenticate(authInfo) {
            if (it.succeeded()) {
                val user = it.result() as? JWTUser
                if (user != null) {
                    val token = user.principal()
                    val customRole = token.getString("custom:roles")
                    val customName = token.getString("email")
                    val (namespace, roleName) = parseCustomRole(customRole)
                    val u = TockUser(customName, namespace, TockUserRole.values().map { it.toString() }.toSet())
                    u.isAuthorized(roleName) { result ->
                        if (result.result()) {
                            resultHandler.handle(Future.succeededFuture(u))
                        } else {
                            resultHandler.handle(Future.failedFuture("Unauthorized."))
                        }
                    }
                } else {
                    resultHandler.handle(Future.failedFuture("Unauthorized"))
                }
            } else {
                if (authInfo.getBoolean("retry") == null) {
                    publicKeyCache.invalidate(AWS_PUBLIC_KEY_LABEL)
                    this.authenticate(authInfo.put("retry", true), resultHandler)
                }
                resultHandler.handle(Future.failedFuture("Unauthorized"))
            }
        }
    }

    /**
     *  TODO a workaround
     */
    private fun parseCustomRole(customRole: String): Pair<String, String> {
        return Pair("OUIBOT", "technicalAdmin")
    }

    private fun getJwtAuthProvider(authInfo: JsonObject): JWTAuth? {

        val publicKey = publicKeyCache.getIfPresent("awsPublicKey")
        return if (publicKey == null) {
            val segments = authInfo.getString("jwt").split("\\.".toRegex())
            if (segments.size == 3) {
                val headerSeg = segments[0]
                val header = JsonObject(String(BaseEncoding.base64Url().decode(headerSeg)))
                val kid = header.getString("kid")
                val newPublicKey = RetrofitAWSPublicKeyClient.getPublicKey(kid)
                if (newPublicKey != null) {
                    publicKeyCache.put(
                        AWS_PUBLIC_KEY_LABEL,
                        newPublicKey.replace("-----BEGIN PUBLIC KEY-----\n", "").replace(
                            "\n-----END PUBLIC KEY-----\n",
                            ""
                        )
                    )
                    JWTAuth.create(
                        vertx, JWTAuthOptions(
                            JsonObject()
                                .put(
                                    "pubSecKeys", JsonArray()
                                        .add(
                                            JsonObject()
                                                .put("algorithm", jwtAlgorithm)
                                                .put(
                                                    "publicKey",
                                                    newPublicKey.replace(
                                                        "-----BEGIN PUBLIC KEY-----\n",
                                                        ""
                                                    ).replace("\n-----END PUBLIC KEY-----\n", "")
                                                )
                                        )
                                )
                        )
                    )
                } else null
            } else null
        } else jwtAuthProvider
    }

    override fun generateToken(claims: JsonObject?, options: JWTOptions?): String {
        return ""
    }
}