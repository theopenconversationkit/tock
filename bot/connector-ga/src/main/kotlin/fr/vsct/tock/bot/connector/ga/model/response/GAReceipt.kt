package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#Receipt
 */
data class GAReceipt(
        val confirmedActionOrderId: String,
        val userVisibleOrderId: String
)