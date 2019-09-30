package ai.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/ActionType
 */
enum class GAActionType {
    UNKNOWN,
    VIEW_DETAILS,
    MODIFY,
    CANCEL,
    RETURN,
    EXCHANGE,
    EMAIL,
    CALL,
    REORDER,
    REVIEW,
    CUSTOMER_SERVICE
}