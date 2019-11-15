package ai.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionUserDecision
 */
enum class GATransactionUserDecision {
    UNKNOWN_USER_DECISION,
    ORDER_ACCEPTED,
    ORDER_REJECTED,
    DELIVERY_ADDRESS_UPDATED,
    CART_CHANGE_REQUESTED
}