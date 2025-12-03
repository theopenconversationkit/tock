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

package ai.tock.bot.connector.whatsapp.cloud.services

import ai.tock.bot.connector.ConnectorException
import ai.tock.bot.connector.whatsapp.cloud.WhatsAppCloudApiClient
import ai.tock.bot.connector.whatsapp.cloud.database.model.PayloadWhatsAppCloud
import ai.tock.bot.connector.whatsapp.cloud.database.repository.PayloadWhatsAppCloudDAO
import ai.tock.bot.connector.whatsapp.cloud.model.common.MetaUploadHandle
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.FileType
import ai.tock.bot.connector.whatsapp.cloud.model.send.media.MediaResponse
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudSendBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudTypingIndicatorMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.HeaderParameter
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.PayloadParameter
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsappTemplateComponent
import ai.tock.bot.connector.whatsapp.cloud.model.template.WhatsappTemplate
import ai.tock.bot.connector.whatsapp.cloud.spi.AssetUploadingException
import ai.tock.bot.engine.BotRepository
import ai.tock.shared.Executor
import ai.tock.shared.TockProxyAuthenticator
import ai.tock.shared.cache.getOrCache
import ai.tock.shared.injector
import ai.tock.shared.jackson.mapper
import ai.tock.shared.provide
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.litote.kmongo.toId
import retrofit2.Call
import retrofit2.Response
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.UUID

class WhatsAppCloudApiService(private val apiClient: WhatsAppCloudApiClient) {
    private val logger = KotlinLogging.logger {}
    private val payloadWhatsApp: PayloadWhatsAppCloudDAO get() = injector.provide()
    private val executor: Executor get() = injector.provide()
    private val client = OkHttpClient.Builder().apply(TockProxyAuthenticator::install).build()

    fun sendMessage(
        phoneNumberId: String,
        messageRequest: WhatsAppCloudSendBotMessage,
    ) {
        send(messageRequest) {
            apiClient.sendMessage(phoneNumberId, messageRequest).execute()
        }
    }

    fun sendTypingIndicator(
        phoneNumberId: String,
        messageId: String,
    ) {
        apiClient.sendMessage(phoneNumberId, WhatsAppCloudTypingIndicatorMessage(messageId)).execute()
    }

    fun downloadImgByBinary(
        imgId: String,
        mimeType: String,
    ): String {
        val url =
            send(imgId) {
                apiClient.retrieveMediaUrl(imgId).execute()
            }.url

        val base64Response =
            responseDownloadMedia(url, mimeType) {
                apiClient.downloadMediaBinary(url).execute()
            }
        return base64Response
    }

    fun getUploadedImageId(imageUrl: String): String {
        val res = sendMedia(imageUrl, FileType.PNG.type)
        return res.id
    }

    fun getUploadedImageId(
        imageId: String,
        imageBytes: ByteArray,
        mimeType: String,
    ): String {
        val res = sendMedia(imageId, imageBytes, mimeType)
        return res.id
    }

    fun shortenPayload(parameters: PayloadParameter): PayloadParameter {
        return parameters.payload?.takeIf { it.length >= 128 }?.let {
            val uuidPayload = UUID.randomUUID().toString()
            executor.executeBlocking {
                payloadWhatsApp.save(PayloadWhatsAppCloud(uuidPayload, it, Date.from(Instant.now())))
            }
            parameters.copy(payload = uuidPayload)
        } ?: parameters
    }

    fun shortenPayload(payload: String): String {
        return if (payload.length >= 256) {
            val uuidPayload = UUID.randomUUID().toString()
            executor.executeBlocking {
                payloadWhatsApp.save(PayloadWhatsAppCloud(uuidPayload, payload, Date.from(Instant.now())))
            }
            uuidPayload
        } else {
            payload
        }
    }

    private fun sendMedia(
        fileUrl: String,
        fileType: String,
    ): MediaResponse {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_media")

        return getOrCache("${apiClient.phoneNumberId}-$fileUrl".toId(), IMAGE_ID_CACHE) {
            try {
                val file = retrieveMedia(fileUrl).toRequestBody(fileType.toMediaTypeOrNull())

                apiClient.uploadMediaInWhatsAppAccount(file).execute().body()
            } catch (e: Exception) {
                BotRepository.requestTimer.throwable(e, requestTimerData)
                throw if (e is ConnectorException) e else ConnectorException("Error sending media: ${e.message}")
            } finally {
                BotRepository.requestTimer.end(requestTimerData)
            }
        } ?: throw ConnectorException("Error sending media")
    }

