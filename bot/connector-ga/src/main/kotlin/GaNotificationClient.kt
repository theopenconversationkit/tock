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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ga.model.notification.GANotification
import ai.tock.bot.connector.ga.model.notification.GAPushNotification
import ai.tock.bot.connector.ga.model.notification.GATarget
import ai.tock.bot.connector.ga.model.notification.GaPushMessage
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.resourceAsStream
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import ai.tock.shared.tokenAuthenticationInterceptor
import com.google.auth.oauth2.ServiceAccountCredentials
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.Collections

/**
 * https://developers.google.com/actions/assistant/updates/notifications
 */
class GaNotificationClient {

    interface GaNotificationApi {
        @POST("./conversations:send")
        fun push(@Body notification: GAPushNotification): Call<ResponseBody>
    }

    private val logger = KotlinLogging.logger {}
    private val gaNotificationApi: GaNotificationApi
    private val version = property("tock_ga_notification_api_version", "2")
    private val baseUrl = property("tock_ga_notification_api_url", "https://actions.googleapis.com")

    init {
        gaNotificationApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_ga_notification_api_timeout", 30000),
            logger,
            interceptors = listOf(tokenAuthenticationInterceptor(getAccessToken()))
        )
            .baseUrl("$baseUrl/v$version/")
            .addJacksonConverter()
            .build()
            .create()
    }

    fun push(title: String, userId: String, intent: String, locale: String): Boolean {
        return try {
            gaNotificationApi.push(
                GAPushNotification(
                    GaPushMessage(
                        GANotification(title),
                        GATarget(
                            userId,
                            intent,
                            locale
                        )
                    )
                )
            ).execute().isSuccessful
        } catch (e: Exception) {
            logger.error(e)
            false
        }
    }

    private fun getAccessToken(): String {
        val token = loadCredentials().refreshAccessToken()
        return token.tokenValue
    }

    private fun loadCredentials(): ServiceAccountCredentials {
        val actionsApiServiceAccountFile = resourceAsStream("/service-account.json")
        val serviceAccountCredentials = ServiceAccountCredentials.fromStream(actionsApiServiceAccountFile)
        return serviceAccountCredentials.createScoped(
            Collections.singleton(
                "https://www.googleapis.com/auth/actions.fulfillment.conversation"
            )
        ) as ServiceAccountCredentials
    }
}
