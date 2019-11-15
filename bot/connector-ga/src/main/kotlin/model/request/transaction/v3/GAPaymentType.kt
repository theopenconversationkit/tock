package ai.tock.bot.connector.ga.model.request.transaction.v3

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#PaymentType
 */
enum class GAPaymentType {
    PAYMENT_TYPE_UNSPECIFIED,
    PAYMENT_CARD,
    BANK,
    LOYALTY_PROGRAM,
    ON_FULFILLMENT,
    GIFT_CARD,
}