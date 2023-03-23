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

package ai.tock.bot.connector.whatsapp

import ai.tock.bot.connector.whatsapp.model.send.WhatsAppResponse
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppSendBotImageMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppSendBotInteractiveMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppSendBotMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppSendBotMessageInteractiveMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppSendBotTextMessage
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.basicAuthInterceptor
import ai.tock.shared.create
import ai.tock.shared.error
import ai.tock.shared.jackson.addDeserializer
import ai.tock.shared.jackson.mapper
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer
import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 *
 */
internal class WhatsAppClient(
    whatsAppUrl: String,
    login: String,
    password: String
) {

    data class LoginResponse(val users: List<LoginUser> = emptyList())
    data class LoginUser(
        val token: String,
        @get:JsonProperty("expires_after") val expiresAfter: OffsetDateTime
    )

    data class MediaResponse(val media: List<MediaId> = emptyList())
    data class MediaId(val id: String)

    private interface WhatsAppLoginApi {

        @Headers("Content-Type: application/json")
        @POST("v1/users/login")
        fun login(): Call<LoginResponse>
    }

    private interface WhatsAppApi {

        @Headers("Content-Type: application/json")
        @POST("v1/messages")
        fun sendMessage(@Body message: WhatsAppSendBotMessage): Call<WhatsAppResponse>

        @GET("v1/media/{mediaId}")
        fun getMedia(@Path("mediaId") mediaId: String): Call<ResponseBody>

        @DELETE("v1/media/{mediaId}")
        fun deleteMedia(@Path("mediaId") mediaId: String): Call<ResponseBody>

        @POST("v1/media")
        fun sendMedia(
            @Header("Content-Type") contentType: String,
            @Body body: RequestBody
        ): Call<MediaResponse>
    }

    private val logger = KotlinLogging.logger {}
    private val loginApi: WhatsAppLoginApi
    private val api: WhatsAppApi

    @Volatile
    private var tokenExpiration: OffsetDateTime? = null

    @Volatile
    private var token: String? = null

    val clientMapper = mapper.copy().registerModule(
        SimpleModule()
            .addDeserializer(
                OffsetDateTime::class,
                object : InstantDeserializer<OffsetDateTime>(
                    OffsetDateTime::class.java,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"),
                    { OffsetDateTime.from(it) },
                    { a -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId) },
                    { a ->
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(a.integer, a.fraction.toLong()),
                            a.zoneId
                        )
                    },
                    { d, z -> d.withOffsetSameInstant(z.rules.getOffset(d.toLocalDateTime())) },
                    false
                ) {
                }
            )
    )

    init {
        loginApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_whatsapp_request_timeout_ms", 30000),
            logger,
            interceptors = listOf(basicAuthInterceptor(login, password))
        )
            .baseUrl(whatsAppUrl)
            .addJacksonConverter(clientMapper)
            .build()
            .create()

        api = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_whatsapp_request_timeout_ms", 30000),
            logger,
            interceptors = listOf(tokenInterceptor())
        )
            .baseUrl(whatsAppUrl)
            .addJacksonConverter(clientMapper)
            .build()
            .create()
    }

    /**
     * Create a Bearer token interceptor.
     */
    private fun tokenInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()

            val requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer $token")

            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    private fun checkLogin(): Boolean {
        return if (token == null || tokenExpiration?.isBefore(OffsetDateTime.now().plusHours(1)) != false) {
            login()
        } else {
            true
        }
    }

    private fun Response<*>.logError() {
        val error = message()
        val errorCode = code()
        logger.warn { "WhatsApp Error : $errorCode $error" }
        val errorBody = errorBody()?.string()
        logger.warn { "Messenger Error body : $errorBody" }
    }

    fun getMedia(id: String): ByteArray? {
        return if (checkLogin()) {
            api.getMedia(id).execute().run {
                body()?.bytes() ?: null.also { logError() }
            }
        } else {
            null
        }
    }

    fun sendMessage(message: WhatsAppSendBotMessage) {
        if (checkLogin()) {
            try {
                when (message) {
                    is WhatsAppSendBotTextMessage, is WhatsAppSendBotInteractiveMessage, is WhatsAppSendBotMessageInteractiveMessage -> {
                        val response = api.sendMessage(message).execute()
                        if (!response.isSuccessful) {
                            response.logError()
                        }
                    }
                    is WhatsAppSendBotImageMessage -> {
                        val response = api.sendMedia(
                            message.image.contentType,
                            message.image.byteImages!!.toRequestBody(
                                message.image.contentType.toMediaType()
                            )
                        ).execute()
                        val id = response.body()?.media?.firstOrNull()?.id
                        if (id == null) {
                            response.logError()
                        } else {
                            message.image.id = id
                            val response2 = api.sendMessage(message).execute()
                            if (!response2.isSuccessful) {
                                response2.logError()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    fun login(): Boolean {
        return try {
            val response = loginApi.login().execute()
            if (response.isSuccessful) {
                response.body()?.users?.firstOrNull()?.let {
                    token = it.token
                    tokenExpiration = it.expiresAfter
                    true
                } ?: false
            } else {
                response.logError()
                false
            }
        } catch (e: Exception) {
            logger.error(e)
            false
        }
    }
}
