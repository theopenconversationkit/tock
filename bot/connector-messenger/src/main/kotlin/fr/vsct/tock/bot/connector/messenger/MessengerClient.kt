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

import fr.vsct.tock.bot.connector.ConnectorException
import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.UserProfile
import fr.vsct.tock.bot.connector.messenger.model.send.ActionRequest
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.SendResponse
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.retrofitBuilderWithTimeout
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

    init {
        val retrofit = retrofitBuilderWithTimeout(30)
                .baseUrl("https://graph.facebook.com")
                .addJacksonConverter()
                .build()
        graphApi = retrofit.create()
    }

    fun sendMessage(token: String, messageRequest: MessageRequest): SendResponse {
        return send(messageRequest, { graphApi.sendMessage(token, messageRequest).execute() })
    }

    fun sendAction(token: String, actionRequest: ActionRequest): SendResponse {
        return send(actionRequest, { graphApi.activateTyping(token, actionRequest).execute() })
    }

    fun getUserProfile(token: String, recipient: Recipient): UserProfile {
        try {
            return graphApi.getUserProfile(recipient.id, token, "first_name,last_name,profile_pic,locale,timezone,gender")
                    .execute().body()
        } catch(e: Exception) {
            logger.error(e)
            return UserProfile("", "", null, null, 0, null)
        }
    }

    private fun <T> send(request: T, call: (T) -> Response<SendResponse>): SendResponse {
        try {
            val response = call(request)

            if (!response.isSuccessful) {
                throw ConnectorException(response.message())
            } else {
                return response.body()
            }
        } catch(e: Exception) {
            logger.error(e)
            throw ConnectorException(e.message ?: "")
        }
    }
}