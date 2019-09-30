package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/PaymentType
 */
enum class GaPaymentType{

    PAYMENT_TYPE_UNSPECIFIED,
    PAYMENT_CARD,
    BANK,
    LOYALTY_PROGRAM,
    ON_FULFILLMENT,
    GIFT_CARD

}