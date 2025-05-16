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

import ai.tock.nlp.front.shared.parser.IntentQualifier
import ai.tock.nlp.front.shared.parser.QueryState
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 *
 */
class ParserRequestDataTest : AbstractTest() {

    @Test
    fun isStateEnabledForIntentId_shouldReturnTrue_whenIntentIsEnabledAndIntentSupportState() {
        val testState = "testState"
        val data = ParserRequestData(
            app,
            parseQuery.copy(state = QueryState(setOf(testState))),
            defaultClassifiedSentence,
            setOf(IntentQualifier(defaultIntentName, 0.2)),
            listOf(defaultIntentDefinition)
        )
        assertTrue(data.isStateEnabledForIntentId(defaultIntentDefinition._id))
    }
}
