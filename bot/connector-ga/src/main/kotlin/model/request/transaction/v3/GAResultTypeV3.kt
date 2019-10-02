package ai.tock.bot.connector.ga.model.request.transaction.v3

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/ResultType
 */
enum class GAResultTypeV3 {
    RESULT_TYPE_UNSPECIFIED,
    CANNOT_TRANSACT,
    CAN_TRANSACT
}