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

package ai.tock.bot.connector.whatsapp.cloud.services

import ai.tock.bot.connector.ConnectorException
import ai.tock.bot.connector.whatsapp.cloud.WhatsAppCloudApiClient
import ai.tock.bot.connector.whatsapp.cloud.model.send.SendSuccessfulResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.ResponseCreateTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.WhatsAppCloudTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.FileType
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.MediaResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.*
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.Component
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.HeaderParameter
import ai.tock.bot.engine.BotRepository
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class WhatsAppCloudApiService(private val apiClient: WhatsAppCloudApiClient) {

    private val logger = KotlinLogging.logger {}

    fun sendMessage(phoneNumberId: String, token: String, messageRequest: WhatsAppCloudSendBotMessage) {
        try {
            when (messageRequest) {
                is WhatsAppCloudSendBotTextMessage, is WhatsAppCloudSendBotInteractiveMessage, is
                WhatsAppCloudSendBotLocationMessage -> {
                    send(messageRequest){apiClient.graphApi.sendMessage(phoneNumberId, token, messageRequest).execute()}
                }
                is WhatsAppCloudSendBotTemplateMessage -> {
                    replaceWithRealImageId(messageRequest, phoneNumberId, token)
                    send(messageRequest){apiClient.graphApi.sendMessage(phoneNumberId, token, messageRequest).execute()}
                }
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }


    fun sendMedia (phoneNumberId: String, token: String, fileUrl: String, fileType: String): MediaResponse {
        val requestTimerData = BotRepository.requestTimer.start("whatsapp_send_${fileUrl.javaClass.simpleName.lowercase()}")
        try {

            val file = uploadMedia(fileUrl, fileType)

            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file","fileimage",file)
                    .addFormDataPart("messaging_product","whatsapp")
                    .build()
            val request2 = Request.Builder()
                    .url("https://graph.facebook.com/v19.0/$phoneNumberId/media")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            val client = OkHttpClient()
            val response = client.newCall(request2).execute()

            if (response != null) {
                if(!response.isSuccessful){
                    throw ConnectorException("Failed to send message: ${response.code}")
                }

            }

            return mapper.readValue<MediaResponse>(response.body!!.string())

        } catch (e: Throwable) {
            BotRepository.requestTimer.throwable(e, requestTimerData)
            if (e is ConnectorException) {
                throw e
            } else {
                throwError(fileUrl, e.message ?: "")
            }
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
    }

    fun sendBuildTemplate(whatsAppBusinessAccountId: String, token: String, messageTemplate: WhatsAppCloudTemplate){
        sendTemplate(messageTemplate){apiClient.graphApi.createMessageTemplate(whatsAppBusinessAccountId, token, messageTemplate).execute()}
    }

    private fun <T: Any> sendTemplate (request: T, call: (T) -> Response<ResponseCreateTemplate>): ResponseCreateTemplate {
        val requestTimerData = BotRepository.requestTimer.start("whatsapp_send_${request.javaClass.simpleName.lowercase()}")
        try {
            val response = call(request)
            if(!response.isSuccessful){
                throw ConnectorException("Failed to send message: ${response.errorBody()?.string()}")
            }
            return response.body()?:throw  ConnectorException("Null response body")

        } catch (e: Throwable) {
            BotRepository.requestTimer.throwable(e, requestTimerData)
            if (e is ConnectorException) {
                throw e
            } else {
                throwError(request, e.message ?: "")
            }
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
    }

    private fun <T: Any> send (request: T, call: (T) -> Response<SendSuccessfulResponse>): SendSuccessfulResponse {
        val requestTimerData = BotRepository.requestTimer.start("whatsapp_send_${request.javaClass.simpleName.lowercase()}")
        try {
            val response = call(request)
            if(!response.isSuccessful){
                throw ConnectorException("Failed to send message: ${response.errorBody()?.string()}")
            }
            return response.body()?:throw  ConnectorException("Null response body")

        } catch (e: Throwable) {
            BotRepository.requestTimer.throwable(e, requestTimerData)
            if (e is ConnectorException) {
                throw e
            } else {
                throwError(request, e.message ?: "")
            }
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
    }

    private fun replaceWithRealImageId(messageRequest: WhatsAppCloudSendBotTemplateMessage, phoneNumberId: String, token: String) {
        messageRequest.template.components.forEach { component ->
            if (component is Component.Carousel) {
                component.cards.forEach { card ->
                    card.components
                            .filterIsInstance<Component.Header>()
                            .flatMap { it.parameters }
                            .filterIsInstance<HeaderParameter.Image>()
                            .forEach { imageHeader ->
                                imageHeader.image.id?.let { imageId ->
                                    val newImageId = sendMedia(phoneNumberId, token, imageId, FileType.PNG.type).id
                                    imageHeader.image.id = newImageId
                                }
                            }
                }
            }
        }
    }

    private fun uploadMedia(fileUrl: String, fileType: String) : RequestBody {

        val client = OkHttpClient()
        val request = Request.Builder().url(fileUrl).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to download file: $fileUrl")

            val inputStream: InputStream = response.body!!.byteStream()
            return createMultipart(inputStream, fileType)
        }

    }

    private fun createMultipart(stream: InputStream, fileType: String): RequestBody {
        val dump = ByteArrayOutputStream().apply {
            var nRead: Int = 0
            val data = ByteArray(2048)
            while (nRead != -1) {
                nRead = stream.read(data, 0, data.size)
                if (nRead != -1)
                    write(data, 0, Math.min(nRead, data.size))
            }
            flush()
        }

        return dump.toByteArray().toRequestBody(fileType.toMediaTypeOrNull())

    }

    private fun <T> throwError(request: T, errorMessage: String): Nothing {
        //warnRequest(request) { mapper.writeValueAsString(request) }
        throw ConnectorException(errorMessage)
    }
}