package ai.tock.bot.connector.ga.model.request.transaction.v3

import ai.tock.bot.connector.ga.model.request.GAArgumentValue
import ai.tock.bot.connector.ga.model.request.GAArgumentValueType
import ai.tock.bot.connector.ga.model.request.transaction.v3.GAResultTypeV3


/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValue#TransactionRequirementsCheckResult
 */
data class GATransactionRequirementsCheckResultV3(
    val resultType: GAResultTypeV3
) : GAArgumentValue(
    GAArgumentValueType.transactionRequirementsCheckResult
)

