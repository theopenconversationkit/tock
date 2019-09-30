package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/PaymentOptions#GoogleProvidedPaymentOptions
 */
data class GAGoogleProvidedPaymentOptions(
        val tokenizationParameters:GAPaymentMethodTokenizationParameters,
        val supportedCardNetworks: Set<GACardNetwork>,
        val prepaidCardDisallowed:Boolean
)