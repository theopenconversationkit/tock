package ai.tock.bot.engine.action

/**
 * ActionTag deals with type of message notification.
 */
enum class ActionNotificationType {
    reservationUpdate,
    issueResolution,
    transportationUpdate,
    newFeatureFunctionality,
    ticketUpdate,
    accountUpdate,
    paymentUpdate,
    personalFinanceUpdate
}