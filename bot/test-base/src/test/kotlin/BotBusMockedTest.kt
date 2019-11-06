import ai.tock.bot.engine.BotBus
import ai.tock.bot.test.mockTockCommon
import ai.tock.translator.raw
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BotBusMockedTest {

    private val botBus = mockk<BotBus>()

    @BeforeEach
    internal fun setUp() {
        mockTockCommon(botBus)
    }

    @Test
    fun shouldTranslateString() {
        assertEquals( "Tock - The (Best) Open Conversation Kit", botBus.translate("Tock - The (Best) Open Conversation Kit").toString())
    }

    @Test
    fun shouldTranslateTranslatedCharSequence() {
        assertEquals("Tock - The (Best) Open Conversation Kit", botBus.translate("Tock - The (Best) Open Conversation Kit".raw).toString())
    }

}