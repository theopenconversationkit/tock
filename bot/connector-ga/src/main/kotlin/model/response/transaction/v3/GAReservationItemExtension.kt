package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.request.GALocation
import ai.tock.bot.connector.ga.model.response.GAImage

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order#ReservationItemExtension
 */
data class GAReservationItemExtension(
    val status: GAReservationStatus = GAReservationStatus.RESERVATION_STATUS_UNSPECIFIED,
    val userVisibleStatusLabel: String,
    val type: GAReservationType? = GAReservationType.RESERVATION_TYPE_UNSPECIFIED,
    val confirmationCode: String? = null,
    val reservationTime: GATime? = null,
    val userAcceptableTimeRange: GATime? = null,
    val partySize: Int? = null,
    val staffFacilitators: List<GAStaffFacilitator>? = null,
    val location: GALocation? = null
)

data class GATime(
    val timeIso8601: String
)

data class GAStaffFacilitator(
    val name: String?,
    val image: GAImage?
)

enum class GAReservationStatus{
    CONFIRMED,
    RESERVATION_STATUS_UNSPECIFIED,
    PENDING,
    CANCELLED,
    FULFILLED,
    CHANGE_REQUESTED,
    REJECTED
}

enum class GAReservationType{
    RESERVATION_TYPE_UNSPECIFIED
}