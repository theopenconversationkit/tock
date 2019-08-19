package fr.vsct.tock.bot.test


import ch.tutteli.atrium.api.cc.en_GB.isA
import ch.tutteli.atrium.api.cc.en_GB.notToBeNull
import ch.tutteli.atrium.api.cc.en_GB.property
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.domain.builders.AssertImpl
import ch.tutteli.atrium.domain.creating.any.typetransformation.AnyTypeTransformation
import ch.tutteli.atrium.reporting.RawString
import ch.tutteli.atrium.reporting.translating.Untranslatable
import ch.tutteli.atrium.verbs.expect
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.ButtonPayload
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.PostbackButton
import fr.vsct.tock.bot.connector.messenger.model.send.TextMessage
import fr.vsct.tock.bot.connector.messenger.model.send.TextQuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.UrlButton


fun Assert<BotBusMockLog>.toBeMessengerTextMessage(text: String) {
    expect(subject.messenger()).notToBeNull { isA<TextMessage> { property(TextMessage::text).toBe(text) } }
}

fun Assert<BotBusMockLog>.toBeMessengerAttachmentMessage(assertionCreator: Assert<AttachmentMessage>.() -> Unit) {
    val parameterObject = AnyTypeTransformation.ParameterObject(
        Untranslatable("is a"),
        RawString.create(AttachmentMessage::class.simpleName!!),
        this,
        assertionCreator,
        Untranslatable("is not an messenger attachment message")
    )
    AssertImpl.any.typeTransformation.transform(
        parameterObject, { it.messenger() is AttachmentMessage }, { it.messenger() as AttachmentMessage },
        AssertImpl.any.typeTransformation.failureHandlers.newExplanatory()
    )
}

fun Assert<AttachmentMessage>.withButtonAttachment(text: String, buttonTitle: String) =
    withButtonAttachment(text, listOf(buttonTitle))

fun Assert<AttachmentMessage>.withButtonAttachment(text: String, buttonTitles: List<String>) =
    property(AttachmentMessage::attachment)
        .property(Attachment::payload)
        .isA<ButtonPayload> {
            property(ButtonPayload::text).toBe(text)
            property(ButtonPayload::buttons) {
                expect(subject.map { button ->
                    when (button) {
                        is PostbackButton -> button.title
                        is UrlButton -> button.title
                        else -> null
                    }
                }).toBe(buttonTitles)

            }
        }

fun Assert<AttachmentMessage>.withGenericTemplateElement(
    index: Int,
    expectedTitle: String,
    subtitle: String? = null,
    buttonTitles: List<String>
) =
    property(AttachmentMessage::attachment)
        .property(Attachment::payload)
        .isA<GenericPayload> {
            property(GenericPayload::elements) {
                expect(subject[index]) {
                    property(Element::title).toBe(expectedTitle)
                    subtitle?.let { property(Element::subtitle).toBe(it) }
                    property(Element::buttons).notToBeNull {
                        expect(subject.map { button ->
                            when (button) {
                                is PostbackButton -> button.title
                                is UrlButton -> button.title
                                else -> null
                            }
                        }).toBe(buttonTitles)
                    }
                }

            }

        }

fun Assert<AttachmentMessage>.withTextQuickReplies(quickReplies: List<String>) = addAssertionsCreatedBy {
    expect(subject.quickReplies.orEmpty().filterIsInstance<TextQuickReply>().map { it.title })
        .toBe(quickReplies)
}