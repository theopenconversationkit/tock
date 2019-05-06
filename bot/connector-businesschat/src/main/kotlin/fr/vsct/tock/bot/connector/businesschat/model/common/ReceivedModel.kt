package fr.vsct.tock.bot.connector.businesschat.model.common

import com.fasterxml.jackson.annotation.JsonProperty
import fr.vsct.tock.bot.connector.businesschat.model.csp.BusinessChatCommonModel

/**
 * Common model for different types of message in Business Chat
 * See [https://developer.apple.com/documentation/businesschatapi/messages_received/receiving_messages_from_the_business_chat_service]
 * - message : the body contains the message
 * - interactive message : simple list picker response. Datas are in the InteractiveData
 * - large interactive message : list picker with icons. Datas are in th InteractiveDataRef
 */
class ReceivedModel(
    sourceId: String,
    destinationId: String,
    val body: String?,
    val interactiveData: InteractiveData?,
    val interactiveDataRef: InteractiveDataRef?,
    @JsonProperty("data")
    val handoverData: HandoverData?
) : BusinessChatCommonModel(sourceId = sourceId, destinationId = destinationId, type = MessageType.text)

data class HandoverData (
    @JsonProperty("new_owner_app_id")
    val newOwnerAppId: String?,
    @JsonProperty("recipient_id")
    val recipientId: String?,
    val metadata: String?,
    @JsonProperty("recipient_user_id")
    val recipientUserId: String?
)

class InteractiveData(val data: Data)
class Data(val replyMessage: ReplyMessage?)
class ReplyMessage(val title: String)

class InteractiveDataRef(
    val url: String,
    val bid: String,
    @JsonProperty("signature-base64")
    val signatureBase64: String,
    val key: String,
    val signature: String,
    val owner: String,
    val size: Int
)

