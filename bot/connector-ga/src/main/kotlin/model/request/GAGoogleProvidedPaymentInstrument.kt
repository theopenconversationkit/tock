package ai.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValue#GoogleProvidedPaymentInstrument
 */
data class GAGoogleProvidedPaymentInstrument(
        val instrumentToken: String
)