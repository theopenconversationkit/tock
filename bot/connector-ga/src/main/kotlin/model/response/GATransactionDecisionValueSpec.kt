package ai.tock.bot.connector.ga.model.response

import ai.tock.bot.connector.ga.model.GAIntent

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValueSpec
 */
data class GATransactionDecisionValueSpec(
        val proposedOrder: GAProposedOrder,
        val orderOptions: GAOrderOptions,
        val paymentOptions: GAPaymentOptions? = null
): GAInputValueData(GAIntent.transactionDecision.type!!)