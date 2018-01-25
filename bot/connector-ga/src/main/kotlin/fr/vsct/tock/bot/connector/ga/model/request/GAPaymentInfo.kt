package fr.vsct.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValue#PaymentInfo
 */
data class GAPaymentInfo(
        val paymentType: GAPaymentType,
        val displayName: String,
        val googleProvidedPaymentInstrument: GAGoogleProvidedPaymentInstrument
)