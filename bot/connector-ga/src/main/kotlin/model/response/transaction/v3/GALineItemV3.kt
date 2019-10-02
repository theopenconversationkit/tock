package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.response.GAImage

/**
* @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#LineItem
*/
data class GALineItemV3(
    val id: String,
    val name: String? = null,
    val priceAttributes: List<GAPriceAttribute>? = emptyList(),
    val image: GAImage? = null,
    val description: String? = null,
    val notes: List<String>? = emptyList(),
    val purchase: GAPurchaseItemExtension? = null,
    val reservation: GAReservationItemExtension? = null
)