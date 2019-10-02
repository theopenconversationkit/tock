package ai.tock.bot.connector.ga.model.response.transaction.v3

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#Contents
 */
data class GAContents (
    val lineItems : List<GALineItemV3>? = null
)