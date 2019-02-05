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
package fr.vsct.tock.bot.connector.teams.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.gson.Gson
import com.microsoft.bot.schema.models.Activity
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import fr.vsct.tock.shared.Level
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import io.vertx.core.MultiMap
import mu.KotlinLogging
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.IOException
import java.io.Serializable
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit


@Suppress("PropertyName")
internal class AuthenticateBotConnectorService(private val appId: String) {

    var microsoftOpenIdMetadataApi: MicrosoftOpenIdMetadataApi
    var microsoftJwksApi: MicrosoftJwksApi

    private val logger = KotlinLogging.logger {}
    val teamsMapper: ObjectMapper = mapper.copy().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    @Volatile
    private var tokenIds: List<String>? = null
    @Volatile
    var cacheKeys: Cache<String, MicrosoftValidSigningKeys> = CacheBuilder.newBuilder()
        .maximumSize(1)
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build()

    init {
        microsoftOpenIdMetadataApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_microsoft_request_timeout", 5000),
            this.logger,
            level = Level.BASIC
        )
            .baseUrl(OPENID_METADATA_LOCATION)
            .addJacksonConverter(teamsMapper)
            .build()
            .create()

        microsoftJwksApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_microsoft_request_timeout", 5000),
            this.logger,
            level = Level.BASIC
        )
            .baseUrl("https://login.botframework.com/v1/.well-known/keys/")
            .addJacksonConverter(teamsMapper)
            .build()
            .create()
    }

    companion object {
        const val OPENID_METADATA_LOCATION = "https://login.botframework.com/v1/"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val ISSUER = "https://api.botframework.com"
        const val BEARER_PREFIX = "Bearer "
    }

    fun checkRequestFromConnectorBotService(headers: MultiMap, activity: Activity) {
        checkTokenValidity(headers, activity)
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
    private fun checkTokenValidity(headers: MultiMap, activity: Activity) {
        logger.debug("Validating token from incoming request...")
        val authorizationHeader = headers[AUTHORIZATION_HEADER]
        try {
            if (!authorizationHeader.contains(BEARER_PREFIX)) throw ForbiddenException("Authorization Header does not contains the Bearer scheme")
            val token = authorizationHeader.removePrefix(BEARER_PREFIX)
            val signedJWT = SignedJWT.parse(token)
            if (signedJWT.jwtClaimsSet.issuer != ISSUER) throw ForbiddenException("Issuer is not valid")
            if (!signedJWT.jwtClaimsSet.audience.contains(appId)) throw ForbiddenException("Audience is not valid")
            checkValidity(signedJWT)
            checkSignature(signedJWT, activity)
            if ((signedJWT.jwtClaimsSet.getClaim("serviceurl")
                        ?: throw ForbiddenException("Token doesn't contains any serviceUrl Claims")) != activity.serviceUrl()
            ) {
                throw ForbiddenException("ServiceUrl in token Authorization and in activity doesn't match")
            }
        } catch (e: Exception) {
            logger.error("Unvalid JWT in Authorization Header : ${e.message}")
            throw ForbiddenException("Unvalid JWT in Authorization Header : ${e.message}")
        }
    }

    private fun checkValidity(signedJWT: SignedJWT) {
        val notBefore = signedJWT.jwtClaimsSet.notBeforeTime
        val expiration = signedJWT.jwtClaimsSet.expirationTime
        val now = Date.from(Instant.now())
        if (now.after(expiration)) throw ForbiddenException("Authorization header is expired")
        if (now.before(notBefore)) throw ForbiddenException("Authorization header is not valid yet")
    }

    private fun checkSignature(signedJWT: SignedJWT, activity: Activity) {
        cacheKeys.get("jwks_uri") {
            getJWK()
        }?.keys?.forEach {
            if (it.endorsements?.contains(activity.channelId()) == true && (it.kid == signedJWT.header.keyID)) {
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


    private fun getJWK(): MicrosoftValidSigningKeys {
        logger.debug("Getting new jwks")
        val microsoftOpenidMetadata = microsoftOpenIdMetadataApi.getMicrosoftOpenIdMetadata().execute()
        val response = microsoftOpenidMetadata.body()
        tokenIds =
            response?.idTokenSigningAlgValuesSupported ?: throw IOException("Error : Unable to get OpenidMetadata")
        return microsoftJwksApi.getJwk(
            response.jwksUri
        ).execute().body() ?: throw IOException("Error : Unable to get JWK signatures")
    }

    data class MicrosoftOpenidMetadata(
        val issuer: String,
        val authorizationEndpoint: String,
        val jwksUri: String,
        val idTokenSigningAlgValuesSupported: List<String>,
        val tokenEndpointAuthMethodsSupported: List<String>
    )

    internal interface MicrosoftOpenIdMetadataApi {

        @GET(".well-known/openidconfiguration/")
        fun getMicrosoftOpenIdMetadata(): Call<MicrosoftOpenidMetadata>
    }

    data class MicrosoftValidSigningKeys(
        val keys: List<MicrosoftValidSigningKey>
    ) : Serializable

    data class MicrosoftValidSigningKey(
        val kty: String,
        val use: String,
        val kid: String,
        val x5t: String?,
        val n: String,
        val e: String,
        val x5c: List<String>?,
        val endorsements: List<String>?
    ) : Serializable {

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    internal interface MicrosoftJwksApi {

        @GET
        fun getJwk(
            @Url url: String
        ): Call<MicrosoftValidSigningKeys>
    }
}