package fr.vsct.tock.bot.connector.ga.model.request


/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValue
 */
data class GATransactionDecisionValue(
        val checkResult: GATransactionDecisionCheckResult,
        val userDecision: GATransactionUserDecision,
        val order: GAOrder,
        val deliveryAddress: GALocation?
): GAArgumentValue(GAArgumentValueType.transactionDecisionValue
)

