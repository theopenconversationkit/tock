package fr.vsct.tock.bot.connector.ga.model.request

import fr.vsct.tock.bot.connector.ga.model.response.GAProposedOrder

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValue#Order
 */
data class GAOrder(
        val finalOrder: GAProposedOrder,
        val googleOrderId: String,
        val orderDate: String,
        val paymentInfo: GAPaymentInfo,
        val actionOrderId: String,
        val customerInfo: GACustomerInfo
)