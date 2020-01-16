package ai.tock.analytics.chatbase.model

import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


internal class StatusTest {

    @Test
    fun `serialize deserialize response`() {
        val response = Response("messageId", Status.OK)
        val s = mapper.writeValueAsString(response)
        assertThat(mapper.readValue<Response>(s)).isEqualTo(response)
    }
}