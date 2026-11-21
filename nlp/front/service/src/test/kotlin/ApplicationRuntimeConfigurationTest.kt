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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.Intent.Companion.RAG_EXCLUDED_INTENT_NAME
import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.asRuntimeConfiguration
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.InternalTockApi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.litote.kmongo.toId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(InternalTockApi::class)
class ApplicationRuntimeConfigurationTest {
    @Test
    fun `GIVEN a sentence with a qualifier WHEN the runtime configuration saves it THEN the qualifier is kept, as the complete configuration does`() {
        val configuration: ApplicationConfiguration = mockk(relaxed = true)
        val sentence: ClassifiedSentence = mockk()
        every { sentence.qualifier } returns "julien"

        configuration.asRuntimeConfiguration().save(sentence)

        verify { configuration.save(sentence, "julien") }
    }

    @Test
    fun `GIVEN the reserved intents WHEN their id is looked up THEN no intent is read`() {
        val intent: IntentDefinition = mockk { every { _id } returns "id".toId() }
        val find: (String, String) -> IntentDefinition? = { namespace, name -> intent.takeIf { namespace == "ns" && name == "greetings" } }

        assertEquals(UNKNOWN_INTENT_NAME.toId(), intentIdByQualifiedName(UNKNOWN_INTENT_NAME) { _, _ -> error("not read") })
        assertEquals(RAG_EXCLUDED_INTENT_NAME.toId(), intentIdByQualifiedName(RAG_EXCLUDED_INTENT_NAME) { _, _ -> error("not read") })
        assertEquals("id".toId(), intentIdByQualifiedName("ns:greetings", find))
        assertNull(intentIdByQualifiedName("ns:other", find))
    }
}
