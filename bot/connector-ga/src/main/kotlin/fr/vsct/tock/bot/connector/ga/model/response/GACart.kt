package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/ProposedOrder#Cart
 */
data class GACart(
        val id: String,
        val merchant: GAMerchant? = null,
        val lineItems: List<GALineItem>,
        val otherItems: List<GALineItem> ? = null,
        val notes: String
)