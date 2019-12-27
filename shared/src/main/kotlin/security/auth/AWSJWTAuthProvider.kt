/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.shared.security.auth

import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.mapProperty
import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import com.google.common.io.BaseEncoding
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
import io.vertx.ext.web.handler.AuthHandler
import mu.KotlinLogging

internal class AWSJWTAuthProvider(vertx: Vertx) : SSOTockAuthProvider(vertx), JWTAuth {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val jwtAlgorithm = property("jwt_algorithm", "ES256")
        private val namespaceMapping = mapProperty("tock_jwt_custom_namespace_mapping", emptyMap())
        private val customRolesMapping = mapProperty("tock_jwt_custom_roles_mapping", emptyMap())
    }

    //cached values
    @Volatile
    private var publicKey: String? = null
    @Volatile
    private var jwtAuthProvider: JWTAuth? = null

    private val executor: Executor get() = injector.provide()

    override fun createAuthHandler(verticle: WebVerticle): AuthHandler = AWSJWTAuthHandler(this, null)

    override fun authenticate(authInfo: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
        jwtAuthProvider = getJwtAuthProvider(authInfo)

        jwtAuthProvider?.authenticate(authInfo) {
            if (it.succeeded()) {
                val user = it.result() as? JWTUser
                if (user != null) {
                    val token = user.principal()
                    val customRoles = token.getString("custom:roles")
                    val roles = parseUserRoles(customRoles)
                    if (roles.isEmpty()) {
                        logger.warn { "empty role for $customRoles" }
                        resultHandler.handle(Future.failedFuture("Unauthorized"))
                    } else {
                        val namespace = parseNamespace(customRoles)
                        if (namespace == null) {
                            logger.warn { "no namespace for $customRoles" }
                            resultHandler.handle(Future.failedFuture("Unauthorized"))
                        } else {
                            executor.executeBlocking {
                                val customName = token.getString("email")
                                val u = injector.provide<TockUserListener>().registerUser(TockUser(customName, namespace, roles))
                                resultHandler.handle(Future.succeededFuture(u))
                            }
                        }
                    }
                } else {
                    resultHandler.handle(Future.failedFuture("Unauthorized"))
                }
            } else {
                if (authInfo.getBoolean("retry") == null) {
                    publicKey = null
                    this.authenticate(authInfo.put("retry", true), resultHandler)
                }
                resultHandler.handle(Future.failedFuture("Unauthorized"))
            }
        } ?: resultHandler.handle(Future.failedFuture("no jwt provider"))
    }

    private fun parseCustomRoles(customRoles: String): List<String> = customRoles
        .split(",")
        .map { it.removePrefix("[").removeSuffix("]").trim() }

    private fun parseNamespace(customRoles: String): String? =
        parseCustomRoles(customRoles)
            .flatMap { namespaceMapping[it]?.split(",")?.map { n -> n.trim() } ?: emptyList() }
            .firstOrNull()

    private fun parseUserRoles(customRoles: String): Set<String> =
        parseCustomRoles(customRoles)
            .flatMap { customRolesMapping[it]?.split(",")?.map { r -> TockUserRole.toRole(r) } ?: emptyList() }
            .filterNotNull()
            .map { it.name }
            .toSet()

    private fun getJwtAuthProvider(authInfo: JsonObject): JWTAuth? {

        return if (publicKey == null) {
            val segments = authInfo.getString("jwt").split("\\.".toRegex())
            if (segments.size == 3) {
                val headerSeg = segments[0]
                val header = JsonObject(String(BaseEncoding.base64Url().decode(headerSeg)))
                val kid = header.getString("kid")
                val newPublicKey = RetrofitAWSPublicKeyClient.getPublicKey(kid)
                if (newPublicKey != null) {
                    publicKey = newPublicKey.replace("-----BEGIN PUBLIC KEY-----\n", "").replace(
                        "\n-----END PUBLIC KEY-----\n",
                        ""
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
                                            publicKey
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
        //do nothing
        return ""
    }
}