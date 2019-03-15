package fr.vsct.tock.bot.connector.teams.messages

import com.microsoft.bot.schema.models.CardAction
import com.microsoft.bot.schema.models.CardImage

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

}

class TeamsCardAction(
    val actionTitle: String,
    val buttons: List<CardAction>): TeamsBotMessage(null) {
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