/*
 * Copyright (C) 2017 VSCT
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

package fr.vsct.tock.bot.admin.test

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.rest.client.model.ClientAttachment
import fr.vsct.tock.bot.connector.rest.client.model.ClientAttachmentType
import fr.vsct.tock.bot.connector.rest.client.model.ClientChoice
import fr.vsct.tock.bot.connector.rest.client.model.ClientConnectorType
import fr.vsct.tock.bot.connector.rest.client.model.ClientLocation
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessage
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentence
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentenceElement
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentenceSubElement
import fr.vsct.tock.bot.connector.rest.client.model.ClientUserInterfaceType
import fr.vsct.tock.bot.connector.rest.client.model.ClientUserLocation
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.message.Attachment
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Location
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.bot.engine.message.SentenceElement
import fr.vsct.tock.bot.engine.message.SentenceSubElement
import fr.vsct.tock.bot.engine.user.UserLocation
import fr.vsct.tock.translator.UserInterfaceType

fun SendAttachment.AttachmentType.toClientAttachmentType(): ClientAttachmentType {
    return ClientAttachmentType.valueOf(name)
}

fun ClientAttachmentType.toAttachmentType(): SendAttachment.AttachmentType {
    return SendAttachment.AttachmentType.valueOf(name)
}

fun Location.toClientLocation(): ClientLocation {
    return ClientLocation(location?.let { ClientUserLocation(it.lat, it.lng) })
}

fun ClientLocation.toLocation(): Location {
    return Location(location?.let { UserLocation(it.lat, it.lng) })
}

fun ConnectorType.toClientConnectorType(): ClientConnectorType {
    return ClientConnectorType(id, userInterfaceType.toClientUserInterfaceType())
}

fun ClientConnectorType.toConnectorType(): ConnectorType {
    return ConnectorType(id, userInterfaceType.toUserInterfaceType())
}

fun UserInterfaceType.toClientUserInterfaceType(): ClientUserInterfaceType {
    return ClientUserInterfaceType.valueOf(name)
}

fun ClientUserInterfaceType.toUserInterfaceType(): UserInterfaceType {
    return UserInterfaceType.valueOf(name)
}

fun Message.toClientMessage(): ClientMessage {
    return when (this) {
        is Sentence -> ClientSentence(text, messages.map { it.toClientSentenceElement() }.toMutableList())
        is Choice -> ClientChoice(intentName, parameters)
        is Attachment -> ClientAttachment(url, type.toClientAttachmentType())
        is Location -> toClientLocation()
        else -> error("unsupported message $this")
    }
}

fun ClientMessage.toMessage(): Message {
    return when (this) {
        is ClientSentence -> Sentence(text, messages.map { it.toSentenceElement() }.toMutableList())
        is ClientChoice -> Choice(intentName, parameters)
        is ClientAttachment -> Attachment(url, type.toAttachmentType())
        is ClientLocation -> toLocation()
        else -> error("unsupported message $this")
    }
}

fun SentenceElement.toClientSentenceElement(): ClientSentenceElement {
    return ClientSentenceElement(
            connectorType.toClientConnectorType(),
            attachments.map { it.toClientMessage() as ClientAttachment },
            choices.map { it.toClientMessage() as ClientChoice },
            texts,
            locations.map { it.toClientLocation() },
            metadata,
            subElements.map { it.toClientSentenceSubElement() }
    )
}

fun ClientSentenceElement.toSentenceElement(): SentenceElement {
    return SentenceElement(
            connectorType.toConnectorType(),
            attachments.map { it.toMessage() as Attachment },
            choices.map { it.toMessage() as Choice },
            texts,
            locations.map { it.toLocation() },
            metadata,
            subElements.map { it.toSentenceSubElement() }
    )
}

fun SentenceSubElement.toClientSentenceSubElement(): ClientSentenceSubElement {
    return ClientSentenceSubElement(
            attachments.map { it.toClientMessage() as ClientAttachment },
            choices.map { it.toClientMessage() as ClientChoice },
            texts,
            locations.map { it.toClientLocation() },
            metadata
    )
}

fun ClientSentenceSubElement.toSentenceSubElement(): SentenceSubElement {
    return SentenceSubElement(
            attachments.map { it.toMessage() as Attachment },
            choices.map { it.toMessage() as Choice },
            texts,
            locations.map { it.toLocation() },
            metadata
    )
}