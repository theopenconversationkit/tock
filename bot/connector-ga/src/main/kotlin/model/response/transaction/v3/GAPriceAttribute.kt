package ai.tock.bot.connector.ga.model.response.transaction.v3

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#PriceAttribute
 */
data class GAPriceAttribute (
    val type: GAMoneyType,
    val name: String,
    val state: GAPriceState,
    val taxIncluded: Boolean?,
    val amount: GAMoneyV3?
)

enum class GAMoneyType{
    TYPE_UNSPECIFIED,
    REGULAR,
    DISCOUNT,
    TAX,
    DELIVERY,
    SUBTOTAL,
    FEE,
    GRATUITY,
    TOTAL
}

enum class GAPriceState{
    STATE_UNSPECIFIED,
    ESTIMATE,
    ACTUAL
}

data class GAPriceMoney(
    val currencyCode: String,
    val amountInMicros: String
)