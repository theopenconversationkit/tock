/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.connector.ga

import ai.tock.translator.TextAndVoiceTranslatedString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class ExtensionsTest {

    @Test
    fun simpleResponseWithoutTranslate_shouldReturnsSpeechToTextWithoutEmoji() {
        val r = simpleResponseWithoutTranslate("a:)d:(")
        assertEquals("ad", r.textToSpeech)
    }

    @Test
    fun simpleResponseWithoutTranslate_shouldReturnsSSMLWithoutEmoji() {
        val r = simpleResponseWithoutTranslate("<speak>a:)d:(</speak>")
        assertEquals("<speak>ad</speak>", r.ssml)
    }

    @Test
    fun simpleResponseWithoutTranslate_shouldReturnsDisplayTextWithEmoji() {
        val r = simpleResponseWithoutTranslate(TextAndVoiceTranslatedString("a:)d:(", "a:)d:("))
        assertEquals("ad", r.textToSpeech)
        assertEquals("a:)d:(", r.displayText)
    }

    @Test
    fun simpleResponseWithoutTranslate_shouldReturnsDash_whenThereIsOnlyEmojisInTheString() {
        val r = simpleResponseWithoutTranslate(":):(")
        assertEquals(" - ", r.textToSpeech)
    }

    @Test
    fun simpleResponseWithoutTranslate_shouldReturnsSameWording_whenWordingContainsColonPlus3() {
        val r = simpleResponseWithoutTranslate("08:37")
        assertEquals("08:37", r.textToSpeech)
    }

    @Test
    fun simpleResponseWithoutTranslate_shouldReturnsSameWording_whenWordingContainsColonPlus0() {
        val r = simpleResponseWithoutTranslate("08:07")
        assertEquals("08:07", r.textToSpeech)
    }

    @Test
    fun simpleResponseWithoutTranslate_shouldReturnsSameWording_whenWordingContainsLinkUrl() {
        val r = simpleResponseWithoutTranslate("https://oui.sncf")
        assertEquals("https://oui.sncf", r.textToSpeech)
    }
}