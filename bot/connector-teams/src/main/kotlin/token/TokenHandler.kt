package fr.vsct.tock.bot.connector.teams.token

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import fr.vsct.tock.bot.engine.nlp.NlpProxyBotListener.logger
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.concurrent.fixedRateTimer

/**
 * Handle the generation and the refresh of the token header
 * This token is mandatory in request from bot to teams via microsoft-api
 */
object TokenHandler {

    @Volatile
    var token: String? = null
    @Volatile
    private var tokenExpiration: Instant? = null

    private lateinit var appId: String
    private lateinit var password: String

    val teamsMapper: ObjectMapper = mapper.copy().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)


    var loginApi: LoginMicrosoftOnline = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_whatsapp_request_timeout_ms", 30000),
        logger
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

    fun takeCareOfTheToken(appId: String, password: String) {
        setId(appId, password)
        fixedRateTimer(name = "microsoft-api-token-handling", initialDelay = 0.toLong(), period = 60000.toLong()) {
            checkToken()
        }
    }

}
