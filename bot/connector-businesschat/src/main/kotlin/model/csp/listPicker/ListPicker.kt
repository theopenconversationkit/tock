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
