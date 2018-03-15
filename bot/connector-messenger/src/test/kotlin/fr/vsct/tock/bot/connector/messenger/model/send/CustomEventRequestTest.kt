package fr.vsct.tock.bot.connector.messenger.model.send

import fr.vsct.tock.shared.jackson.mapper
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals

class CustomEventRequestTest {

    @Test
    fun `GIVEN a CustomEventRequest WHEN serializing to JSON THEN gets the expected result`() {
        val customEventName = "MyCustomEventName"
        val pageId = "0123456789"
        val userId = UUID.randomUUID().toString()
        val customEventRequest = CustomEventRequest(CustomEvent(customEventName), pageId, userId)
        val serialized = mapper.writeValueAsString(customEventRequest)
        assertEquals(
            "{\"custom_events\":[{\"_eventName\":\"$customEventName\"}],\"page_id\":\"$pageId\",\"page_scoped_user_id\":\"$userId\",\"event\":\"CUSTOM_APP_EVENTS\",\"advertiser_tracking_enabled\":0,\"application_tracking_enabled\":1,\"extinfo\":\"[\\\"mb1\\\"]\"}",
            serialized
        )
    }
}
