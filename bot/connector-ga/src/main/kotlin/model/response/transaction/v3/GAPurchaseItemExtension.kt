package ai.tock.bot.connector.ga.model.response.transaction.v3

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#PurchaseItemExtension
 */
data class GAPurchaseItemExtension(
    val status: GAPurchaseStatus = GAPurchaseStatus.PURCHASE_STATUS_UNSPECIFIED,
    val userVisibleStatusLabel: String,
    val type: GAPurchaseType = GAPurchaseType.PURCHASE_TYPE_UNSPECIFIED,
    val quantity: Int? = null,
    val itemOptions: List<GaItemOption>? = emptyList()
)

enum class GAPurchaseStatus{
    CONFIRMED,
    CREATED,
    DELIVERED,
    CANCELLED,
    PURCHASE_STATUS_UNSPECIFIED
}

enum class GAPurchaseType{
    PURCHASE_TYPE_UNSPECIFIED,
}