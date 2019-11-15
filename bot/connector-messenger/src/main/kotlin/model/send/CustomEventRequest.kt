package ai.tock.bot.connector.messenger.model.send

import com.fasterxml.jackson.annotation.JsonProperty
import ai.tock.shared.jackson.mapper

data class CustomEventRequest(
    @JsonProperty("custom_events") val customEvents: List<CustomEvent>,
    @JsonProperty("page_id") val pageId: String,
    @JsonProperty("page_scoped_user_id") val pageScopedUserId: String,
    val event: String = "CUSTOM_APP_EVENTS",
    @JsonProperty("advertiser_tracking_enabled") val advertiserTrackingEnabled: Short = 0,
    @JsonProperty("application_tracking_enabled") val applicationTrackingEnabled: Short = 1,
    val extinfo: String = mapper.writeValueAsString(listOf("mb1"))
) {
    constructor(
        customEvent: CustomEvent,
        pageId: String,
        pageScopedUserId: String,
        event: String = "CUSTOM_APP_EVENTS",
        advertiserTrackingEnabled: Short = 0,
        applicationTrackingEnabled: Short = 1,
        extinfo: String = mapper.writeValueAsString(listOf("mb1"))
    ) : this(
        listOf(customEvent),
        pageId,
        pageScopedUserId,
        event,
        advertiserTrackingEnabled,
        applicationTrackingEnabled,
        extinfo
    )
}
