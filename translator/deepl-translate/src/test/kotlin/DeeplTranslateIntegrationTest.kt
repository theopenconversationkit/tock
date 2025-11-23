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

package ai.tock.translator.deepl

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

/**
 * All these tests are disabled because it uses Deepl pro api that can be expensive
 */
class DeeplTranslateIntegrationTest {
    private val deeplTranslatorEngine = DeeplTranslatorEngine(OkHttpDeeplClient())

    @Test
    @Disabled
    fun simpleTest() {
        val result =
            deeplTranslatorEngine.translate(
                "Bonjour, je voudrais me rendre à New-York Mardi prochain",
                Locale.FRENCH,
                Locale.ENGLISH,
            )
        assertEquals("Hello, I would like to go to New York next Tuesday.", result)
    }

    @Test
    @Disabled
    fun testWithEmoticonAndAntislash() {
        val result =
            deeplTranslatorEngine.translate(
                "Bonjour, je suis l'Agent virtuel SNCF Voyageurs! \uD83E\uDD16\n" +
                    "Je vous informe sur l'état du trafic en temps réel.\n" +
                    "Dites-moi par exemple \"Mon train 6111 est-il à l'heure ?\", \"Aller à Saint-Lazare\", \"Prochains départs Gare de Lyon\" ...",
                Locale.FRENCH,
                Locale.ENGLISH,
            )

        assertEquals(
            "Hello, I'm the SNCF Voyageurs Virtual Agent! \uD83E\uDD16\n" +
                "I inform you about traffic conditions in real time.\n" +
                "Tell me for example \"Is my train 6111 on time?\", \"Going to Saint-Lazare\", \"Next departures Gare de Lyon\" ...",
            result,
        )
    }

    @Test
    @Disabled
    fun testWithParameters() {
        val result =
            deeplTranslatorEngine.translate(
                "Bonjour, je voudrais me rendre à {:city} {:date}",
                Locale.FRENCH,
                Locale.GERMAN,
            )
        assertEquals("Hallo, ich würde gerne nach {:city} {:date} fahren.", result)
    }

    @Test
    @Disabled
    fun testWithHTML() {
        val result =
            deeplTranslatorEngine.translate(
                "Bonjour, je voudrais me rendre à Paris <br><br/> demain soir",
                Locale.FRENCH,
                Locale.GERMAN,
            )
        assertEquals("Hallo, ich möchte morgen Abend nach Paris <br><br/> fahren", result)
    }
}
