package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/LineItemUpdate
 */
data class GALineItemUpdate(
        val orderState: GAOrderState,
        val price: GAPrice? = null,
        val reason: String? = null
)