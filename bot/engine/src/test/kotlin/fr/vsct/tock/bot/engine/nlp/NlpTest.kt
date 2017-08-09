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

package fr.vsct.tock.bot.engine.nlp

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import fr.vsct.tock.bot.engine.BotEngineTest
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.NextUserActionState
import fr.vsct.tock.nlp.api.client.model.NlpIntentQualifier
import org.junit.Test

/**
 *
 */
class NlpTest : BotEngineTest() {

    @Test
    fun parseSentence_shouldCallNlpClientParse_whenExpectedIntentIsNullInDialogState() {
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)
        verify(nlpClient).parse(any())
        verify(nlpClient, never()).parseIntentEntities(any())
    }

    @Test
    fun parseSentence_shouldCallNlpClientParseIntentEntities_whenIntentsQualifiersIsNotNullInDialogState() {
        dialog.state.nextActionState = NextUserActionState(listOf(NlpIntentQualifier("test2")))
        Nlp().parseSentence(userAction as SendSentence, userTimeline, dialog, connectorController, botDefinition)
        verify(nlpClient).parseIntentEntities(any())
        verify(nlpClient, never()).parse(any())
    }
}