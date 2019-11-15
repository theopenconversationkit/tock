package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/PaymentOptions
 */
data class GAPaymentOptions(
        val googleProvidedOptions:GAGoogleProvidedPaymentOptions?=null,
        val actionProvidedOptions: GAActionProvidedPaymentOptions? = null
)