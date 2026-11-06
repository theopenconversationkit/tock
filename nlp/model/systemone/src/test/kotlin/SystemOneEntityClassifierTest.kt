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

import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.model.EntityCallContextForIntent
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.systemone.SystemOneEntityClassifier
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals

class SystemOneEntityClassifierTest {
    @Test
    fun `classifyEntities always returns an empty list - clef has no entity extraction capability`() {
        val model = EntityModelHolder(nativeModel = "clef", configuration = NlpApplicationConfiguration())
        val classifier = SystemOneEntityClassifier(model)

        val context =
            EntityCallContextForIntent(
                Intent("app:greetings", emptyList()),
                Locale.ENGLISH,
                NlpEngineType.systemone,
                "app",
                ZonedDateTime.now(),
            )

        val result = classifier.classifyEntities(context, "some text", arrayOf("some", "text"))

        assertEquals(emptyList(), result)
    }
}
