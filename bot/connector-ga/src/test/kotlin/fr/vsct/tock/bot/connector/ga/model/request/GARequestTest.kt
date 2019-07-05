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

package fr.vsct.tock.bot.connector.ga.model.request

import fr.vsct.tock.bot.connector.ga.model.request.GACapability.Companion.AUDIO_OUTPUT
import fr.vsct.tock.translator.UserInterfaceType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class GARequestTest {

    @Test
    fun getEventState_shouldReturnsVoiceAssistant_whenOnlyAudioCapability() {
        val request = GARequest(
            GAUser(),
            GADevice(),
            GASurface(listOf(GACapability(AUDIO_OUTPUT))),
            GAConversation(),
            emptyList()
        )

        assertEquals(UserInterfaceType.voiceAssistant, request.getEventState().userInterface)
    }
}