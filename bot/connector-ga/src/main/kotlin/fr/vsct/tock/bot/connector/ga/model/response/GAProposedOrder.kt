package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/ProposedOrder
 */
data class GAProposedOrder(
        val id: String,
        val cart: GACart,
        val otherItems: List<GALineItem> ? = null,
        val image: GAImage,
        val termsOfServiceUrl: String,
        val totalPrice: GAPrice
)


