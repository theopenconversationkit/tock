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
import fr.vsct.tock.bot.connector.messenger.model.attachment.AttachmentRequest
import fr.vsct.tock.bot.connector.messenger.model.send.ActionRequest
import fr.vsct.tock.bot.connector.messenger.model.send.CustomEventRequest
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.SendResponse
import fr.vsct.tock.bot.connector.messenger.model.send.SendResponseErrorContainer
import fr.vsct.tock.bot.engine.BotRepository.requestTimer
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.intProperty
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.lang.Exception

/**
 * Messenger client.
 */
internal class MessengerClient(val secretKey: String) {

    interface GraphApi {

        @POST("/v2.12/me/messages")
        fun sendMessage(@Query("access_token") accessToken: String, @Body messageRequest: MessageRequest): Call<SendResponse>

        @POST("/v2.12/me/messages")
        fun sendAction(@Query("access_token") accessToken: String, @Body actionRequest: ActionRequest): Call<SendResponse>

        @GET("/v2.12/{userId}/")
        fun getUserProfile(@Path("userId") userId: String, @Query("access_token") accessToken: String, @Query("fields") fields: String): Call<UserProfile>

        @POST("/{appId}/activities")
        fun sendCustomEvent(@Path("appId") appId: String, @Body customEventRequest: CustomEventRequest): Call<SendResponse>

        @POST("/v2.12/me/message_attachments")
        fun sendAttachment(@Query("access_token") accessToken: String, @Body attachmentRequest: AttachmentRequest): Call<SendResponse>

    }

    interface StatusApi {

        @GET("/platform/api-status")
        fun status(): Call<ResponseBody>
    }

    private val logger = KotlinLogging.logger {}
    private val graphApi: GraphApi
    private val statusApi: StatusApi
    private val nbRetriesLimit = intProperty("messenger_retries_on_error_limit", 1)
    private val nbRetriesWaitInMs = longProperty("messenger_retries_on_error_wait_in_ms", 5000)

    init {
        graphApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_messenger_request_timeout_ms", 30000),
            logger,
            requestGZipEncoding = booleanProperty("tock_messenger_request_gzip", true)
        )
            .baseUrl("https://graph.facebook.com")
            .addJacksonConverter()
            .build()
            .create()
        statusApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_messenger_request_timeout_ms", 5000),
            logger
        )
            .baseUrl("https://www.facebook.com")
            .build()
            .create()
    }

    fun healthcheck(): Boolean {
        return try {
            statusApi.status().execute().isSuccessful
        } catch (t: Throwable) {
            logger.error(t)
            false
        }
    }

    fun sendMessage(token: String, messageRequest: MessageRequest): SendResponse {
        return send(messageRequest, { graphApi.sendMessage(token, messageRequest).execute() })
    }

    fun sendAttachment(token: String, request: AttachmentRequest): SendResponse? {
        return graphApi.sendAttachment(token, request).execute().body()
    }

    fun sendAction(token: String, actionRequest: ActionRequest): SendResponse? {
        return try {
            send(actionRequest, { graphApi.sendAction(token, actionRequest).execute() })
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }

    fun sendCustomEvent(applicationId: String, customEventRequest: CustomEventRequest): SendResponse {
        return send(customEventRequest, { graphApi.sendCustomEvent(applicationId, customEventRequest).execute() })
    }

    private fun defaultUserProfile(): UserProfile {
        return UserProfile("", "", null, null, 0, null)
    }

    fun getUserProfile(token: String, recipient: Recipient): UserProfile {
        val requestTimerData = requestTimer.start("messenger_user_profile")
        return try {
            graphApi.getUserProfile(recipient.id!!, token, "first_name,last_name,profile_pic,locale,timezone,gender")
                .execute().body() ?: defaultUserProfile()
        } catch (e: Exception) {
            logger.logError(e, requestTimerData)
            defaultUserProfile()
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
                val errorBody = response.errorBody()?.string()
                logger.warn { "Messenger Error body : $errorBody" }

                if (request is MessageRequest && nbTries <= nbRetriesLimit && errorBody != null) {
                    val errorContainer: SendResponseErrorContainer = mapper.readValue(errorBody)
                    if (errorContainer.error != null) {
                        //cf https://developers.facebook.com/docs/messenger-platform/send-api-reference/errors
                        with(errorContainer.error) {
                            if (code == 1200 || code == 613 || (code == 200 && errorSubcode == 1545041)) {
                                logger.info { "Try to send again in $nbRetriesWaitInMs ms $request" }
                                Thread.sleep(nbRetriesWaitInMs)
                                return send(request, call, nbTries + 1)
                            }
                        }
                    }
                }
                throw ConnectorException(response.message())
            } else {
                return response.body() ?: throw ConnectorException("null body")
            }
        } catch (e: Exception) {
            logger.logError(e, requestTimerData)
            throw ConnectorException(e.message ?: "")
        } finally {
            requestTimer.end(requestTimerData)
        }
    }
}