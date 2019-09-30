package ai.tock.bot.connector.businesschat.model.csp.listPicker

import ai.tock.bot.connector.businesschat.model.common.MessageType
import ai.tock.bot.connector.businesschat.model.csp.BusinessChatCommonModel
import java.util.UUID

/**
 * See [https://developer.apple.com/documentation/businesschatapi/messages_sent/interactive_messages/list_picker]
 */
const val BID: String =
    "com.apple.messages.MSMessageExtensionBalloonPlugin:0000000000:com.apple.icloud.apps.messages.business.extension"

class ListPickerMessage(
    sourceId: String,
    destinationId: String,
    val interactiveData: InteractiveData
) : BusinessChatCommonModel(
    sourceId = sourceId,
    destinationId = destinationId,
    type = MessageType.interactive
)

class InteractiveData(
    val data: ListPickerData,
    val receivedMessage: ReceivedMessage
) {
    val bid: String = BID
}

class ReceivedMessage(
    val title: String,
    val subtitle: String?
) {
    val style: String = "icon"
}

class ListPickerData(
    val listPicker: ListPicker,
    val images: List<Image?>?
) {
    val version: String = "1.0"
    val requestIdentifier: String = UUID.randomUUID().toString()
}

class Image(val identifier: String, val data: String)

class ListPicker(
    val sections: List<ListPickerSection> = emptyList()
)

class ListPickerSection(
    val items: List<ListPickerItem> = emptyList(),
    val order: Int,
    val title: String,
    val multipleSelection: Boolean = false
)

class ListPickerItem(
    val identifier: String,
    val imageIdentifier: String? = null,
    val order: Int,
    val subtitle: String?,
    val title: String
) {
    val style: String = "default"
}
