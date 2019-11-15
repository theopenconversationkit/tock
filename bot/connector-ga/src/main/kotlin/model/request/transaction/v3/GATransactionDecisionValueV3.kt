package ai.tock.bot.connector.ga.model.request.transaction.v3

import ai.tock.bot.connector.ga.model.request.GAArgumentValue
import ai.tock.bot.connector.ga.model.request.GAArgumentValueType
import ai.tock.bot.connector.ga.model.request.GALocation
import ai.tock.bot.connector.ga.model.response.transaction.v3.GAOrder

/**
* @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/TransactionDecisionValue
*/
data class GATransactionDecisionValueV3(
    val transactionDecision: GATransactionDecision,
    val order: GAOrder?,
    val deliveryAddress: GALocation?
): GAArgumentValue(GAArgumentValueType.transactionRequirementsCheckResultV3)