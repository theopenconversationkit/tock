package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#Action
 */
data class GAAction(
        val type: GAActionType,
        val button: GAButton
)