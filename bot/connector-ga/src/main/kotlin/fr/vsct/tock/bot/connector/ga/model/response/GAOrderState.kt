package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/OrderState
 */
data class GAOrderState(
        val state: GAState,
        val label: String
)