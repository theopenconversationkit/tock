package fr.vsct.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/PaymentType
 */
enum class GAPaymentType {
    PAYMENT_TYPE_UNSPECIFIED,
    PAYMENT_CARD,
    BANK,
    LOYALTY_PROGRAM,
    ON_FULFILLMENT,
    GIFT_CARD,
}