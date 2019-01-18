package fr.vsct.tock.shared.security

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.io.BaseEncoding
import fr.vsct.tock.shared.property
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

class AWSJWTAuthProviderImpl(val vertx: Vertx) : JWTAuth {

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
                    var user = TockUser(customName, namespace, TockUserRole.values().map { it.toString() }.toSet())
                    user.isAuthorized(roleName) { result ->
                        if (result.succeeded()) {
                            resultHandler.handle(Future.succeededFuture(user))
                        } else {
                                resultHandler.handle(Future.failedFuture("Unauthorized."))
                        }
                    }
                } else {
                    resultHandler.handle(Future.failedFuture("Unauthorized"))
                }
            } else {
                if(authInfo.getBoolean("retry")== null) {
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