package ai.tock.bot.connector.ga.model.response.transaction.v3

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#PurchaseOrderExtension
 */
data class GAPurchaseOrderExtension(
    val status: GAPurchaseStatus = GAPurchaseStatus.PURCHASE_STATUS_UNSPECIFIED,
    val userVisibleStatusLabel: String,
    val type: GAPurchaseType = GAPurchaseType.PURCHASE_TYPE_UNSPECIFIED
)