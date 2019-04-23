/*
 * Copyright (C) 2019 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.businesschat

import fr.vsct.tock.bot.connector.businesschat.model.common.ListPickerChoice
import fr.vsct.tock.bot.connector.businesschat.model.common.ReceivedModel
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorImageMessage
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorListPickerMessage
import fr.vsct.tock.bot.connector.businesschat.model.input.BusinessChatConnectorTextMessage

/**
 * This interface should be implemented to define how the Business Chat implementation from the CSP behaves
 */
interface CSPBusinessChatClient {
    fun sendMessage(message: BusinessChatConnectorTextMessage)
    fun sendAttachment(attachment: BusinessChatConnectorImageMessage)
    fun sendListPicker(listPicker: BusinessChatConnectorListPickerMessage)
    fun receiveListPickerChoice(receivedModel: ReceivedModel): ListPickerChoice?
}