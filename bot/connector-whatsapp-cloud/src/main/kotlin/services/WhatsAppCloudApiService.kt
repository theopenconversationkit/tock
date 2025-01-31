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
import ai.tock.bot.connector.whatsapp.cloud.database.model.PayloadWhatsAppCloud
import ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudDAO
import ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudMongoDAO
import ai.tock.bot.connector.whatsapp.cloud.model.send.SendSuccessfulResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.ResponseCreateTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.WhatsAppCloudTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.FileType
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.Media
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.MediaResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotTemplateMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotInteractiveMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotImageMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotLocationMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotTextMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.Component
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.HeaderParameter
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.PayloadParameter
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotActionButton
import ai.tock.bot.engine.BotRepository
import ai.tock.shared.Executor
import ai.tock.shared.TockProxyAuthenticator
import ai.tock.shared.cache.getOrCache
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.litote.kmongo.toId
import retrofit2.Response
import java.time.Instant
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class WhatsAppCloudApiService(private val apiClient: WhatsAppCloudApiClient) {

    private val logger = KotlinLogging.logger {}
    private val payloadWhatsApp: PayloadWhatsAppCloudDAO = PayloadWhatsAppCloudMongoDAO
    private val executor: Executor = injector.provide()
    private val executorService = Executors.newCachedThreadPool()

    fun sendMessage(phoneNumberId: String, token: String, messageRequest: WhatsAppCloudSendBotMessage) {
        try {
            when (messageRequest) {
                is WhatsAppCloudSendBotTextMessage,

                is WhatsAppCloudSendBotLocationMessage -> handleSimpleMessage(phoneNumberId, token, messageRequest)

                is WhatsAppCloudSendBotImageMessage -> handleImageMessage(phoneNumberId, token, messageRequest)

                is WhatsAppCloudSendBotInteractiveMessage -> handleInteractiveMessage(
                    phoneNumberId,
                    token,
                    messageRequest
                )

                is WhatsAppCloudSendBotTemplateMessage -> handleTemplateMessage(phoneNumberId, token, messageRequest)
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    private fun handleImageMessage(
        phoneNumberId: String,
        token: String,
        messageRequest: WhatsAppCloudSendBotImageMessage
    ) {
        replaceWithRealMessageImageId(messageRequest, phoneNumberId, token)
        send(messageRequest) {
            apiClient.graphApi.sendMessage(phoneNumberId, token, messageRequest).execute()
        }
    }

    fun downloadImgByBinary(token: String, imgId: String, mimeType: String): String {

        val url = getMedia(imgId) {
            apiClient.graphApi.retrieveMediaUrl(imgId, token).execute()
        }.url

        val base64Response = responseDownloadMedia(url, mimeType) {
            apiClient.graphApi.downloadMediaBinary(url, "Bearer $token").execute()
        }
        return base64Response
    }

    private fun handleSimpleMessage(phoneNumberId: String, token: String, messageRequest: WhatsAppCloudSendBotMessage) {
        send(messageRequest) {
            apiClient.graphApi.sendMessage(phoneNumberId, token, messageRequest).execute()
        }
    }

    private fun handleInteractiveMessage(
        phoneNumberId: String,
        token: String,
        messageRequest: WhatsAppCloudSendBotInteractiveMessage
    ) {
        messageRequest.interactive.action?.let { action ->
            if (!action.buttons.isNullOrEmpty()) {
                val updateButtons = action.buttons.map { button ->
                    updateButton(button)
                }
                sendUpdatedInteractiveMessage(phoneNumberId, token, messageRequest, updateButtons)
            } else {
                send(messageRequest) {
                    apiClient.graphApi.sendMessage(
                        phoneNumberId,
                        token,
                        messageRequest
                    ).execute()
                }
            }
        }
    }

    private fun updateButton(button: WhatsAppCloudBotActionButton): WhatsAppCloudBotActionButton =
        if (button.reply.id.length >= 256) {
            val uuidPayload = UUID.randomUUID().toString()
            executor.executeBlocking {
                payloadWhatsApp.save(
                    PayloadWhatsAppCloud(
                        uuidPayload,
                        button.reply.id,
                        Date.from(Instant.now()),
                    )
                )
            }
            val copyReply = button.reply.copy(title = button.reply.title, id = uuidPayload)
            button.copy(reply = copyReply)
        } else {
            button
        }

    private fun sendUpdatedInteractiveMessage(
        phoneNumberId: String,
        token: String,
        messageRequest: WhatsAppCloudSendBotInteractiveMessage,
        updatedButtons: List<WhatsAppCloudBotActionButton>
    ) {
        val updateAction = messageRequest.interactive.action?.copy(buttons = updatedButtons)
        val updateMessageRequest = messageRequest.copy(
            interactive = messageRequest.interactive.copy(action = updateAction)
        )
        send(updateMessageRequest) {
            apiClient.graphApi.sendMessage(
                phoneNumberId,
                token,
                updateMessageRequest
            ).execute()
        }
    }

    private fun handleTemplateMessage(
        phoneNumberId: String,
        token: String,
        messageRequest: WhatsAppCloudSendBotTemplateMessage
    ) {
        val updatedComponents = messageRequest.template.components.map { component ->
            when (component) {
                is Component.Carousel -> updateCarouselPayloads(component)
                is Component.Button -> updateTemplateButton(component)
                else -> component
            }
        }

        val updatedMessageRequest = messageRequest.copy(
            template = messageRequest.template.copy(
                components = updatedComponents
            )
        )

        sendUpdatedTemplateMessage(phoneNumberId, token, updatedMessageRequest)
    }

    fun updatePayloadParameter(payloadParameter: PayloadParameter, newPayload: String?): PayloadParameter {
        return payloadParameter.copy(type = payloadParameter.type, payload = newPayload, text = payloadParameter.text)
    }

    private fun updateCarouselPayloads(carousel: Component.Carousel): Component.Carousel {
        val updatedCards = carousel.cards.map { card ->
            val updatedComponents = card.components.map { component ->
                if (component is Component.Button) {
                    updateTemplateButton(component)
                } else {
                    component
                }
            }
            card.copy(components = updatedComponents)
        }
        return carousel.copy(cards = updatedCards)
    }

    private fun updateTemplateButton(button: Component.Button): Component.Button =
        button.copy(parameters = button.parameters.map { parameters ->
            parameters.payload?.takeIf { it.length >= 128 }?.let {
                val uuidPayload = UUID.randomUUID().toString()
                executor.executeBlocking {
                    payloadWhatsApp.save(PayloadWhatsAppCloud(uuidPayload, it, Date.from(Instant.now())))
                }
                parameters.copy(payload = uuidPayload)
            } ?: parameters
        })

    private fun sendUpdatedTemplateMessage(
        phoneNumberId: String,
        token: String,
        messageRequest: WhatsAppCloudSendBotTemplateMessage,
    ) {

        replaceWithRealImageId(messageRequest, phoneNumberId, token)
        send(messageRequest) {
            apiClient.graphApi.sendMessage(phoneNumberId, token, messageRequest).execute()
        }
    }

    private fun sendMedia(
        client: OkHttpClient,
        phoneNumberId: String,
        token: String,
        fileUrl: String,
        fileType: String
    ): MediaResponse {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_${fileUrl.javaClass.simpleName.lowercase()}")

        return getOrCache("$phoneNumberId-$fileUrl".toId(), IMAGE_ID_CACHE) {
            try {
                val file = retrieveMedia(client, fileUrl, fileType)

                val media = apiClient.graphApi.uploadMediaInWhatsAppAccount(
                    phoneNumberId,
                    "Bearer $token",
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", "fileimage", file)
                        .addFormDataPart("messaging_product", "whatsapp")
                        .build()
                )

                media.execute().body()
            } catch (e: Exception) {
                BotRepository.requestTimer.throwable(e, requestTimerData)
                throw if (e is ConnectorException) e else ConnectorException("Error sending media: ${e.message}")
            } finally {
                BotRepository.requestTimer.end(requestTimerData)
            }
        } ?: throw ConnectorException("Error sending media")
    }

    fun sendBuildTemplate(whatsAppBusinessAccountId: String, token: String, messageTemplate: WhatsAppCloudTemplate) {
        sendTemplate(messageTemplate) {
            apiClient.graphApi.createMessageTemplate(
                whatsAppBusinessAccountId,
                token,
                messageTemplate
            ).execute()
        }
    }

    private fun <T : Any> sendTemplate(
        request: T,
        call: (T) -> Response<ResponseCreateTemplate>
    ): ResponseCreateTemplate {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_${request.javaClass.simpleName.lowercase()}")
        try {
            val response = call(request)
            if (!response.isSuccessful) {
                throw ConnectorException("Failed to send message: ${response.errorBody()?.string()}")
            }
            return response.body() ?: throw ConnectorException("Null response body")

        } catch (e: Throwable) {
            BotRepository.requestTimer.throwable(e, requestTimerData)
            if (e is ConnectorException) {
                throw e
            } else {
                throwError(e.message ?: "")
            }
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
    }

    private fun <T : Any> send(request: T, call: (T) -> Response<SendSuccessfulResponse>): SendSuccessfulResponse {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_${request.javaClass.simpleName.lowercase()}")
        try {
            val response = call(request)
            if (!response.isSuccessful) {
                throw ConnectorException("Failed to send message: ${response.errorBody()?.string()}")
            }
            return response.body() ?: throw ConnectorException("Null response body")

        } catch (e: Throwable) {
            BotRepository.requestTimer.throwable(e, requestTimerData)
            if (e is ConnectorException) {
                throw e
            } else {
                throwError(e.message ?: "")
            }
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
    }

    private fun <T : Any> getMedia(request: T, call: (T) -> Response<Media>): Media {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_${request.javaClass.simpleName.lowercase()}")
        try {
            val response = call(request)
            if (!response.isSuccessful) {
                throw ConnectorException("Failed to send message: ${response.errorBody()?.string()}")
            }
            return response.body() ?: throw ConnectorException("Null response body")

        } catch (e: Throwable) {
            BotRepository.requestTimer.throwable(e, requestTimerData)
            if (e is ConnectorException) {
                throw e
            } else {
                throwError(e.message ?: "")
            }
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
    }

    private fun <T : Any> responseDownloadMedia(
        request: T,
        mimeType: String,
        call: (T) -> Response<ResponseBody>
    ): String {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_${request.javaClass.simpleName.lowercase()}")

        try {
            val response = call(request)
            if (response.isSuccessful && response.body() != null) {
                val byteArray = response.body()!!.bytes()
                val base64String = Base64.getEncoder().encodeToString(byteArray)

                return "data:$mimeType;base64,$base64String"
            } else {
                throw ConnectorException("Failed to download media: ${response.errorBody()?.string()}")
            }
        } catch (e: Throwable) {
            BotRepository.requestTimer.throwable(e, requestTimerData)
            throw e
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
    }

    private fun replaceWithRealMessageImageId(
        messageRequest: WhatsAppCloudSendBotImageMessage,
        phoneNumberId: String,
        token: String
    ) {
        val client = OkHttpClient.Builder().apply(TockProxyAuthenticator::install).build()

        var res = sendMedia(
            client,
            phoneNumberId,
            token,
            messageRequest.image.id,
            FileType.PNG.type
        )

        val image = messageRequest.image
        val newImageId = res.id
        image.id = newImageId
    }


    private fun replaceWithRealImageId(
        messageRequest: WhatsAppCloudSendBotTemplateMessage,
        phoneNumberId: String,
        token: String
    ) {
        val client = OkHttpClient.Builder().apply(TockProxyAuthenticator::install).build()
        messageRequest.template.components
            .asSequence()
            .filterIsInstance<Component.Carousel>()
            .flatMap { it.cards }
            .flatMap { it.components }
            .filterIsInstance<Component.Header>()
            .flatMap { it.parameters }
            .filterIsInstance<HeaderParameter.Image>()
            .filter { it.image.id != null }
            .map {
                it to executorService.submit(Callable {
                    sendMedia(
                        client,
                        phoneNumberId,
                        token,
                        it.image.id!!,
                        FileType.PNG.type
                    )
                })
            }
            //exit from sequence
            .toList()
            .forEach {
                val imageHeader = it.first
                val newImageId = it.second.get().id
                imageHeader.image.id = newImageId
            }
    }

    private fun retrieveMedia(client: OkHttpClient, fileUrl: String, fileType: String): RequestBody {

        val request = Request.Builder()
            .url(fileUrl).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Failed to download file: $fileUrl - ${response.message}")
            val mediaType = fileType.toMediaTypeOrNull()
            return response.body?.byteStream()?.readBytes()?.toRequestBody(mediaType)
                ?: error("no response message for: $fileUrl")
        }

    }

    private fun throwError(errorMessage: String): Nothing {
        throw ConnectorException(errorMessage)
    }
}

private const val IMAGE_ID_CACHE = "whatsapp_image_id_cache"
