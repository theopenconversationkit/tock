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
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.MediaResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.*
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.Component
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.HeaderParameter
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.PayloadParameter
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotActionButton
import ai.tock.bot.engine.BotRepository
import ai.tock.shared.TockProxyAuthenticator
import ai.tock.shared.error
import ai.tock.shared.jackson.mapper
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.IOException
import java.time.Instant
import java.util.*

class WhatsAppCloudApiService(private val apiClient: WhatsAppCloudApiClient) {

    private val logger = KotlinLogging.logger {}
    private val payloadWhatsApp: PayloadWhatsAppCloudDAO = PayloadWhatsAppCloudMongoDAO

    fun sendMessage(phoneNumberId: String, token: String, messageRequest: WhatsAppCloudSendBotMessage) {
        try {
            when (messageRequest) {
                is WhatsAppCloudSendBotTextMessage,
                is WhatsAppCloudSendBotLocationMessage -> handleSimpleMessage(phoneNumberId, token, messageRequest)

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
        messageRequest.interactive.action?.buttons?.let { buttons ->
            val updateButtons = buttons.map { button ->
                updateButton(button)
            }
            sendUpdatedInteractiveMessage(phoneNumberId, token, messageRequest, updateButtons)
        }
    }

    private fun updateButton(button: WhatsAppCloudBotActionButton): WhatsAppCloudBotActionButton =
        if (button.reply.id.length >= 256) {
            val uuidPayload = UUID.randomUUID().toString()
            payloadWhatsApp.save(
                PayloadWhatsAppCloud(
                    uuidPayload,
                    button.reply.id,
                    Date.from(Instant.now()),
                )
            )
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
            if (component is Component.Carousel) {
                updateCarouselPayloads(component)
            } else {
                component
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

    fun updateCarouselPayloads(carousel: Component.Carousel): Component.Carousel {
        val updatedCards = carousel.cards.map { card ->
            val updatedComponents = card.components.map { component ->
                if (component is Component.Button) {
                    val updatedParameters = component.parameters.map { param ->
                        if ((param.payload?.length ?: 0) > 128) {
                            val newPayload = UUID.randomUUID().toString()
                            payloadWhatsApp.save(
                                PayloadWhatsAppCloud(
                                    newPayload,
                                    param.payload!!,
                                    Date.from(Instant.now())
                                )
                            )
                            updatePayloadParameter(param, newPayload)
                        } else {
                            param
                        }
                    }
                    component.copy(parameters = updatedParameters)
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
                payloadWhatsApp.save(PayloadWhatsAppCloud(uuidPayload, it, Date.from(Instant.now())))
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
        try {

            val file = uploadMedia(client, fileUrl, fileType)

            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "fileimage", file)
                .addFormDataPart("messaging_product", "whatsapp")
                .build()
            val request = Request.Builder()
                .url("https://graph.facebook.com/v19.0/$phoneNumberId/media")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ConnectorException("Failed to send message: ${response.code}")
                }
                return mapper.readValue(response.body!!.string(), MediaResponse::class.java)
            }
        } catch (e: Exception) {
            BotRepository.requestTimer.throwable(e, requestTimerData)
            throw if (e is ConnectorException) e else ConnectorException("Error sending media: ${e.message}")
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
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
                throwError(request, e.message ?: "")
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
                throwError(request, e.message ?: "")
            }
        } finally {
            BotRepository.requestTimer.end(requestTimerData)
        }
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
            .forEach { imageHeader ->
                imageHeader.image.id?.let { imageId ->
                    val newImageId = sendMedia(client, phoneNumberId, token, imageId, FileType.PNG.type).id
                    imageHeader.image.id = newImageId
                }
            }
    }

    private fun uploadMedia(client: OkHttpClient, fileUrl: String, fileType: String): RequestBody {

        val request = Request.Builder()
            .url(fileUrl).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to download file: $fileUrl")
            val mediaType = fileType.toMediaTypeOrNull()
            return response.body!!.byteStream().readBytes().toRequestBody(mediaType)
        }

    }

    private fun <T> throwError(request: T, errorMessage: String): Nothing {
        //warnRequest(request) { mapper.writeValueAsString(request) }
        throw ConnectorException(errorMessage)
    }
}