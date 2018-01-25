package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/ProposedOrder#SubLine
 */
data class GASubLine(
        val lineItem: GALineItem,
        val note: String
)