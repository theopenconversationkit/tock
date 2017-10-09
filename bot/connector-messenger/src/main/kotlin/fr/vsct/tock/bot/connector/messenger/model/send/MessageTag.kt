package fr.vsct.tock.bot.connector.messenger.model.send

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionNotificationType


enum class MessageTag {
    SHIPPING_UPDATE,
    RESERVATION_UPDATE,
    ISSUE_RESOLUTION,
    APPOINTMENT_UPDATE,
    GAME_EVENT,
    TRANSPORTATION_UPDATE,
    FEATURE_FUNCTIONALITY_UPDATE,
    TICKET_UPDATE,
    ACCOUNT_UPDATE,
    PAYMENT_UPDATE,
    PERSONAL_FINANCE_UPDATE;

    companion object {
        fun toMessageTag(action: Action): MessageTag? {
            return when (action.metadata.notificationType) {
                ActionNotificationType.transportationUpdate -> TRANSPORTATION_UPDATE
                ActionNotificationType.issueResolution -> ISSUE_RESOLUTION
                ActionNotificationType.newFeatureFunctionality -> FEATURE_FUNCTIONALITY_UPDATE
                ActionNotificationType.reservationUpdate -> RESERVATION_UPDATE
                ActionNotificationType.accountUpdate -> ACCOUNT_UPDATE
                ActionNotificationType.paymentUpdate -> PAYMENT_UPDATE
                ActionNotificationType.personalFinanceUpdate -> PERSONAL_FINANCE_UPDATE
                else -> null
            }
        }
    }
}