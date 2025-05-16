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

import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import io.mockk.every
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 *
 */
class ConfigurationRepositoryTest : AbstractTest() {

    val entityTypes = mutableListOf(EntityTypeDefinition("present"))

    @BeforeEach
    fun before() {
        every { context.entityTypeDefinitionDAO.getEntityTypes() } returns entityTypes
        ConfigurationRepository.initRepository()
    }

    @AfterEach
    fun after() {
        ConfigurationRepository.refreshEntityTypes()
    }

    @Test
    fun entityTypeByName_shouldReloadBeforeFailing_whenNoEntityTypeIsFound() {
        with(context) {
            assertTrue(ConfigurationRepository.entityTypeExists("present"))
            assertFalse(ConfigurationRepository.entityTypeExists("notPresent"))

            entityTypes += EntityTypeDefinition("notPresent")

            assertEquals("notPresent", ConfigurationRepository.entityTypeByName("notPresent")?.name)
        }
    }

    @Test
    fun entityTypeByName_shouldNotFail_whenNoEntityTypeIsFound() {
        with(context) {
            assertTrue(ConfigurationRepository.entityTypeExists("present"))
            assertFalse(ConfigurationRepository.entityTypeExists("notPresent"))

            assertEquals("present", ConfigurationRepository.entityTypeByName("present")?.name)

            assertTrue(ConfigurationRepository.entityTypeExists("present"))
            assertFalse(ConfigurationRepository.entityTypeExists("notPresent"))

            // should return null
            assertNull(ConfigurationRepository.entityTypeByName("notPresent"))
        }
    }
}
