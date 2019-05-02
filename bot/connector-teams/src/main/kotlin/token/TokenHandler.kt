package fr.vsct.tock.bot.connector.teams.token

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import fr.vsct.tock.shared.Level
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Handle the generation and the refresh of the token header
 * This token is mandatory in request from bot to teams via microsoft-api
 */
object TokenHandler {

    private val logger = KotlinLogging.logger {}

    @Volatile
    var token: String? = null
    @Volatile
    private var tokenExpiration: Instant? = null

    @Volatile
    private lateinit var appId: String
    @Volatile
    private lateinit var password: String

    val teamsMapper: ObjectMapper = mapper.copy().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
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

    fun setId(appId: String, password: String) {
        this.appId = appId
        this.password = password
    }

    fun launchTokenCollector(appId: String, password: String, msInterval: Long = 60 * 60 * 1000L) {
        setId(appId, password)
        tokenTimerTask = fixedRateTimer(name = "microsoft-api-token-handling", initialDelay = 0L, period = msInterval) {
            checkToken()
        }
    }

    fun stopTokenCollector() {
        if (::tokenTimerTask.isInitialized) {
            tokenTimerTask.cancel()
            tokenTimerTask.purge()
        }
    }

}
