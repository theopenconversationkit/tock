package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/PaymentOptions#PaymentMethodTokenizationParameters
 */
data class GAPaymentMethodTokenizationParameters(
        val tokenizationType: GAPaymentMethodTokenizationType
)