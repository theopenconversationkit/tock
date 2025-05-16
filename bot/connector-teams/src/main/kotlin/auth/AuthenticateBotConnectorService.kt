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
package ai.tock.bot.connector.teams.auth

import ai.tock.shared.devEnvironment
import com.microsoft.bot.schema.Activity
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import io.vertx.core.MultiMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Instant
import java.util.Date

@Suppress("PropertyName")
internal class AuthenticateBotConnectorService(
    private val appId: String
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val ISSUER_BOT_CONNECTOR_SERVICE = "https://api.botframework.com"
        val ISSUER_BOT_FWK_EMULATOR = listOf(
            "https://sts.windows.net/d6d49420-f39b-4df7-a1dc-d59a935871db/",
            "https://sts.windows.net/f8cdef31-a31e-4b4a-93e4-5f571e91255a/",
            "https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0"
        )
        const val BEARER_PREFIX = "Bearer "
    }

    /**
     * Here we check the token header validity.
     *
     * In dev environment, we test also the Bot Framework Emulator token.
     */
    fun checkRequestValidity(jwkHandler: JWKHandler, headers: MultiMap, activity: Activity) {
        var isFromTheBotConnectorService = false
        var isFromTheBotFwkEmulator = false
        var errorStackTrace: String? = ""

        runBlocking {
            val jobAuthenticateFromBotConnectorService = async(Dispatchers.Default) {
                isFromTheBotConnectorService = try {
                    checkTokenValidityFromConnectorService(jwkHandler, headers, activity)
                } catch (e: Exception) {
                    errorStackTrace = e.message
                    false
                }
            }
            val jobAuthenticateFromBotFrameworkEmulator = if (devEnvironment) {
                async(Dispatchers.Default) {
                    isFromTheBotFwkEmulator = checkTokenValidityFromEmulator(jwkHandler, headers)
                }
            } else null

            jobAuthenticateFromBotConnectorService.join()
            jobAuthenticateFromBotFrameworkEmulator?.join()
        }

        if (!isFromTheBotConnectorService && !isFromTheBotFwkEmulator) {
            throw ForbiddenException("Unvalid JWT in Authorization Header : $errorStackTrace")
        }
    }

    /**
     * @see https://docs.microsoft.com/en-us/azure/bot-service/rest-api/bot-framework-rest-connector-authentication?view=azure-bot-service-4.0#emulator-to-bot
     *
     * The token was sent in the HTTP Authorization header with "Bearer" scheme.
     * The token is valid JSON that conforms to the JWT standard.
     * The token contains a valid "issuer" claim.
     * The token contains an "audience" claim with a value equal to the bot's Microsoft App ID.
     * The token contains an "appid" claim with the value equal to the bot's Microsoft App ID
     * The token is within its validity period. Industry-standard clock-skew is 5 minutes.
     * The token has a valid cryptographic signature, with a key listed in the OpenID keys document that was retrieved in Step 3, using the signing algorithm that is specified in the id_token_signing_alg_values_supported property of the Open ID Metadata document that was retrieved in Step 2.
     */
    private fun checkTokenValidityFromEmulator(jwkHandler: JWKHandler, headers: MultiMap): Boolean {
        logger.debug("Validating token from incoming request...")
        val authorizationHeader = headers[AUTHORIZATION_HEADER]
        try {
            if (!authorizationHeader.contains(BEARER_PREFIX)) throw ForbiddenException("Authorization Header does not contains the Bearer scheme")
            val token = authorizationHeader.removePrefix(BEARER_PREFIX)
            val signedJWT = SignedJWT.parse(token)
            if (!ISSUER_BOT_FWK_EMULATOR.contains(signedJWT.jwtClaimsSet.issuer)) {
                logger.error("Invalid issuer : ${signedJWT.jwtClaimsSet.issuer}")
                throw ForbiddenException("Issuer is not valid")
            }
            if (!signedJWT.jwtClaimsSet.audience.contains(appId)) throw ForbiddenException("Audience is not valid")
            if (signedJWT.jwtClaimsSet.getClaim("azp") != appId) throw ForbiddenException("AppId claim is not valid")
            checkValidity(signedJWT)
            checkSignature(signedJWT, jwkHandler)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * @see https://docs.microsoft.com/en-us/azure/bot-service/rest-api/bot-framework-rest-connector-authentication?view=azure-bot-service-4.0#connector-to-bot
     *
     * The token was sent in the HTTP Authorization header with "Bearer" scheme.
     * The token is valid JSON that conforms to the JWT standard.
     * The token contains an "issuer" claim with value of https://api.botframework.com.
     * The token contains an "audience" claim with a value equal to the bot's Microsoft App ID.
     * The token is within its validity period. Industry-standard clock-skew is 5 minutes.
     * The token has a valid cryptographic signature, with a key listed in the OpenID keys document that was retrieved in Step 3, using the signing algorithm that is specified in the id_token_signing_alg_values_supported property of the Open ID Metadata document that was retrieved in Step 2.
     * The token contains a "serviceUrl" claim with value that matches the servieUrl property at the root of the Activity object of the incoming request.    *
     */
    private fun checkTokenValidityFromConnectorService(jwkHandler: JWKHandler, headers: MultiMap, activity: Activity): Boolean {
        logger.debug("Validating token from incoming request...")
        val authorizationHeader = headers[AUTHORIZATION_HEADER]
        try {
            if (!authorizationHeader.contains(BEARER_PREFIX)) throw ForbiddenException("Authorization Header does not contains the Bearer scheme")
            val token = authorizationHeader.removePrefix(BEARER_PREFIX)
            val signedJWT = SignedJWT.parse(token)
            if (signedJWT.jwtClaimsSet.issuer != ISSUER_BOT_CONNECTOR_SERVICE) throw ForbiddenException("Issuer is not valid")
            if (!signedJWT.jwtClaimsSet.audience.contains(appId)) throw ForbiddenException("Audience is not valid")
            checkValidity(signedJWT)
            checkSignature(signedJWT, jwkHandler)
            if ((
                signedJWT.jwtClaimsSet.getClaim("serviceurl")
                    ?: throw ForbiddenException("Token doesn't contains any serviceUrl Claims")
                ) != activity.serviceUrl
            ) {
                throw ForbiddenException("ServiceUrl in token Authorization and in activity doesn't match")
            }
        } catch (e: Exception) {
            throw ForbiddenException("Unvalid JWT in Authorization Header : ${e.message}")
        }
        return true
    }

    private fun checkValidity(signedJWT: SignedJWT) {
        val notBefore = signedJWT.jwtClaimsSet.notBeforeTime
        val expiration = signedJWT.jwtClaimsSet.expirationTime
        val now = Date.from(Instant.now())
        if (now.after(expiration)) throw ForbiddenException("Authorization header is expired")
        if (now.before(notBefore)) throw ForbiddenException("Authorization header is not valid yet")
    }

    private fun checkSignature(signedJWT: SignedJWT, jwkHandler: JWKHandler) {
        jwkHandler.getJWK()?.keys?.forEach {
            if ((it.kid == signedJWT.header.keyID)) {
                val algo = it.kty
                val verifier: JWSVerifier = when (algo) {
                    "RSA" -> RSASSAVerifier(RSAKey.parse(it.toString()))
                    "EC" -> ECDSAVerifier(ECKey.parse(it.toString()))
                    else -> throw ForbiddenException("$algo is not a supported algorithm")
                }
                if (signedJWT.verify(verifier)) {
                    logger.debug("the token received from botconnector is good")
                    return
                }
            }
        }
        throw JOSEException("Signature verification unsuccessfull")
    }
}
