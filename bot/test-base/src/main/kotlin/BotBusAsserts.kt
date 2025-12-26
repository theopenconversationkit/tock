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
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.get
import ch.tutteli.atrium.api.fluent.en_GB.getExisting
import ch.tutteli.atrium.api.fluent.en_GB.notToContain
import ch.tutteli.atrium.api.fluent.en_GB.notToEqualNull
import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toContainExactly
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.fluent.en_GB.toHaveElementsAndAny
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.core.Option
import ch.tutteli.atrium.core.Some
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.logic._logic
import ch.tutteli.atrium.logic.creating.iterablelike.contains.reporting.InOrderOnlyReportingOptions
import ch.tutteli.atrium.logic.creating.transformers.impl.subjectchanger.DefaultFailureHandlerImpl
import ch.tutteli.atrium.logic.creating.transformers.subjectChanger
import ch.tutteli.atrium.reporting.Text
import ch.tutteli.atrium.reporting.translating.Untranslatable

/**
 * Expects that the subject of `this` expectation (a [BotBusMockLog]) is a simple text message
 * with the value [expectedText].
 *
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<BotBusMockLog>.toBeSimpleTextMessage(expectedText: String) = feature(BotBusMockLog::text).toEqual(expectedText)

/**
 * Expects that the subject of `this` expectation (a [BotBusMockLog]) is a [ConnectorMessage],
 * and converts it to a [GenericMessage] for further assertions.
 */
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

/**
 * Expects that the subject of `this` expectation (a [GenericMessage]) has top-level text
 * with the value [expectedText].
 *
 * @param paramName the name of the text parameter to check (connector-dependant).
 *   Common values are [GenericMessage.TEXT_PARAM], [GenericMessage.TITLE_PARAM], or [GenericMessage.SUBTITLE_PARAM].
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<GenericMessage>.toHaveGlobalText(
    expectedText: String,
    paramName: String = GenericMessage.TEXT_PARAM,
): Expect<GenericMessage> = feature(GenericMessage::texts) {
    getExisting(paramName).toEqual(expectedText)
}

/**
 * Expects that the subject of `this` expectation (a [GenericMessage]) contains top-level choices (buttons)
 * with the given button titles.
 *
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<GenericMessage>.toHaveGlobalChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
): Expect<GenericMessage> = feature(GenericMessage::choices) {
    feature("title", {
        map { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }
    }, { toContain(expectedChoice, *otherExpectedChoices) })
}

/**
 * Expects that the subject of `this` expectation (a [GenericMessage]) does _not_ contain top-level choices (buttons)
 * with the given button titles.
 *
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<GenericMessage>.toHaveNotGlobalChoices(
    unexpectedChoice: String,
    vararg otherUnexpectedChoices: String,
): Expect<GenericMessage> = feature(GenericMessage::choices) {
    feature("title", {
        map { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }
    }, { notToContain(unexpectedChoice, *otherUnexpectedChoices) })
}

/**
 * Expects that the subject of `this` expectation (a [GenericMessage]) contains exactly the top-level choices (buttons)
 * with the given button titles, in the specified order.
 *
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<GenericMessage>.toHaveExactlyGlobalChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
): Expect<GenericMessage> = feature(GenericMessage::choices) {
    feature("title", {
        map { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }
    }, { toContainExactly(expectedChoice, *otherExpectedChoices) })
}

/**
 * Expects that the subject of `this` expectation (a [GenericMessage]) contains only an entry holding
 * the assertions created by [firstChoiceAssertion]
 * and likewise an additional entry for each [otherChoicesAssertions] (if given)
 * whereas the entries have to appear in the defined order.
 *
 * It is a shortcut for `toContain.inOrder.only.entries(firstChoiceAssertion, *otherChoicesAssertions)`
 *
 * @param firstChoiceAssertion The identification lambda which creates the assertions which the entry we are looking
 *   for has to hold; or in other words, the function which defines whether an entry is the one we are looking for
 *   or not.
 * @param otherChoicesAssertions Additional identification lambdas which each identify (separately) an entry
 *   which we are looking for (see [firstChoiceAssertion] for more information).
 * @param report The lambda configuring the [InOrderOnlyReportingOptions] -- it is optional where
 *   the default [InOrderOnlyReportingOptions] apply if not specified.
 *
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<GenericMessage>.toHaveExactlyGlobalChoices(
    firstChoiceAssertion: (Expect<Choice>.() -> Unit),
    vararg otherChoicesAssertions: Expect<Choice>.() -> Unit,
    report: InOrderOnlyReportingOptions.() -> Unit = {}
): Expect<GenericMessage> = feature(GenericMessage::choices) {
    toContainExactly(
        assertionCreatorOrNull = firstChoiceAssertion,
        otherAssertionCreatorsOrNulls = otherChoicesAssertions,
        report = report
    )
}

/**
 * Expects that the given [index] is within the bounds of the elements in the subject of `this` expectation
 * (a [GenericElement]) and tests the [GenericElement] at that position.
 *
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<GenericMessage>.toHaveElement(
    index: Int,
    assertionCreator: Expect<GenericElement>.() -> Unit,
): Expect<GenericMessage> = feature(GenericMessage::subElements) {
    get(index, assertionCreator)
}

/**
 * Expects that the subject of `this` expectation (a [GenericElement]) has text
 * with the value [expectedText].
 *
 * @param paramName the name of the text parameter to check (connector-dependant).
 *   Common values are [GenericMessage.TEXT_PARAM], [GenericMessage.TITLE_PARAM], or [GenericMessage.SUBTITLE_PARAM]
 * @return an [Expect] for the subject of `this` expectation.
 *
 * @see toHaveTitle
 * @see toHaveSubtitle
 */
