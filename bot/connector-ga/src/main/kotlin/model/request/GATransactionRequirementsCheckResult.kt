package ai.tock.bot.connector.ga.model.request


/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/TransactionDecisionValue#TransactionRequirementsCheckResult
 */
data class GATransactionRequirementsCheckResult(
    val resultType: GAResultType
) : GAArgumentValue(
    GAArgumentValueType.transactionRequirementsCheckResult
)

