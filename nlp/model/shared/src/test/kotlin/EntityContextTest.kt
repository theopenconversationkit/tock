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

package ai.tock.nlp.model

import ai.tock.nlp.core.Application
import ai.tock.nlp.core.BuildContext
import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpEngineType
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

class EntityContextTest {
    private val application = Application("test", emptyList(), emptySet())
    private val intent = Intent("test", emptyList())
    private val entity = Entity(EntityType("test:entity"), "role")

    @Test
    fun `EntityCallContextForIntent uses entityEngineType, not engineType, from a CallContext`() {
        val callContext = CallContext(application, Locale.US, NlpEngineType.rasa, NlpEngineType.opennlp)
        val entityContext = EntityCallContextForIntent(callContext, intent)
        assertEquals(NlpEngineType.opennlp, entityContext.engineType)
    }

    @Test
    fun `EntityCallContextForIntent falls back to engineType when entityEngineType is not overridden`() {
        val callContext = CallContext(application, Locale.US, NlpEngineType.rasa)
        val entityContext = EntityCallContextForIntent(callContext, intent)
        assertEquals(NlpEngineType.rasa, entityContext.engineType)
    }

    @Test
    fun `EntityCallContextForEntity uses entityEngineType, not engineType, from a CallContext`() {
        val callContext = CallContext(application, Locale.US, NlpEngineType.rasa, NlpEngineType.opennlp)
        val entityContext = EntityCallContextForEntity(callContext, entity)
        assertEquals(NlpEngineType.opennlp, entityContext.engineType)
    }

    @Test
    fun `EntityBuildContextForIntent uses entityEngineType, not engineType, from a BuildContext`() {
        val buildContext = BuildContext(application, Locale.US, NlpEngineType.rasa, NlpEngineType.opennlp)
        val entityContext = EntityBuildContextForIntent(buildContext, intent)
        assertEquals(NlpEngineType.opennlp, entityContext.engineType)
    }

    @Test
    fun `EntityBuildContextForIntent falls back to engineType when entityEngineType is not overridden`() {
        val buildContext = BuildContext(application, Locale.US, NlpEngineType.rasa)
        val entityContext = EntityBuildContextForIntent(buildContext, intent)
        assertEquals(NlpEngineType.rasa, entityContext.engineType)
    }

    @Test
    fun `EntityBuildContextForSubEntities uses entityEngineType, not engineType, from a BuildContext`() {
        val buildContext = BuildContext(application, Locale.US, NlpEngineType.rasa, NlpEngineType.opennlp)
        val entityContext = EntityBuildContextForSubEntities(buildContext, entity.entityType)
        assertEquals(NlpEngineType.opennlp, entityContext.engineType)
    }
}
