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

package ai.tock.bot.test

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.definition.ParameterKey
import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericElement
import ai.tock.bot.engine.message.GenericMessage
import ch.tutteli.atrium.api.cc.en_GB.any
import ch.tutteli.atrium.api.cc.en_GB.contains
import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.containsNot
import ch.tutteli.atrium.api.cc.en_GB.notToBeNull
import ch.tutteli.atrium.api.cc.en_GB.property
import ch.tutteli.atrium.api.cc.en_GB.returnValueOf
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.domain.builders.AssertImpl
import ch.tutteli.atrium.domain.creating.any.typetransformation.AnyTypeTransformation
import ch.tutteli.atrium.reporting.RawString
import ch.tutteli.atrium.reporting.translating.Untranslatable
import ch.tutteli.atrium.verbs.expect

fun Assert<BotBusMockLog>.toBeSimpleTextMessage(expectedText: String) = returnValueOf(BotBusMockLog::text).toBe(expectedText)

fun Assert<BotBusMockLog>.asGenericMessage(assertionCreator: Assert<GenericMessage>.() -> Unit) {
    val parameterObject =
        AnyTypeTransformation.ParameterObject(
            Untranslatable("is a"),
            RawString.create(GenericMessage::class.simpleName!!),
            this,
            assertionCreator,
            Untranslatable("cannot be converted to generic message : is not a send sentence"),
        )
    AssertImpl.any.typeTransformation.transform(
        parameterObject,
        { it.action is SendSentence && it.genericMessage() != null },
        { it.genericMessage()!! },
        AssertImpl.any.typeTransformation.failureHandlers.newExplanatory(),
    )
}

fun Assert<GenericMessage>.toHaveGlobalText(
    expectedText: String,
    textName: String = "text",
) = property(GenericMessage::texts).addAssertionsCreatedBy {
    returnValueOf(Map<String, String>::get, textName).toBe(expectedText)
}

fun Assert<GenericMessage>.toHaveGlobalChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
) = property(GenericMessage::choices).addAssertionsCreatedBy {
    expect(
        subject.map { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        },
    ).contains(expectedChoice, *otherExpectedChoices)
}

fun Assert<GenericMessage>.toHaveNotGlobalChoices(
    unexpectedChoice: String,
    vararg otherUnexpectedChoices: String,
) = property(GenericMessage::choices).addAssertionsCreatedBy {
    expect(
        subject.map { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        },
    ).containsNot(unexpectedChoice, *otherUnexpectedChoices)
}

fun Assert<GenericMessage>.toHaveExactlyGlobalChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
) = property(GenericMessage::choices).addAssertionsCreatedBy {
    expect(
        subject.map { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        },
    ).containsExactly(expectedChoice, *otherExpectedChoices)
}

fun Assert<GenericMessage>.toHaveElement(
    index: Int,
    assertionCreator: Assert<GenericElement>.() -> Unit,
) = property(GenericMessage::subElements).addAssertionsCreatedBy {
    returnValueOf(List<GenericElement>::get, index) {
        addAssertionsCreatedBy(assertionCreator)
    }
}

fun Assert<GenericElement>.toHaveText(
    expectedText: String,
    textName: String,
) = property(GenericElement::texts).addAssertionsCreatedBy {
    returnValueOf(Map<String, String>::get, textName).toBe(expectedText)
}

fun Assert<GenericElement>.toHaveTitle(expectedTitle: String) = toHaveText(expectedTitle, "title")

fun Assert<GenericElement>.toHaveSubtitle(expectedSubtitle: String) = toHaveText(expectedSubtitle, "subtitle")

fun Assert<GenericElement>.toHaveChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
) = property(GenericElement::choices).addAssertionsCreatedBy {
    expect(
        subject.mapNotNull { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        },
    ).contains(expectedChoice, *otherExpectedChoices)
}

fun Assert<GenericElement>.toHaveNotChoices(
    unexpectedChoice: String,
    vararg otherUnexpectedChoices: String,
) = property(GenericElement::choices).addAssertionsCreatedBy {
    expect(
        subject.mapNotNull { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        },
    ).containsNot(unexpectedChoice, *otherUnexpectedChoices)
}

fun Assert<GenericElement>.toHaveExactlyChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
) = property(GenericElement::choices).addAssertionsCreatedBy {
    expect(
        subject.mapNotNull { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        },
    ).containsExactly(expectedChoice, *otherExpectedChoices)
}

fun ConnectorMessage.asGenericMessage(assertionCreator: Assert<GenericMessage>.() -> Unit) {
    expect(toGenericMessage()).notToBeNull(assertionCreator)
}

fun Assert<GenericElement>.toHaveAttachment(assertionCreator: Assert<Attachment>.() -> Unit) =
    property(GenericElement::attachments).addAssertionsCreatedBy {
        any {
            addAssertionsCreatedBy(assertionCreator)
        }
    }

fun Assert<Attachment>.toHaveUrl(url: String) = property(Attachment::url).toBe(url)

fun Assert<Attachment>.toBeImage() = property(Attachment::type).toBe(SendAttachment.AttachmentType.image)

fun Assert<GenericMessage>.toHaveChoice(
    title: String,
    assertionCreator: Assert<Choice>.() -> Unit,
) = property(GenericMessage::choices).addAssertionsCreatedBy {
    any {
        toHaveTitle(title)
        addAssertionsCreatedBy(assertionCreator)
    }
}

fun Assert<Choice>.toHaveTitle(title: String) {
    toHaveParameter(SendChoice.TITLE_PARAMETER, title)
}

fun Assert<Choice>.toHaveIntent(intentName: String) {
    property(Choice::intentName).toBe(intentName)
}

fun Assert<Choice>.toHaveParameter(
    key: ParameterKey,
    value: String,
) {
    property(Choice::parameters).addAssertionsCreatedBy {
        returnValueOf(Map<String, String>::get, key.name).toBe(value)
    }
}

fun Assert<Choice>.toHaveParameter(
    key: String,
    value: String,
) {
    property(Choice::parameters).addAssertionsCreatedBy {
        returnValueOf(Map<String, String>::get, key).toBe(value)
    }
}
