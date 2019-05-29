package fr.vsct.tock.bot.connector.teams.messages

import com.microsoft.bot.schema.models.CardAction
import com.microsoft.bot.schema.models.CardImage
import fr.vsct.tock.bot.connector.teams.teamsConnectorType
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.message.Attachment
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.GenericMessage
import fr.vsct.tock.shared.mapNotNullValues

class TeamsBotTextMessage(text: String) : TeamsBotMessage(text) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TeamsBotTextMessage) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun toString(): String {
        return text ?: ""
    }

    override fun toGenericMessage(): GenericMessage {
        return GenericMessage(
            connectorType = connectorType,
            texts = mapOf("text" to toString())
        )
    }
}

class TeamsHeroCard(
    val title: String,
    val subtitle: String?,
    val attachmentContent: String,
    val images: List<CardImage>?,
    val buttons: List<CardAction>?,
    val tap: CardAction?
): TeamsBotMessage(null) {

    override fun toString(): String {
        val images = images?.map {  it.url() } ?: ""
        val buttons = buttons?.map { it.value() } ?: ""
        return "TeamsHeroCard(" +
            "title=$title, " +
            "subtitle=$subtitle, " +
            "attachmentContent=$attachmentContent, " +
            "images=$images, " +
            "buttons=$buttons, " +
            "tap=${tap?.value()})"
    }

    override fun toGenericMessage(): GenericMessage {
        return GenericMessage(
            connectorType = connectorType,
            texts = mapNotNullValues(
                "title" to title,
                "subtitle" to subtitle,
                "attachmentContent" to attachmentContent
            ),
            choices = buttons?.map {
                Choice(intentName = it.text()?.toString() ?: it.title(),
                    parameters = mapNotNullValues(
                        "title" to it.title()?.toString(),
                        "value" to it.value()?.toString(),
                        "displayText" to it.displayText(),
                        "text" to it.text(),
                        "type" to it.type().toString()
                    )
                )
            } ?: emptyList(),
            attachments = images?.map {
                Attachment(
                    url = it.url(),
                    type = SendAttachment.AttachmentType.image
                )
            } ?: emptyList()
        )
    }

}

class TeamsCardAction(
    val actionTitle: String,
    val buttons: List<CardAction>): TeamsBotMessage(null) {

    override fun toGenericMessage(): GenericMessage? {
        return GenericMessage(
            connectorType = connectorType,
            texts = mapNotNullValues("title" to actionTitle),
            choices = buttons.map {
                Choice(intentName = it.text()?.toString() ?: it.title(),
                    parameters = mapNotNullValues(
                        "title" to it.title()?.toString(),
                        "value" to it.value()?.toString(),
                        "displayText" to it.displayText(),
                        "text" to it.text(),
                        "type" to it.type().toString()
                    )
                )
            }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TeamsCardAction) return false
        if (!super.equals(other)) return false

        if (actionTitle != other.actionTitle) return false
        if (buttons != other.buttons) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + actionTitle.hashCode()
        result = 31 * result + buttons.hashCode()
        return result
    }

    override fun toString(): String {
        val allValues = buttons.groupBy { it.title() }.mapValues { it.value[0].value() }
        return "TeamsCardAction(actionTitle='$actionTitle', buttons=$allValues)"
    }
}