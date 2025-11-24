/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    val location: GALocation? = null,
)

data class GATime(
    val timeIso8601: String,
)

data class GAStaffFacilitator(
    val name: String?,
    val image: GAImage?,
)

enum class GAReservationStatus {
    CONFIRMED,
    RESERVATION_STATUS_UNSPECIFIED,
    PENDING,
    CANCELLED,
    FULFILLED,
    CHANGE_REQUESTED,
    REJECTED,
}

enum class GAReservationType {
    RESERVATION_TYPE_UNSPECIFIED,
}
