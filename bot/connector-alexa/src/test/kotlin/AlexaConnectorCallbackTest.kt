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

package ai.tock.bot.connector.alexa

import ai.tock.bot.definition.BotDefinition
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.I18nTranslator
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.raw
import com.amazon.speech.ui.PlainTextOutputSpeech
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class AlexaConnectorCallbackTest {

    @Test
    fun `sendResponse with no test returns unknown response`() {
        val botDefinition: BotDefinition = mockk()
        val controller: ConnectorController = mockk()
        val unknownAnswer = I18nLabelValue("", "", "", "label")
        val i18nTranslator: I18nTranslator = mockk()
        every { controller.botDefinition } returns botDefinition
        every { botDefinition.i18nTranslator(any(), any(), any()) } returns i18nTranslator
        every { botDefinition.defaultUnknownAnswer } returns unknownAnswer
        every { i18nTranslator.translate(any()) } returns unknownAnswer.raw
        val callback = AlexaConnectorCallback(
            "id",
            controller,
            AlexaTockMapper("id"),
            mockk(),
            mutableListOf()
        )

        callback.sendResponse()

        assertEquals("label", (callback.alexaResponse?.outputSpeech as? PlainTextOutputSpeech)?.text)
    }
}
