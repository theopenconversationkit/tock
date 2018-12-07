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
import fr.vsct.tock.bot.connector.messenger.model.handover.PassThreadControlRequest
import fr.vsct.tock.bot.connector.messenger.model.handover.RequestThreadControlRequest
import fr.vsct.tock.bot.connector.messenger.model.handover.SecondaryReceiverData
import fr.vsct.tock.bot.connector.messenger.model.handover.SecondaryReceiverResponse
import fr.vsct.tock.bot.connector.messenger.model.handover.TakeThreadControlRequest
import fr.vsct.tock.bot.connector.messenger.model.handover.ThreadOwnerResponse
import fr.vsct.tock.bot.connector.messenger.model.send.ActionRequest
import fr.vsct.tock.bot.connector.messenger.model.send.CustomEventRequest
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.SendResponse
import fr.vsct.tock.bot.connector.messenger.model.send.SendResponseErrorContainer
import fr.vsct.tock.bot.connector.messenger.model.webhook.SubscriptionsResponse
import fr.vsct.tock.bot.connector.messenger.model.webhook.SuccessResponse
import fr.vsct.tock.bot.engine.BotRepository.requestTimer
import fr.vsct.tock.bot.engine.monitoring.logError
import fr.vsct.tock.shared.Level
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.intProperty
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import fr.vsct.tock.shared.warn
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

        @POST("/v3.1/me/messages")
        fun sendMessage(@Query("access_token") accessToken: String, @Body messageRequest: MessageRequest): Call<SendResponse>

        @POST("/v3.1/me/messages")
        fun sendAction(@Query("access_token") accessToken: String, @Body actionRequest: ActionRequest): Call<SendResponse>

        @GET("/v3.1/{userId}/")
        fun getUserProfile(@Path("userId") userId: String, @Query("access_token") accessToken: String, @Query("fields") fields: String): Call<UserProfile>

        @POST("/v3.1/me/message_attachments")
        fun sendAttachment(@Query("access_token") accessToken: String, @Body attachmentRequest: AttachmentRequest): Call<SendResponse>

        @POST("/v3.1/me/pass_thread_control")
        fun passThreadControl(@Query("access_token") accessToken: String, @Body request: PassThreadControlRequest): Call<SendResponse>

        @POST("/v3.1/me/take_thread_control")
        fun takeThreadControl(@Query("access_token") accessToken: String, @Body request: TakeThreadControlRequest): Call<SendResponse>

        @POST("/v3.1/me/request_thread_control")
        fun requestThreadControl(@Query("access_token") accessToken: String, @Body request: RequestThreadControlRequest): Call<SendResponse>

        @GET("/v3.1/me/secondary_receivers")
        fun secondaryReceivers(@Query("access_token") accessToken: String, @Query("fields") recipient: String = "id,name"): Call<SecondaryReceiverResponse>

        @GET("/v3.1/me/thread_owner")
        fun threadOwner(@Query("access_token") accessToken: String, @Query("recipient") recipient: String): Call<ThreadOwnerResponse>

        @POST("/{appId}/activities")
        fun sendCustomEvent(@Path("appId") appId: String, @Body customEventRequest: CustomEventRequest): Call<SendResponse>

        @GET("/v3.1/{appId}/subscriptions")
        fun getSubscriptions(@Path("appId") appId: String, @Query("access_token") appAccessToken: String): Call<SubscriptionsResponse>

        @POST("/v3.1/{appId}/subscriptions")
        fun subscriptions(
            @Path("appId") appId: String, @Query("object") obj: String,
            @Query("callback_url") callbackUrl: String, @Query("fields") fields: String,
            @Query("verify_token") verifyToken: String, @Query("access_token") appAccessToken: String
        ): Call<SuccessResponse>

        @DELETE("/v3.1/{pageId}/subscribed_apps")
        fun deleteSubscribedApps(
            @Path("pageId") pageId: String, @Query("subscribed_fields") subscribedFields: String,
            @Query("access_token") accessToken: String
        ): Call<SuccessResponse>

        @POST("/v3.1/{pageId}/subscribed_apps")
        fun subscribedApps(
            @Path("pageId") pageId: String, @Query("subscribed_fields") subscribedFields: String,
            @Query("access_token") accessToken: String
        ): Call<SuccessResponse>

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
            logger,
            Level.BASIC
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
        return send(messageRequest) { graphApi.sendMessage(token, messageRequest).execute() }
    }

    fun sendAttachment(token: String, request: AttachmentRequest): SendResponse? {
        return graphApi.sendAttachment(token, request).execute().body()
    }

    fun sendAction(token: String, actionRequest: ActionRequest): SendResponse? {
        return try {
            send(actionRequest) { graphApi.sendAction(token, actionRequest).execute() }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }

    fun sendCustomEvent(applicationId: String, customEventRequest: CustomEventRequest): SendResponse {
        return send(customEventRequest) { graphApi.sendCustomEvent(applicationId, customEventRequest).execute() }
    }

    fun requestThreadControl(token: String, request: RequestThreadControlRequest): SendResponse? {
        return try {
            send(request) { graphApi.requestThreadControl(token, request).execute() }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }

    fun takeThreadControl(token: String, request: TakeThreadControlRequest): SendResponse? {
        return try {
            send(request) { graphApi.takeThreadControl(token, request).execute() }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }

    fun passThreadControl(token: String, request: PassThreadControlRequest): SendResponse? {
        return try {
            send(request) { graphApi.passThreadControl(token, request).execute() }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }

    fun getThreadOwnerId(token: String, userId: String): String? =
        try {
            graphApi.threadOwner(token, userId).execute().body()?.data?.threadOwner?.appId
        } catch (e: Exception) {
            logger.warn(e)
            null
        }

    fun getSecondaryReceivers(token: String): List<SecondaryReceiverData>? =
        try {
            graphApi.secondaryReceivers(token).execute().body()?.data
        } catch (e: Exception) {
            logger.warn(e)
            null
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
            logger.warn { recipient }
            logger.logError(e, requestTimerData)
            defaultUserProfile()
        } finally {
            requestTimer.end(requestTimerData)
        }
    }

    private fun <T> send(request: T, call: (T) -> Response<SendResponse>): SendResponse {
        return send(request, call, 0)
    }

    private fun <T> throwError(request: T, errorMessage: String): Nothing {
        logger.warn { mapper.writeValueAsString(request) }
        throw ConnectorException(errorMessage)
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
                throwError(request, response.message())
            } else {
                return response.body() ?: throwError(request, "null body")
            }
        } catch (e: Exception) {
            logger.logError(e, requestTimerData)
            throwError(request, e.message ?: "")
        } finally {
            requestTimer.end(requestTimerData)
        }
    }

    fun getSubscriptions(appId: String, appToken: String): SubscriptionsResponse? {
        return try {
            graphApi.getSubscriptions(appId, appToken).execute().body()
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }

    fun subscriptions(
        appId: String,
        callbackUrl: String,
        fields: String,
        verifyToken: String,
        appToken: String
    ): SuccessResponse? {
        return try {
            if (callbackUrl == "") {
                throw ConnectorException("No callback URL found to subscribe webhook")
            }
            graphApi.subscriptions(appId, "page", callbackUrl, fields, verifyToken, appToken).execute().body()
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }

    fun deleteSubscribedApps(pageId: String, fields: String, token: String): SuccessResponse? {
        return try {
            graphApi.deleteSubscribedApps(pageId, fields, token).execute().body()
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }

    fun subscribedApps(pageId: String, fields: String, token: String): SuccessResponse? {
        return try {
            graphApi.subscribedApps(pageId, fields, token).execute().body()
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }
    }
}