    private fun sendMedia(
        fileId: String,
        fileBytes: ByteArray,
        fileType: String,
    ): MediaResponse {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_media_bytes")

        return getOrCache("${apiClient.phoneNumberId}-$fileId".toId(), IMAGE_ID_CACHE) {
            try {
                val file = fileBytes.toRequestBody(fileType.toMediaTypeOrNull())

                apiClient.uploadMediaInWhatsAppAccount(file).execute().body()
            } catch (e: Exception) {
                BotRepository.requestTimer.throwable(e, requestTimerData)
                throw if (e is ConnectorException) e else ConnectorException("Error sending media: ${e.message}")
            } finally {
                BotRepository.requestTimer.end(requestTimerData)
            }
        } ?: throw ConnectorException("Error sending media")
    }

    fun getOrUpload(
        metaApplicationId: String,
        fileUrl: String,
        fileType: String,
        fileContents: ByteArray? = null,
    ): MetaUploadHandle {
        val requestTimerData = BotRepository.requestTimer.start("whatsapp_upload_business_asset")

        return getOrCache("$metaApplicationId-$fileUrl".toId(), APP_UPLOAD_ID_CACHE) {
            try {
                val actualFileContents = fileContents ?: retrieveMedia(fileUrl)
                val uploadId =
                    call("asset_upload_start") {
                        apiClient.startFileUpload(metaApplicationId, actualFileContents.size.toLong(), fileType)
                    }.id
                val handle = apiClient.uploadFile(client, uploadId, actualFileContents)
                MetaUploadHandle(handle)
            } finally {
                BotRepository.requestTimer.end(requestTimerData)
            }
        } ?: throw AssetUploadingException("Error uploading asset $fileUrl to $metaApplicationId")
    }

    fun createOrUpdateTemplate(template: WhatsappTemplate) {
        // Ensure this code never runs concurrently for any given template name
        synchronized(template.name.intern()) {
            val existingTemplate =
                call(template.name, apiClient::getMessageTemplates)
                    .data.firstOrNull { it.language == template.language }

            if (existingTemplate == null) {
                logger.info { "Creating template ${template.name} for language ${template.language}" }
                logger.debug { mapper.writerWithDefaultPrettyPrinter().writeValueAsString(template) }
                call(template, apiClient::createMessageTemplate)
            } else if (!existingTemplate.contentEquals(template)) {
                logger.info { "Updating template ${template.name} for language ${template.language}" }
                call(checkNotNull(existingTemplate.id)) { id ->
                    apiClient.editMessageTemplate(id, template)
                }
            } else {
                logger.info { "Skipping existing template ${template.name} for language ${template.language}" }
            }
        }
    }

    fun deleteTemplate(templateName: String) {
        apiClient.deleteMessageTemplate(templateName).execute()
    }

    private inline fun <T : Any, R : Any> call(
        request: T,
        prepareCall: (T) -> Call<R>,
    ): R =
        send(request) {
            prepareCall(it).execute()
        }

    private inline fun <T : Any, R : Any> send(
        request: T,
        call: (T) -> Response<R>,
    ): R {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_${request as? String ?: request.javaClass.simpleName.lowercase()}")
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
        requestType: String = request.javaClass.simpleName.lowercase(),
        call: (T) -> Response<ResponseBody>,
    ): String {
        val requestTimerData =
            BotRepository.requestTimer.start("whatsapp_send_$requestType")

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

    fun replaceWithRealImageId(
        components: List<WhatsappTemplateComponent>,
        phoneNumberId: String,
    ) {
        components
            .asSequence()
            .filterIsInstance<WhatsappTemplateComponent.Carousel>()
            .flatMap { it.cards }
            .flatMap { it.components }
            .filterIsInstance<WhatsappTemplateComponent.Header>()
            .flatMap { it.parameters }
            .filterIsInstance<HeaderParameter.Image>()
            .filter { it.image.id != null }
            .map {
                it to
                    executor.executeBlockingTask {
                        sendMedia(
                            it.image.id!!,
                            FileType.PNG.type,
                        )
                    }
            }
            // exit from sequence
            .toList()
            .forEach {
                val imageHeader = it.first
                val newImageId = it.second.get().id
                imageHeader.image.id = newImageId
            }
    }

    private fun retrieveMedia(fileUrl: String): ByteArray {
        val request =
            Request.Builder()
                .url(fileUrl).build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Failed to download file: $fileUrl - ${response.message}")
            response.body?.byteStream()?.readBytes() ?: error("Empty body")
        }
    }

    private fun throwError(errorMessage: String): Nothing {
        throw ConnectorException(errorMessage)
    }
}

private const val IMAGE_ID_CACHE = "whatsapp_image_id_cache"
private const val APP_UPLOAD_ID_CACHE = "whatsapp_application_upload_id_cache"
