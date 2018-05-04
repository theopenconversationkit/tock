/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.alexa

import com.amazon.speech.ui.PlainTextOutputSpeech
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.I18nTranslator
import fr.vsct.tock.translator.I18nLabelValue
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
        every { i18nTranslator.translate(any<I18nLabelValue>()) } returns unknownAnswer
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