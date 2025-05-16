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

package ai.tock.bot.admin.test

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.rest.client.model.ClientAttachment
import ai.tock.bot.connector.rest.client.model.ClientAttachmentType
import ai.tock.bot.connector.rest.client.model.ClientChoice
import ai.tock.bot.connector.rest.client.model.ClientConnectorType
import ai.tock.bot.connector.rest.client.model.ClientGenericElement
import ai.tock.bot.connector.rest.client.model.ClientGenericMessage
import ai.tock.bot.connector.rest.client.model.ClientLocation
import ai.tock.bot.connector.rest.client.model.ClientMessage
import ai.tock.bot.connector.rest.client.model.ClientSentence
import ai.tock.bot.connector.rest.client.model.ClientUserInterfaceType
import ai.tock.bot.connector.rest.client.model.ClientUserLocation
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericElement
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.message.Location
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.user.UserLocation
import ai.tock.translator.UserInterfaceType

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

fun GenericMessage.toClientSentenceElement(): ClientGenericMessage {
    return ClientGenericMessage(
        connectorType.toClientConnectorType(),
        attachments.map { it.toClientMessage() as ClientAttachment },
        choices.map { it.toClientMessage() as ClientChoice },
        texts,
        locations.map { it.toClientLocation() },
        metadata,
        subElements.map { it.toClientSentenceSubElement() }
    )
}

fun ClientGenericMessage.toSentenceElement(): GenericMessage {
    return GenericMessage(
        connectorType.toConnectorType(),
        attachments.map { it.toMessage() as Attachment },
        choices.map { it.toMessage() as Choice },
        texts,
        locations.map { it.toLocation() },
        metadata,
        subElements.map { it.toSentenceSubElement() }
    )
}

fun GenericElement.toClientSentenceSubElement(): ClientGenericElement {
    return ClientGenericElement(
        attachments.map { it.toClientMessage() as ClientAttachment },
        choices.map { it.toClientMessage() as ClientChoice },
        texts,
        locations.map { it.toClientLocation() },
        metadata
    )
}

fun ClientGenericElement.toSentenceSubElement(): GenericElement {
    return GenericElement(
        attachments.map { it.toMessage() as Attachment },
        choices.map { it.toMessage() as Choice },
        texts,
        locations.map { it.toLocation() },
        metadata
    )
}
