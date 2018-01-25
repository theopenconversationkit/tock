package fr.vsct.tock.bot.connector.ga.model.response


/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/PaymentOptions#ActionProvidedPaymentOptions
 */
data class GAActionProvidedPaymentOptions(
    val paymentType: GaPaymentType,
    val displayName: String
)