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

package fr.vsct.tock.bot.connector.messenger

import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.ConnectorException
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.UserProfile
import fr.vsct.tock.bot.connector.messenger.model.send.ActionRequest
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.SendResponse
import fr.vsct.tock.bot.connector.messenger.model.send.SendResponseErrorContainer
import fr.vsct.tock.bot.engine.BotRepository.requestTimer
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.intProperty
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.lang.Exception

/**
 *
 */
internal class MessengerClient(val secretKey: String) {

    interface GraphApi {

        @POST("/v2.6/me/messages")
        fun sendMessage(@Query("access_token") accessToken: String, @Body messageRequest: MessageRequest): Call<SendResponse>

        @POST("/v2.6/me/messages")
        fun activateTyping(@Query("access_token") accessToken: String, @Body actionRequest: ActionRequest): Call<SendResponse>

        @GET("/v2.6/{userId}/")
        fun getUserProfile(@Path("userId") userId: String, @Query("access_token") accessToken: String, @Query("fields") fields: String): Call<UserProfile>
    }

    private val logger = KotlinLogging.logger {}
    private val graphApi: MessengerClient.GraphApi
    private val nbRetriesLimit = intProperty("messenger_retries_on_error_limit", 1)
    private val nbRetriesWaitInMs = longProperty("messenger_retries_on_error_wait_in_ms", 5000)

    init {
        val retrofit = retrofitBuilderWithTimeoutAndLogger(
                longProperty("tock_messenger_request_timeout_ms", 30000), logger)
                .baseUrl("https://graph.facebook.com")
                .addJacksonConverter()
                .build()
        graphApi = retrofit.create()
    }

    fun sendMessage(token: String, messageRequest: MessageRequest): SendResponse {
        return send(messageRequest, { graphApi.sendMessage(token, messageRequest).execute() })
    }

    fun sendAction(token: String, actionRequest: ActionRequest) {
        try {
            send(actionRequest, { graphApi.activateTyping(token, actionRequest).execute() })
        } catch(e: Exception) {
            //log and ignore
            logger.error(e)
        }
    }

    fun getUserProfile(token: String, recipient: Recipient): UserProfile {
        val requestTimerData = requestTimer.start("messenger_user_profile")
        try {
            return graphApi.getUserProfile(recipient.id, token, "first_name,last_name,profile_pic,locale,timezone,gender")
                    .execute().body()
        } catch(e: Exception) {
            logger.logError(e, requestTimerData)
            return UserProfile("", "", null, null, 0, null)
        } finally {
            requestTimer.end(requestTimerData)
        }
    }

    private fun <T> send(request: T, call: (T) -> Response<SendResponse>): SendResponse {
        return send(request, call, 0)
    }

    private fun <T> send(request: T, call: (T) -> Response<SendResponse>, nbTries: Int): SendResponse {
        val requestTimerData = requestTimer.start("messenger_send")
        try {
            val response = call(request)

            if (!response.isSuccessful) {
                val error = response.message()
                val errorCode = response.code()
                logger.warn { "Messenger Error : $errorCode $error" }
                val errorBody = response.errorBody().string()
                logger.warn { "Messenger Error body : $errorBody" }

                if (request is MessageRequest && nbTries <= nbRetriesLimit) {
                    val errorContainer: SendResponseErrorContainer = mapper.readValue(errorBody)
                    if (errorContainer.error != null) {
                        //cf https://developers.facebook.com/docs/messenger-platform/send-api-reference/errors
                        with(errorContainer.error) {
                            if (code == 1200 || (code == 200 && errorSubcode == 1545041)) {
                                logger.info { "Try to send again in $nbRetriesWaitInMs ms $request" }
                                Thread.sleep(nbRetriesWaitInMs)
                                return send(request, call, nbTries + 1)
                            }
                        }
                    }
                }
                throw ConnectorException(response.message())
            } else {
                return response.body()
            }
        } catch(e: Exception) {
            logger.logError(e, requestTimerData)
            throw ConnectorException(e.message ?: "")
        } finally {
            requestTimer.end(requestTimerData)
        }
    }
}