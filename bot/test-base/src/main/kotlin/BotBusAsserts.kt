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
import ch.tutteli.atrium.api.fluent.en_GB.any
import ch.tutteli.atrium.api.fluent.en_GB.contains
import ch.tutteli.atrium.api.fluent.en_GB.containsExactly
import ch.tutteli.atrium.api.fluent.en_GB.containsNot
import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.domain.builders.ExpectImpl
import ch.tutteli.atrium.domain.creating.any.typetransformation.AnyTypeTransformation
import ch.tutteli.atrium.reporting.RawString
import ch.tutteli.atrium.reporting.translating.Untranslatable
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.assertions.builders.assertionBuilder
import ch.tutteli.atrium.core.Option
import ch.tutteli.atrium.core.Some
import ch.tutteli.atrium.logic._logic
import ch.tutteli.atrium.logic.changeSubject
import ch.tutteli.atrium.logic.creating.transformers.impl.subjectchanger.DefaultFailureHandlerImpl
import ch.tutteli.atrium.logic.creating.transformers.subjectChanger
import ch.tutteli.atrium.reporting.Text
import kotlin.collections.map

fun Expect<BotBusMockLog>.toBeSimpleTextMessage(expectedText: String) = feature(BotBusMockLog::text).toBe(expectedText)

fun Expect<BotBusMockLog>.asGenericMessage(assertionCreator: Expect<GenericMessage>.() -> Unit) {
    _logic.subjectChanger.reported(
        _logic,
        Untranslatable("is a"),
        Text(GenericMessage::class.simpleName!!),
        { Option.someIf(
            it.action is SendSentence && it.genericMessage() != null,
            { it.genericMessage()!! }
        ) },
        DefaultFailureHandlerImpl(),
        Some(assertionCreator),
    )
}

fun Expect<GenericMessage>.toHaveGlobalText(
    expectedText: String,
    textName: String = "text",
) = feature(GenericMessage::texts) {
    feature(Map<String, String>::get, textName).toBe(expectedText)
}

fun Expect<GenericMessage>.toHaveGlobalChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
): Expect<List<Choice>> = feature(GenericMessage::choices).feature("title", {
        map { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }
    }, { contains(expectedChoice, *otherExpectedChoices) })

fun Expect<GenericMessage>.toHaveNotGlobalChoices(
    unexpectedChoice: String,
    vararg otherUnexpectedChoices: String,
): Expect<List<Choice>> = feature(GenericMessage::choices).feature("title", {
    map { choice ->
        choice.parameters[SendChoice.TITLE_PARAMETER]
    }
}, { containsNot(unexpectedChoice, *otherUnexpectedChoices) })

fun Expect<GenericMessage>.toHaveExactlyGlobalChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
): Expect<List<Choice>> = feature(GenericMessage::choices).feature("title", {
    map { choice ->
        choice.parameters[SendChoice.TITLE_PARAMETER]
    }
}, { containsExactly(expectedChoice, *otherExpectedChoices) })

fun Expect<GenericMessage>.toHaveElement(
    index: Int,
    assertionCreator: Expect<GenericElement>.() -> Unit,
): Expect<GenericMessage> = feature(GenericMessage::subElements) {
    feature(List<GenericElement>::get, index, assertionCreator)
}

fun Expect<GenericElement>.toHaveText(
    expectedText: String,
    textName: String,
): Expect<GenericElement> = feature(GenericElement::texts) {
    feature(Map<String, String>::get, textName).toBe(expectedText)
}

fun Expect<GenericElement>.toHaveTitle(expectedTitle: String): Expect<GenericElement> = toHaveText(expectedTitle, "title")

fun Expect<GenericElement>.toHaveSubtitle(expectedSubtitle: String): Expect<GenericElement> = toHaveText(expectedSubtitle, "subtitle")

fun Expect<GenericElement>.toHaveChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
): Expect<List<Choice>> = feature(GenericElement::choices).feature("title", {
    mapNotNull { choice ->
        choice.parameters[SendChoice.TITLE_PARAMETER]
    }
}, { contains(expectedChoice, *otherExpectedChoices) })

fun Expect<GenericElement>.toHaveNotChoices(
    unexpectedChoice: String,
    vararg otherUnexpectedChoices: String,
): Expect<List<Choice>> = feature(GenericElement::choices).feature("title", {
    mapNotNull { choice ->
        choice.parameters[SendChoice.TITLE_PARAMETER]
    }
}, { containsNot(unexpectedChoice, *otherUnexpectedChoices) })

fun Expect<GenericElement>.toHaveExactlyChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
): Expect<List<Choice>> = feature(GenericElement::choices).feature("title", {
    mapNotNull { choice ->
        choice.parameters[SendChoice.TITLE_PARAMETER]
    }
}, { containsExactly(expectedChoice, *otherExpectedChoices) })

fun ConnectorMessage.asGenericMessage(assertionCreator: Expect<GenericMessage>.() -> Unit) {
    expect(toGenericMessage()).notToBeNull(assertionCreator)
}

fun Expect<GenericElement>.toHaveAttachment(assertionCreator: Expect<Attachment>.() -> Unit) {
    feature(GenericElement::attachments) {
        any(assertionCreator)
    }
}

fun Expect<Attachment>.toHaveUrl(url: String) = feature(Attachment::url).toBe(url)

fun Expect<Attachment>.toBeImage() = feature(Attachment::type).toBe(SendAttachment.AttachmentType.image)

fun Expect<GenericMessage>.toHaveChoice(
    title: String,
    assertionCreator: Expect<Choice>.() -> Unit,
): Expect<GenericMessage> = feature(GenericMessage::choices) {
    any {
        toHaveTitle(title)
        assertionCreator()
    }
}

fun Expect<Choice>.toHaveTitle(title: String) {
    toHaveParameter(SendChoice.TITLE_PARAMETER, title)
}

fun Expect<Choice>.toHaveIntent(intentName: String) {
    feature(Choice::intentName).toBe(intentName)
}

fun Expect<Choice>.toHaveParameter(
    key: ParameterKey,
    value: String,
) {
    feature(Choice::parameters) {
        feature(Map<String, String>::get, key.name).toBe(value)
    }
}

fun Expect<Choice>.toHaveParameter(
    key: String,
    value: String,
) {
    feature(Choice::parameters) {
        feature(Map<String, String>::get, key).toBe(value)
    }
}
