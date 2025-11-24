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

package ai.tock.bot.connector.businesschat

import ai.tock.bot.connector.businesschat.model.common.ListPickerChoice
import ai.tock.bot.connector.businesschat.model.common.ReceivedModel
import ai.tock.bot.connector.businesschat.model.csp.attachment.Attachment
import ai.tock.bot.connector.businesschat.model.csp.attachment.AttachmentDictionnary
import ai.tock.bot.connector.businesschat.model.csp.listPicker.Image
import ai.tock.bot.connector.businesschat.model.csp.listPicker.InteractiveData
import ai.tock.bot.connector.businesschat.model.csp.listPicker.ListPicker
import ai.tock.bot.connector.businesschat.model.csp.listPicker.ListPickerData
import ai.tock.bot.connector.businesschat.model.csp.listPicker.ListPickerItem
import ai.tock.bot.connector.businesschat.model.csp.listPicker.ListPickerMessage
import ai.tock.bot.connector.businesschat.model.csp.listPicker.ListPickerSection
import ai.tock.bot.connector.businesschat.model.csp.listPicker.ReceivedMessage
import ai.tock.bot.connector.businesschat.model.csp.message.Message
import ai.tock.bot.connector.businesschat.model.csp.richLink.Assets
import ai.tock.bot.connector.businesschat.model.csp.richLink.RichLinkData
import ai.tock.bot.connector.businesschat.model.csp.richLink.RichLinkMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorImageMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorListPickerMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorRichLinkMessage
import ai.tock.bot.connector.businesschat.model.input.BusinessChatConnectorTextMessage
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.io.BaseEncoding
import com.google.common.io.ByteStreams
import mu.KotlinLogging
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.codec.binary.Hex
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.security.Key
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import java.util.zip.GZIPInputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class CSPBusinessChatClient(val integrationService: BusinessChatIntegrationService) {
    private val logger = KotlinLogging.logger { }
    private val businessChatClientApi: BusinessChatClientApi

    private interface BusinessChatClientApi {
        @POST("message")
        fun sendMessage(
            @Body message: Message,
        ): Call<ResponseBody>

        @GET("preUpload")
        fun preUploadAttachment(
            @Header("Source-Id") sourceId: String,
            @Header("MMCS-Size") payloadSize: Int,
        ): Call<PreUploadResponse>

        @POST("message")
        fun sendAttachment(
            @Header("MMCS-Size") payloadSize: Int,
            @Body attachment: Attachment,
        ): Call<ResponseBody>

        @GET("preDownload")
        fun preDownloadAttachment(
            @Header("Source-Id") sourceId: String,
            // @retrofit2.http.Header("Destination-Id") destinationId: String,
            @Header("url") url: String,
            @Header("signature") signature: String,
            @Header("owner") owner: String,
        ): Call<PreDownloadResponse>

        @POST("message")
        fun sendListPicker(
            @Body listPicker: ListPickerMessage,
        ): Call<ResponseBody>

        @POST("message")
        fun sendRichLink(
            @Body richLink: RichLinkMessage,
        ): Call<ResponseBody>

        @Headers("Content-Type: application/x-www-form-urlencoded", "accept: */*")
        @POST("decodePayload")
        fun decodePayload(
            @Header("bid") bid: String,
            @Header("source-id") sourceId: String,
            @Body payload: RequestBody,
        ): Call<DecodePayloadResponse>
    }

    private data class FileChecksum(val fileChecksum: String, val size: Int, val receipt: String)

    private data class UploadResponse(val singleFile: FileChecksum)

    private data class PreUploadResponse(
        @JsonProperty("upload-url")
        val uploadUrl: String,
        @JsonProperty("mmcs-url")
        val mmcsUrl: String,
        @JsonProperty("mmcs-owner")
        val mmcsOwner: String,
    )

    private data class PreDownloadResponse(
        @JsonProperty("download-url")
        val downloadUrl: String,
    )

    private data class DecodePayloadResponse(val data: DecodePayloadData)

    private data class DecodePayloadData(val replyMessage: DecodePayloadReplyMessage)

    private data class DecodePayloadReplyMessage(val title: String)

    init {
        businessChatClientApi = integrationService.createClient(BusinessChatClientApi::class, logger)
    }

    fun sendMessage(message: BusinessChatConnectorTextMessage) {
        val response =
            businessChatClientApi
                .sendMessage(
                    Message(
                        message.sourceId,
                        message.destinationId,
                        message.body,
                    ),
                ).execute()
        if (response.isSuccessful) {
            logger.info { "successful call to business chat " }
        } else {
            logger.error { "error while sending message to business chat " }
        }
    }

    fun sendAttachment(attachment: BusinessChatConnectorImageMessage) {
        val rawKey = ByteArray(32)
        SecureRandom.getInstanceStrong().nextBytes(rawKey)
        val key = SecretKeySpec(rawKey, "AES")

        val encryptedAttachment = encryptAttachment(attachment.bytes, key)
        val hexKey = "00" + BaseEncoding.base16().encode(key.encoded)

        // preupload
        val preUploadAttachment =
            businessChatClientApi.preUploadAttachment(attachment.sourceId, encryptedAttachment.size).execute()
        val preUploadResponse = preUploadAttachment.body() ?: error("PreUpload failed")

        // upload
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val client =
            OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectionSpecs(
                    listOf(
                        ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build(),
                    ),
                ).build()

        val upload =
            Request
                .Builder()
                .url(preUploadResponse.uploadUrl)
                .post(RequestBody.create("text/html; charset=utf-8".toMediaType(), encryptedAttachment))
                .build()
        val execute = client.newCall(upload).execute()

        val rep = execute.body!!.string()
        val fileChecksum = jacksonObjectMapper().readValue<UploadResponse>(rep).singleFile.fileChecksum

        // send attachement
        businessChatClientApi.sendAttachment(
            encryptedAttachment.size,
            Attachment(
                attachment.sourceId,
                attachment.destinationId,
                arrayOf(
                    AttachmentDictionnary(
                        name = """${UUID.randomUUID()}.png""",
                        mimeType = attachment.mimeType,
                        size = encryptedAttachment.size,
                        signatureBase64 = fileChecksum,
                        url = preUploadResponse.mmcsUrl,
                        owner = preUploadResponse.mmcsOwner,
                        key = hexKey,
                    ),
                ),
            ),
        ).execute()
    }

    private fun encryptAttachment(
        attachment: ByteArray,
        key: Key,
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ByteArray(16)))
        return cipher.doFinal(attachment)
    }

    fun sendListPicker(listPicker: BusinessChatConnectorListPickerMessage) {
        val listPickerItemsIndexed =
            listPicker.items
                .mapIndexed { index, item ->
                    val identifier = UUID.randomUUID().toString()
                    Pair(
                        ListPickerItem(
                            index.toString(),
                            identifier,
                            index,
                            item.subtitle,
                            item.title,
                        ),
                        if (item.image != null) {
                            Image(identifier, Base64.getEncoder().encodeToString(item.image))
                        } else {
                            null
                        },
                    )
                }
                .unzip()

        val response =
            businessChatClientApi
                .sendListPicker(
                    ListPickerMessage(
                        listPicker.sourceId,
                        listPicker.destinationId,
                        InteractiveData(
                            ListPickerData(
                                images = listPickerItemsIndexed.second.filterNotNull(),
                                listPicker =
                                    ListPicker(
                                        sections =
                                            listOf(
                                                ListPickerSection(
                                                    items = listPickerItemsIndexed.first,
                                                    order = 0,
                                                    title = listPicker.listDetails,
                                                ),
                                            ),
                                    ),
                            ),
                            ReceivedMessage(
                                listPicker.title,
                                listPicker.subtitle,
                            ),
                        ),
                    ),
                ).execute()
        if (response.isSuccessful) {
            logger.info { "successful call to business chat " }
        } else {
            logger.error { "error while sending message to business chat " }
        }
    }

    fun receiveListPickerChoice(receivedModel: ReceivedModel): ListPickerChoice? {
        when {
            isListPickerReply(receivedModel) -> {
                with(receivedModel.interactiveData!!.data.replyMessage!!.title) {
                    return ListPickerChoice(this)
                }
            }
            isComplexListPickerReply(receivedModel) -> {
                // Payload is encrypted and has to be downloaded
                // see https://developer.apple.com/documentation/businesschatapi/messages_sent/interactive_messages/receiving_large_interactive_data_payloads
                val dataRef = receivedModel.interactiveDataRef!!
                val businessId = receivedModel.destinationId

                // getting the download url
                val preDownloadResponse =
                    businessChatClientApi.preDownloadAttachment(
                        businessId,
                        dataRef.url,
                        dataRef.signatureBase64,
                        dataRef.owner,
                    ).execute().body()

                // download the encrypted payload
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                val client =
                    OkHttpClient.Builder().addInterceptor(logging)
                        .connectionSpecs(
                            listOf(
                                ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                                    .tlsVersions(TlsVersion.TLS_1_2)
                                    .build(),
                            ),
                        )
                        .build()

                val download =
                    Request
                        .Builder()
                        .url(preDownloadResponse!!.downloadUrl)
                        .get()
                        .build()
                val cryptedAttachment = client.newCall(download).execute()

                // To decrypt a downloaded attachment file using key,
                // remove the 00 prefix from the hex-encoded string,
                // then decode the string into its original value.
                // Use the decoded key value to decrypt the downloaded attachment file.
                val rep = cryptedAttachment.body!!.bytes()

                val hexKey = dataRef.key.removePrefix("00")
                val decodeHex = Hex.decodeHex(hexKey)

                val secretKey = SecretKeySpec(decodeHex, "AES")
                val decryptedAttachment = decryptAttachement(rep, secretKey)

                val unzippedResult = ungzip(decryptedAttachment)

                val payload =
                    businessChatClientApi.decodePayload(
                        dataRef.bid,
                        businessId,
                        RequestBody.create("application/x-www-form-urlencoded".toMediaType(), unzippedResult),
                    ).execute().run {
                        body()
                            ?: errorBody()?.string()?.let {
                                logger.error("body is empty, then use errorBody")
                                mapper.readValue<DecodePayloadResponse>(it)
                            }
                            ?: error("payload is null")
                    }
                with(payload.data.replyMessage.title) {
                    return ListPickerChoice(this)
                }
            }
            else -> {
                logger.error { "interactiveDataRef is null" }
                return null
            }
        }
    }

    fun sendRichLink(richLink: BusinessChatConnectorRichLinkMessage) {
        businessChatClientApi.sendRichLink(
            RichLinkMessage(
                richLink.sourceId,
                richLink.destinationId,
                RichLinkData(
                    richLink.url,
                    richLink.title,
                    Assets(
                        ai.tock.bot.connector.businesschat.model.csp.richLink.Image(
                            richLink.image,
                            richLink.mimeType,
                        ),
                    ),
                ),
            ),
        ).execute()
    }

    private fun isListPickerReply(receivedModel: ReceivedModel) = receivedModel.interactiveData != null && receivedModel.interactiveData.data.replyMessage != null

    private fun isComplexListPickerReply(receivedModel: ReceivedModel) = receivedModel.interactiveDataRef != null

    private fun decryptAttachement(
        attachment: ByteArray,
        key: Key,
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(16)))
        return cipher.doFinal(attachment)
    }

    private fun ungzip(content: ByteArray): ByteArray = ByteStreams.toByteArray(GZIPInputStream(content.inputStream()))
}
