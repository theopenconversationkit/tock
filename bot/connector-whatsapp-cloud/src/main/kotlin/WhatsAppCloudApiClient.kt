/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.whatsapp.cloud.model.send.SendSuccessfulResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.ResponseCreateTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.WhatsAppCloudTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.Media
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.MediaResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.ResponseDeleteMedia
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.shared.*
import mu.KotlinLogging
import okhttp3.*
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Headers

private const val VERSION = "19.0"


class WhatsAppCloudApiClient(val token: String, val phoneNumber: String) {

    interface GraphApi {

        @POST("v$VERSION/{phoneNumberId}/messages")
        fun sendMessage(
            @Path("phoneNumberId") phoneNumberId: String,
            @Query("access_token") accessToken: String,
            @Body messageRequest: WhatsAppCloudSendBotMessage
        ): Call<SendSuccessfulResponse>

        @POST("v$VERSION/{phoneNumberId}/media")
        fun uploadMediaInWhatsAppAccount(
            @Path("phoneNumberId") phoneNumberId: String,
            @Header("Authorization") headerValue: String,
            @Body body: RequestBody
        ): Call<MediaResponse>


        @GET("v$VERSION/{media-id}")
        fun retrieveMediaUrl(
            @Path("media-id") mediaId: String?,
            @Query("access_token") accessToken: String
        ): Call<Media>

        @GET
        @Streaming
        @Headers(value = ["User-Agent:curl/7.64.1"])
        fun downloadMediaBinary(
            @Url url: String?,
            @Header("Authorization") authorization: String,
        ): Call<ResponseBody>

        @DELETE("v$VERSION/{media-id}")
        fun deleteMedia(
            @Path("media-id") mediaId: String?,
            @Query("access_token") accessToken: String
        ): Call<ResponseDeleteMedia?>?


        @POST("v$VERSION/{whatsAppBusinessAccountId}/message_templates")
        fun createMessageTemplate(
            @Path("whatsAppBusinessAccountId") whatsappBusinessAccountId: String?,
            @Query("access_token") accessToken: String,
            @Body messageTemplate: WhatsAppCloudTemplate?
        )
                : Call<ResponseCreateTemplate>

    }

    private val logger = KotlinLogging.logger {}
    val graphApi: GraphApi = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_whatsappcloud_request_timeout_ms", 30000),
        logger,
        requestGZipEncoding = booleanProperty("tock_whatsappcloud_request_gzip", false)
    )
        .baseUrl("https://graph.facebook.com")
        .addJacksonConverter()
        .build()
        .create()

}
