package fr.vsct.tock.bot.connector.ga.model.request

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValue#CustomerInfo
 */
data class GACustomerInfo(
        val email: String
)