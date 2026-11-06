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

package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.NlpEngineType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApplicationDefinitionTest {
    @Test
    fun `effectiveEntityNlpEngineType falls back to nlpEngineType when entityNlpEngineType is not set`() {
        val app = ApplicationDefinition(name = "a", namespace = "b", nlpEngineType = NlpEngineType.rasa)
        assertNull(app.entityNlpEngineType)
        assertEquals(NlpEngineType.rasa, app.effectiveEntityNlpEngineType)
    }

    @Test
    fun `effectiveEntityNlpEngineType uses entityNlpEngineType when set, independently from nlpEngineType`() {
        val app =
            ApplicationDefinition(
                name = "a",
                namespace = "b",
                nlpEngineType = NlpEngineType.rasa,
                entityNlpEngineType = NlpEngineType.opennlp,
            )
        assertEquals(NlpEngineType.opennlp, app.entityNlpEngineType)
        assertEquals(NlpEngineType.opennlp, app.effectiveEntityNlpEngineType)
    }
}
