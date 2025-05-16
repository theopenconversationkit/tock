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

package ai.tock.bot.connector.teams.token

import ai.tock.shared.Level
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import mu.KotlinLogging
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Timer
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

    private val logLevel = if (logger.isDebugEnabled) {
        Level.BODY
    } else {
        Level.BASIC
    }

    val teamsMapper: ObjectMapper = mapper.copy().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    @Volatile
    private lateinit var tokenTimerTask: Timer

    var loginApi: LoginMicrosoftOnline = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_microsoft_request_timeout", 30000),
        logger,
        logLevel
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

    fun launchTokenCollector(connectorId: String, msInterval: Long = 60 * 60 * 1000L) {
        try {
            checkToken()
            tokenTimerTask = fixedRateTimer(
                name = "microsoft-api-token-handling-$connectorId",
                initialDelay = 0L,
                period = msInterval
            ) {
                checkToken()
            }
        } catch (e: Exception) {
            logger.error(e)
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
