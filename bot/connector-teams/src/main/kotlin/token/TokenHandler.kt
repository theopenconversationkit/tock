package fr.vsct.tock.bot.connector.teams.token

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import fr.vsct.tock.shared.*
import fr.vsct.tock.shared.jackson.mapper
import mu.KotlinLogging
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Handle the generation and the refresh of the token header
 * This token is mandatory in request from bot to teams via microsoft-api
 */
class TokenHandler(private val appId: String, private val password: String) {

    private val logger = KotlinLogging.logger {}

    @Volatile
    var token: String? = null
    @Volatile
    private var tokenExpiration: Instant? = null

    val teamsMapper: ObjectMapper = mapper.copy().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    @Volatile
    private lateinit var tokenTimerTask: Timer

    var loginApi: LoginMicrosoftOnline = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_microsoft_request_timeout", 30000),
        logger,
        level = Level.BASIC
    )
        .baseUrl("https://login.microsoftonline.com")
        .addJacksonConverter(teamsMapper)
        .build()
        .create()

    fun checkToken() {
        if (this.token == null || isTokenExpired()) {
            fetchToken()
        }
    }

    fun isTokenExpired(): Boolean {
        logger.debug { "IS TOKEN EXPIRED" }
        if (Instant.now().isAfter(
                tokenExpiration?.minus(
                    10,
                    ChronoUnit.SECONDS
                )
            )
        ) {
            return true
        }
        return false
    }

    private fun fetchToken() {
        val response = loginApi.login(
            clientId = appId, clientSecret = password
        ).execute()
        token = response.body()?.accessToken ?: error("empty access token")
        tokenExpiration = Instant.now().plus(response.body()?.expiresIn!!, ChronoUnit.SECONDS)
    }

    fun launchTokenCollector(connectorId: String, msInterval: Long =60 * 60 * 1000L) {
        tokenTimerTask = fixedRateTimer(name = "microsoft-api-token-handling-$connectorId", initialDelay = 0L, period = msInterval) {
            checkToken()
        }
    }

    fun stopTokenCollector() {
        if (::tokenTimerTask.isInitialized) {
            tokenTimerTask.cancel()
            tokenTimerTask.purge()
        } else {
            logger.error("Trying to stop an uninitialized tokentimertask !")
        }
    }

}