fun Expect<GenericElement>.toHaveText(
    expectedText: String,
    paramName: String,
): Expect<GenericElement> = feature(GenericElement::texts) {
    getExisting(paramName).toEqual(expectedText)
}

/**
 * Expects that the subject of `this` expectation (a [GenericElement]) has a title
 * with the value [expectedTitle].
 *
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<GenericElement>.toHaveTitle(expectedTitle: String): Expect<GenericElement> = toHaveText(expectedTitle, GenericMessage.TITLE_PARAM)

/**
 * Expects that the subject of `this` expectation (a [GenericElement]) has a subtitle
 * with the value [expectedSubtitle].
 *
 * @return an [Expect] for the subject of `this` expectation.
 */
fun Expect<GenericElement>.toHaveSubtitle(expectedSubtitle: String): Expect<GenericElement> = toHaveText(expectedSubtitle, GenericMessage.SUBTITLE_PARAM)

fun Expect<GenericElement>.toHaveChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
): Expect<GenericElement> = feature(GenericElement::choices) {
    feature("title", {
        mapNotNull { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }
    }, { toContain(expectedChoice, *otherExpectedChoices) })
}

fun Expect<GenericElement>.toHaveNotChoices(
    unexpectedChoice: String,
    vararg otherUnexpectedChoices: String,
): Expect<GenericElement> = feature(GenericElement::choices) {
    feature("title", {
        mapNotNull { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }
    }, { notToContain(unexpectedChoice, *otherUnexpectedChoices) })
}

fun Expect<GenericElement>.toHaveExactlyChoices(
    expectedChoice: String,
    vararg otherExpectedChoices: String,
): Expect<GenericElement> = feature(GenericElement::choices) {
    feature("title", {
        mapNotNull { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }
    }, { toContainExactly(expectedChoice, *otherExpectedChoices) })
}

fun Expect<GenericElement>.toHaveExactlyChoices(
    firstChoiceAssertion: (Expect<Choice>.() -> Unit)?,
    vararg otherChoicesAssertions: Expect<Choice>.() -> Unit,
): Expect<GenericElement> = feature(GenericElement::choices) {
    toContainExactly(assertionCreatorOrNull = firstChoiceAssertion, *otherChoicesAssertions)
}

fun Expect<GenericElement>.toHaveChoice(
    title: String,
    assertionCreator: Expect<Choice>.() -> Unit,
): Expect<GenericElement> = feature(GenericElement::choices) {
    toHaveElementsAndAny {
        toHaveTitle(title)
        assertionCreator()
    }
}

fun ConnectorMessage.asGenericMessage(assertionCreator: Expect<GenericMessage>.() -> Unit) {
    expect(toGenericMessage()).notToEqualNull(assertionCreator)
}

fun Expect<GenericElement>.toHaveAttachment(assertionCreator: Expect<Attachment>.() -> Unit) {
    feature(GenericElement::attachments) {
        toHaveElementsAndAny(assertionCreator)
    }
}

fun Expect<Attachment>.toHaveUrl(url: String) = feature(Attachment::url).toEqual(url)

fun Expect<Attachment>.toBeImage() = feature(Attachment::type).toEqual(SendAttachment.AttachmentType.image)

fun Expect<GenericMessage>.toHaveChoice(
    title: String,
    assertionCreator: Expect<Choice>.() -> Unit,
): Expect<GenericMessage> = feature(GenericMessage::choices) {
    toHaveElementsAndAny {
        toHaveTitle(title)
        assertionCreator()
    }
}

fun Expect<Choice>.toHaveTitle(title: String): Expect<Choice> = toHaveParameter(SendChoice.TITLE_PARAMETER, title)

fun Expect<Choice>.toHaveIntent(intentName: String): Expect<Choice> = feature(Choice::intentName) {
    toEqual(intentName)
}

fun Expect<Choice>.toHaveParameter(
    key: ParameterKey,
    value: String,
): Expect<Choice> = feature(Choice::parameters) {
    feature(Map<String, String>::get, key.name).toEqual(value)
}

fun Expect<Choice>.toHaveParameter(
    key: String,
    value: String,
): Expect<Choice> = feature(Choice::parameters) {
    feature(Map<String, String>::get, key).toEqual(value)
}
