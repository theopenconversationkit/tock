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

package ai.tock.nlp.core

import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

class BuildContextTest {
    private val application = Application("test", emptyList(), emptySet())

    @Test
    fun `entityEngineType defaults to engineType when not specified`() {
        val context = BuildContext(application, Locale.US, NlpEngineType.rasa)
        assertEquals(NlpEngineType.rasa, context.engineType)
        assertEquals(NlpEngineType.rasa, context.entityEngineType)
        assertEquals(false, context.onlyIfNotExists)
    }

    @Test
    fun `entityEngineType can be overridden independently from engineType`() {
        val context = BuildContext(application, Locale.US, NlpEngineType.rasa, NlpEngineType.opennlp, true)
        assertEquals(NlpEngineType.rasa, context.engineType)
        assertEquals(NlpEngineType.opennlp, context.entityEngineType)
        assertEquals(true, context.onlyIfNotExists)
    }
}
