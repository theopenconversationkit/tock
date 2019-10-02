package ai.tock.bot.connector.ga.model.request.transaction.v3

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/TransactionDecision
 */
enum class GATransactionDecision{
    TRANSACTION_DECISION_UNSPECIFIED,
    USER_CANNOT_TRANSACT,
    ORDER_ACCEPTED,
    ORDER_REJECTED,
    DELIVERY_ADDRESS_UPDATED,
    CART_CHANGE_REQUESTED
}