package ai.tock.bot.jackson

import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ActionVisibilityDeserializationTest {

    @Test
    fun `GIVEN lower case visibility THEN deserialization is ok`() {
        val request = """{"lastAnswer":false,"priority":"normal","visibility":"public","replyMessage":"UNKNOWN","quoteMessage":"UNKNOWN"}"""
        assertEquals(ActionVisibility.PUBLIC, mapper.readValue<ActionMetadata>(request).visibility)
    }

    @Test
    fun `GIVEN upper case visibility THEN deserialization is ok`() {
        val request = """{"lastAnswer":false,"priority":"normal","visibility":"PUBLIC","replyMessage":"UNKNOWN","quoteMessage":"UNKNOWN"}"""
        assertEquals(ActionVisibility.PUBLIC, mapper.readValue<ActionMetadata>(request).visibility)
    }
}