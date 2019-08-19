package fr.vsct.tock.bot.test

import ch.tutteli.atrium.api.cc.en_GB.contains
import ch.tutteli.atrium.api.cc.en_GB.property
import ch.tutteli.atrium.api.cc.en_GB.returnValueOf
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.domain.builders.AssertImpl
import ch.tutteli.atrium.domain.creating.any.typetransformation.AnyTypeTransformation
import ch.tutteli.atrium.reporting.RawString
import ch.tutteli.atrium.reporting.translating.Untranslatable
import ch.tutteli.atrium.verbs.expect
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.message.GenericElement
import fr.vsct.tock.bot.engine.message.GenericMessage

fun Assert<BotBusMockLog>.toBeSimpleTextMessage(expectedText: String) =
    returnValueOf(BotBusMockLog::text).toBe(expectedText)

fun Assert<BotBusMockLog>.asGenericMessage(assertionCreator: Assert<GenericMessage>.() -> Unit) {
    val parameterObject = AnyTypeTransformation.ParameterObject(
        Untranslatable("is a"),
        RawString.create(GenericMessage::class.simpleName!!),
        this,
        assertionCreator,
        Untranslatable("cannot be converted to generic message : is not a send sentence")
    )
    AssertImpl.any.typeTransformation.transform(
        parameterObject, { it.action is SendSentence && it.genericMessage() != null }, { it.genericMessage()!! },
        AssertImpl.any.typeTransformation.failureHandlers.newExplanatory()
    )
}

fun Assert<GenericMessage>.toHaveGlobalText(expectedText: String, textName: String = "text") =
    property(GenericMessage::texts).addAssertionsCreatedBy {
        returnValueOf(Map<String, String>::get, textName).toBe(expectedText)
    }

fun Assert<GenericMessage>.toHaveGlobalChoices(expectedChoice  : String, vararg otherExpectedChoices: String) =
    property(GenericMessage::choices).addAssertionsCreatedBy {
        expect(subject.map { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }).contains(expectedChoice, *otherExpectedChoices)
    }

fun Assert<GenericMessage>.toHaveElement(index: Int, assertionCreator: Assert<GenericElement>.() -> Unit) =
    property(GenericMessage::subElements).addAssertionsCreatedBy {
        returnValueOf(List<GenericElement>::get, index) {
            addAssertionsCreatedBy(assertionCreator)
        }
    }

fun Assert<GenericElement>.toHaveText(expectedText: String, textName: String) =
    property(GenericElement::texts).addAssertionsCreatedBy {
        returnValueOf(Map<String, String>::get, textName).toBe(expectedText)
    }

fun Assert<GenericElement>.toHaveTitle(expectedTitle: String) =
    toHaveText(expectedTitle, "title")

fun Assert<GenericElement>.toHaveSubtitle(expectedSubtitle: String) =
    toHaveText(expectedSubtitle, "subtitle")

fun Assert<GenericElement>.toHaveChoices(expectedChoice  : String, vararg otherExpectedChoices: String) =
    property(GenericElement::choices).addAssertionsCreatedBy {
        expect(subject.mapNotNull { choice ->
            choice.parameters[SendChoice.TITLE_PARAMETER]
        }).contains(expectedChoice, *otherExpectedChoices)
    }


