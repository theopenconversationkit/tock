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

import ai.tock.nlp.core.Application
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.systemone.SystemOneConfiguration
import ai.tock.nlp.systemone.SystemOneNlpModelBuilder
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SystemOneNlpModelBuilderTest {
    @Test
    fun `buildIntentModel captures only the model variant - never the API token - as the native model`() {
        val application = Application("app", listOf(Intent("app:greetings", emptyList())), setOf(Locale.ENGLISH))
        val context = IntentContext(application, Locale.ENGLISH, NlpEngineType.systemone)

        val holder = SystemOneNlpModelBuilder.buildIntentModel(context, NlpApplicationConfiguration(), emptyList())

        assertTrue(holder.nativeModel is String)
        assertEquals(SystemOneConfiguration().model, holder.nativeModel)
        assertEquals(application, holder.application)
    }

    @Test
    fun `defaultNlpApplicationConfiguration only needs an application configuration - no local training`() {
        val conf = SystemOneNlpModelBuilder.defaultNlpApplicationConfiguration()

        assertEquals(false, conf.hasTokenizerConfiguration)
        assertEquals(true, conf.hasIntentConfiguration)
        assertEquals(false, conf.hasEntityConfiguration)
        assertEquals(true, conf.hasApplicationConfiguration)
    }
}
