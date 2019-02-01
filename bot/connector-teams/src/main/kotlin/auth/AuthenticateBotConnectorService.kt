package fr.vsct.tock.bot.connector.teams.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.google.gson.Gson
import com.microsoft.bot.schema.models.Activity
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import fr.vsct.tock.bot.connector.teams.auth.AuthenticateBotConnectorService.Companion.AUTHORIZATION_HEADER
import fr.vsct.tock.bot.connector.teams.auth.AuthenticateBotConnectorService.Companion.BEARER_PREFIX
import fr.vsct.tock.bot.engine.nlp.NlpProxyBotListener.logger
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
import java.util.*


@Suppress("PropertyName")
class AuthenticateBotConnectorService(private val headers: MultiMap, private val appId: String, private val activity: Activity) {


    internal var microsoftOpenIdMetadataApi: MicrosoftOpenIdMetadataApi
    internal var microsoftJwksApi: MicrosoftJwksApi

    private val logger = KotlinLogging.logger {}
    internal val teamsMapper: ObjectMapper = mapper.copy().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    @Volatile lateinit var id_token_signing_alg_values_supported: List<String>
    @Volatile lateinit var jwks_uri: String
    @Volatile lateinit var microsoftValidSigningKeys: MicrosoftValidSigningKeys

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

    fun isRequestFromConnectorBotService(): Boolean {
        if (!isKeysNotSetted() || !isKeyExpired()) {
            getOpenIdMetadata()
            getJWK()
        }
        return isTokenValid()
    }

    private fun isKeysNotSetted(): Boolean {
        return false
    }

    private fun isKeyExpired(): Boolean {
        return false
    }

    internal fun getOpenIdMetadata() {
        logger.debug { "Getting openidMetadata" }
        val microsoftOpenidMetadata = microsoftOpenIdMetadataApi.getMicrosoftOpenIdMetadata().execute()
        val response = microsoftOpenidMetadata.body()
        id_token_signing_alg_values_supported = response?.idTokenSigningAlgValuesSupported ?: throw IOException("Error : Unable to get OpenidMetadata")
        jwks_uri = response.jwksUri
    }

    internal fun getJWK() {
        logger.debug("Getting jwks keys")
        microsoftValidSigningKeys = microsoftJwksApi.getJwk(
            jwks_uri
        ).execute().body() ?: throw IOException("Error : Unable to get JWK signatures")
    }

    /**
     * @see https://docs.microsoft.com/en-us/azure/bot-service/rest-api/bot-framework-rest-connector-authentication?view=azure-bot-service-4.0#connector-to-bot
     * The token was sent in the HTTP Authorization header with "Bearer" scheme.
     * The token is valid JSON that conforms to the JWT standard.
     * The token contains an "issuer" claim with value of https://api.botframework.com.
     * The token contains an "audience" claim with a value equal to the bot's Microsoft App ID.
     * The token is within its validity period. Industry-standard clock-skew is 5 minutes.
     * The token has a valid cryptographic signature, with a key listed in the OpenID keys document that was retrieved in Step 3, using the signing algorithm that is specified in the id_token_signing_alg_values_supported property of the Open ID Metadata document that was retrieved in Step 2.
     * The token contains a "serviceUrl" claim with value that matches the servieUrl property at the root of the Activity object of the incoming request.    *
     */
    internal fun isTokenValid(): Boolean {
        logger.debug("Validating token from incoming request...")
        val authorizationHeader = headers[AUTHORIZATION_HEADER]
        try {
            if (!authorizationHeader.contains(BEARER_PREFIX)) throw ForbiddenException("Authorization Header does not contains the Bearer scheme")
            val token = authorizationHeader.removePrefix(BEARER_PREFIX)
            val signedJWT = SignedJWT.parse(token)
            if (signedJWT.jwtClaimsSet.issuer != ISSUER) throw ForbiddenException("Issuer is not valid")
            if (!signedJWT.jwtClaimsSet.audience.contains(appId)) throw ForbiddenException("Audience is not valid")
            checkValidity(signedJWT)
            checkSignature(signedJWT)
            if ((signedJWT.jwtClaimsSet.getClaim("serviceurl") ?: throw ForbiddenException("Token doesn't contains any serviceUrl Claims")) != activity.serviceUrl()) {
                throw ForbiddenException("ServiceUrl in token Authorization and in activity doesn't match")
            }
        } catch (e: Exception) {
            logger.error("Unvalid JWT in Authorization Header")
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

    private fun checkSignature(signedJWT: SignedJWT) {
        microsoftValidSigningKeys.keys.forEach {
            if (it.endorsements?.contains(activity.channelId()) == true && (it.kid == signedJWT.header.keyID)) {
                val algo = it.kty
                val verifier: JWSVerifier = when (algo) {
                    "RSA" -> RSASSAVerifier(RSAKey.parse(it.toString()))
                    "EC" -> ECDSAVerifier(ECKey.parse(it.toString()))
                    else  -> throw ForbiddenException("$algo is not a supported algorithm")
                }
                if (signedJWT.verify(verifier)) {
                    logger.debug("the token received from botconnector is good")
                    return
                }
            }
        }
        throw JOSEException("Signature verification unsuccessfull")
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
        val x5t: String,
        val n: String,
        val e: String,
        val x5c: List<String>,
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