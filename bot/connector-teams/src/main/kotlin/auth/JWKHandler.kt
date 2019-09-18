package fr.vsct.tock.bot.connector.teams.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import fr.vsct.tock.shared.Level
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.devEnvironment
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

/**
 * Handle the generation and the refresh of ths jks
 * Those jks are used to check the authenticity of incoming request
 */
class JWKHandler {

    @Volatile
    private var tokenIds: ArrayList<String>? = null

    @Volatile
    var cacheKeys: MicrosoftValidSigningKeys? = null

    private val logger = KotlinLogging.logger {}

    private val logLevel = if (logger.isDebugEnabled) {
        Level.BODY
    } else {
        Level.BASIC
    }

    private val teamsMapper: ObjectMapper = mapper.copy().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    private lateinit var jwkTimerTask: Timer

    companion object {
        private var OPENID_METADATA_LOCATION: String = "https://login.botframework.com/v1/"
        private var JKS_BASE_LOCATION: String = "https://login.botframework.com/v1/.well-known/keys/"
        private var OPENID_METADATA_LOCATION_BOT_FWK_EMULATOR = "https://login.microsoftonline.com/botframework.com/v2.0/"
    }

    private var microsoftOpenIdMetadataApiForBotFwkEmulator: MicrosoftOpenIdMetadataApi = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_microsoft_request_timeout", 5000),
        this.logger,
        logLevel
    )
        .baseUrl(OPENID_METADATA_LOCATION_BOT_FWK_EMULATOR)
        .addJacksonConverter(teamsMapper)
        .build()
        .create()

    private var microsoftOpenIdMetadataApi: MicrosoftOpenIdMetadataApi = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_microsoft_request_timeout", 5000),
        logger,
        logLevel
    )
        .baseUrl(OPENID_METADATA_LOCATION)
        .addJacksonConverter(teamsMapper)
        .build()
        .create()

    private var microsoftJwksApi: MicrosoftJwksApi = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_microsoft_request_timeout", 5000),
        this.logger,
        logLevel
    )
        .baseUrl(JKS_BASE_LOCATION)
        .addJacksonConverter(teamsMapper)
        .build()
        .create()

    fun launchJWKCollector(connectorId: String, msInterval: Long = 23 * 60 * 60 * 1000L) {
        jwkTimerTask = fixedRateTimer("microsoft-jwk-collector-$connectorId", initialDelay = 0L, period = msInterval) {
            collectJWK()
        }
    }

    fun stopJWKCollector() {
        if (::jwkTimerTask.isInitialized) {
            jwkTimerTask.cancel()
            jwkTimerTask.purge()
        }
    }

    private fun collectJWK() {
        logger.debug("Getting new jwks")
        val microsoftOpenidMetadata = microsoftOpenIdMetadataApi.getMicrosoftOpenIdMetadata().execute()
        val response = microsoftOpenidMetadata.body()
        tokenIds = response?.idTokenSigningAlgValuesSupported
            ?: error("Error : Unable to get OpenidMetadata to validate BotConnectorServiceKeys")
        val keysForBotConnectorService = microsoftJwksApi.getJwk(
            response.jwksUri
        ).execute().body()
            ?: error("Error : Unable to get JWK signatures to validate BotConnectorServiceKeys")
        val listOfKeys: MutableList<MicrosoftValidSigningKey> = keysForBotConnectorService.keys.toMutableList()

        if (devEnvironment) {
            val microsoftOpenidMetadataBotFwkEmulator = microsoftOpenIdMetadataApiForBotFwkEmulator.getMicrosoftOpenIdMetadataForBotFwkEmulator().execute()
            val nextResponse = microsoftOpenidMetadataBotFwkEmulator.body()
            tokenIds?.addAll(
                nextResponse?.idTokenSigningAlgValuesSupported
                    ?: error("Error : Unable to get OpenidMetadata to validate BotFrameworkEmulatorKeys")
            )
            val keysForBotFwkEmulator = microsoftJwksApi.getJwk(
                nextResponse!!.jwksUri
            ).execute().body()
                ?: error("Error : Unable to get JWK signatures to validate BotFrameworkEmulatorKeys")
            listOfKeys.addAll(keysForBotFwkEmulator.keys)
        }

        cacheKeys = MicrosoftValidSigningKeys(listOfKeys)
    }

    fun getJWK(): MicrosoftValidSigningKeys? {
        if (cacheKeys == null) {
            collectJWK()
        }
        return cacheKeys
    }

    internal fun setOpenIdMatadataLocation(url: String) {
        logger.debug("setOpenIdMatadataLocation : $url")
        OPENID_METADATA_LOCATION = url
        microsoftOpenIdMetadataApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_microsoft_request_timeout", 5000),
            logger,
            level = Level.BASIC
        )
            .baseUrl(OPENID_METADATA_LOCATION)
            .addJacksonConverter(teamsMapper)
            .build()
            .create()
    }

    internal fun setOpenIdMatadataLocationBotFwkEmulator(url: String) {
        logger.debug("setOpenIdMatadataLocationBotFwkEmulator : $url")
        OPENID_METADATA_LOCATION_BOT_FWK_EMULATOR = url
        microsoftOpenIdMetadataApiForBotFwkEmulator = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_microsoft_request_timeout", 5000),
            logger,
            level = Level.BASIC
        )
            .baseUrl(OPENID_METADATA_LOCATION)
            .addJacksonConverter(teamsMapper)
            .build()
            .create()
    }

    internal fun setJKSBaseLocation(url: String) {
        logger.debug("setJKSBaseLocation : $url")
        JKS_BASE_LOCATION = url
        microsoftJwksApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_microsoft_request_timeout", 5000),
            this.logger,
            level = Level.BASIC
        )
            .baseUrl(JKS_BASE_LOCATION)
            .addJacksonConverter(teamsMapper)
            .build()
            .create()
    }
}
