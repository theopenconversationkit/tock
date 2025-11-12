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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.whatsapp.cloud.model.send.SendSuccessfulResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.SendTypingIndicatorSuccessfulResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.Media
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.MediaResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.ResponseDeleteMedia
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudTypingIndicatorMessage
import ai.tock.bot.connector.whatsapp.cloud.model.template.WhatsappTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.template.management.CreateTemplateResponse
import ai.tock.bot.connector.whatsapp.cloud.model.template.management.GetTemplatesResponse
import ai.tock.bot.connector.whatsapp.cloud.model.template.management.StartUploadAssetResponse
import ai.tock.bot.connector.whatsapp.cloud.model.template.management.UpdateTemplateResponse
import ai.tock.bot.connector.whatsapp.cloud.model.template.management.UploadAssetResponse
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.booleanProperty
import ai.tock.shared.create
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

private const val WHATSAPP_API_BASE_URL = "https://graph.facebook.com"
private const val VERSION = "22.0"
private const val WHATSAPP_API_URL = "$WHATSAPP_API_BASE_URL/v$VERSION"

class WhatsAppCloudApiClient(private val token: String, val businessAccountId: String, val phoneNumberId: String) {

    interface GraphApi {

        @POST("v$VERSION/{phoneNumberId}/messages")
        fun sendMessage(
            @Path("phoneNumberId") phoneNumberId: String,
            @Query("access_token") accessToken: String,
            @Body messageRequest: WhatsAppCloudSendBotMessage
        ): Call<SendSuccessfulResponse>

        @POST("v$VERSION/{phoneNumberId}/messages")
        fun sendMessage(
            @Path("phoneNumberId") phoneNumberId: String,
            @Query("access_token") accessToken: String,
            @Body messageRequest: WhatsAppCloudTypingIndicatorMessage
        ): Call<SendTypingIndicatorSuccessfulResponse>

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

        @POST("v$VERSION/{metaApplicationId}/uploads")
        fun startUpload(
            @Path("metaApplicationId") metaApplicationId: String,
            @Query("file_length") fileLength: Long,
            @Query("file_type") fileType: String,
            @Header("Authorization") authorization: String,
        ): Call<StartUploadAssetResponse>

        @POST("v$VERSION/{whatsAppBusinessAccountId}/message_templates")
        fun createMessageTemplate(
            @Path("whatsAppBusinessAccountId") whatsappBusinessAccountId: String?,
            @Query("access_token") accessToken: String,
            @Body messageTemplate: WhatsappTemplate?
        ) : Call<CreateTemplateResponse>

        @DELETE("v$VERSION/{whatsAppBusinessAccountId}/message_templates")
        fun deleteMessageTemplate(
            @Path("whatsAppBusinessAccountId") whatsappBusinessAccountId: String?,
            @Query("access_token") accessToken: String,
            @Query("name") templateName: String,
        ) : Call<UpdateTemplateResponse>

        @GET("v$VERSION/{whatsAppBusinessAccountId}/message_templates")
        fun getMessageTemplates(
            @Path("whatsAppBusinessAccountId") whatsappBusinessAccountId: String?,
            @Query("access_token") accessToken: String,
            @Query("name") templateName: String,
        ) : Call<GetTemplatesResponse>

        @POST("v$VERSION/{templateId}")
        fun editMessageTemplate(
            @Query("access_token") accessToken: String,
            @Path("templateId") templateId: String,
            @Body updatedTemplate: WhatsappTemplate,
        ) : Call<UpdateTemplateResponse>

    }

    private val logger = KotlinLogging.logger {}
    private val graphApi: GraphApi = retrofitBuilderWithTimeoutAndLogger(
        longProperty("tock_whatsappcloud_request_timeout_ms", 30000),
        logger,
        requestGZipEncoding = booleanProperty("tock_whatsappcloud_request_gzip", false)
    )
        .baseUrl(WHATSAPP_API_BASE_URL)
        .addJacksonConverter()
        .build()
        .create()

    fun uploadMediaInWhatsAppAccount(file: RequestBody) = graphApi.uploadMediaInWhatsAppAccount(
        phoneNumberId,
        "Bearer $token",
        MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", "fileimage", file)
            .addFormDataPart("messaging_product", "whatsapp")
            .build()
    )
    fun retrieveMediaUrl(imgId: String) = graphApi.retrieveMediaUrl(imgId, token)
    fun downloadMediaBinary(url: String) = graphApi.downloadMediaBinary(url, "Bearer $token")
    fun sendMessage(phoneNumberId: String, messageRequest: WhatsAppCloudSendBotMessage) = graphApi.sendMessage(phoneNumberId, token, messageRequest)
    fun sendMessage(phoneNumberId: String, messageRequest: WhatsAppCloudTypingIndicatorMessage) = graphApi.sendMessage(phoneNumberId, token, messageRequest)
    fun createMessageTemplate(template: WhatsappTemplate) = graphApi.createMessageTemplate(businessAccountId, token, template)
    fun deleteMessageTemplate(templateName: String) = graphApi.deleteMessageTemplate(businessAccountId, token, templateName)
    fun editMessageTemplate(templateId: String, updatedTemplate: WhatsappTemplate) = graphApi.editMessageTemplate(token, templateId, updatedTemplate)
    fun getMessageTemplates(templateName: String) = graphApi.getMessageTemplates(businessAccountId, token, templateName)

    fun startFileUpload(metaApplicationId: String, fileLength: Long, fileType: String) = graphApi.startUpload(
        metaApplicationId,
        fileLength,
        fileType,
        authorization = "Bearer $token",
    )

    fun uploadFile(client: OkHttpClient, uploadId: String, fileContents: ByteArray): String {
        val url = "$WHATSAPP_API_URL/$uploadId"
        val requestBody = fileContents.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("file_offset", "0")
            .addHeader("Authorization", "OAuth $token")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            return mapper.readValue<UploadAssetResponse>(response.body?.string() ?: error("empty body")).handle
        }
    }
}
