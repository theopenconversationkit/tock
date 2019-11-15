package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.request.GALocation

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/TicketItemExtension
 */
data class GATicketItemExtension(
    val ticketEvent: TicketEvent,
    val quantity: Int?,
    val ticketType: String?
)

data class TicketEvent(
    val type: TicketEventType = TicketEventType.EVENT_TYPE_UNKNOWN,
    val name: String,
    val description: String?,
    val url: String?,
    val location: GALocation?

)

enum class TicketEventType{
    EVENT_TYPE_UNKNOWN,
}