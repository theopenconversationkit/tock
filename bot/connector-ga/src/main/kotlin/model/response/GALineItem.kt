package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/ProposedOrder#LineItem
 */
data class GALineItem(
        val id: String,
        val name: String,
        val type: GALineItemType,
        val quantity: Int,
        val description: String,
        val image: GAImage,
        val price: GAPrice,
        val subLines: List<GASubLine> ? = null,
        val offerId: String ? = null
)