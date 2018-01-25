package fr.vsct.tock.bot.connector.ga.model.request

import fr.vsct.tock.bot.connector.ga.request.GAResultType


/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValue#TransactionRequirementsCheckResult
 */
data class GATransactionRequirementsCheckResult(
        val resultType: GAResultType
) : GAArgumentValue(GAArgumentValueType.transactionRequirementsCheckResult
)

