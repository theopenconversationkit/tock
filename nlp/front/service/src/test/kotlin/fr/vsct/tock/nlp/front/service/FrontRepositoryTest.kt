/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.front.service

import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import io.mockk.every
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 *
 */
class FrontRepositoryTest : AbstractTest() {

    val entityTypes = mutableListOf(EntityTypeDefinition("present"))

    @Before
    fun before() {
        every { context.config.getEntityTypes() } returns entityTypes
    }

    @After
    fun after() {
        FrontRepository.entityTypes.clear()
    }

    @Test
    fun entityTypeByName_shouldReloadBeforeFailing_whenNoEntityTypeIsFound() {
        with(context) {
            assertTrue(FrontRepository.entityTypeExists("present"))
            assertFalse(FrontRepository.entityTypeExists("notPresent"))

            entityTypes += EntityTypeDefinition("notPresent")

            assertEquals("notPresent", FrontRepository.entityTypeByName("notPresent")?.name)
        }
    }

    @Test
    fun entityTypeByName_shouldNotFail_whenNoEntityTypeIsFound() {
        with(context) {
            assertTrue(FrontRepository.entityTypeExists("present"))
            assertFalse(FrontRepository.entityTypeExists("notPresent"))

            assertEquals("present", FrontRepository.entityTypeByName("present")?.name)

            assertTrue(FrontRepository.entityTypeExists("present"))
            assertFalse(FrontRepository.entityTypeExists("notPresent"))

            //should return null
            assertNull(FrontRepository.entityTypeByName("notPresent"))
        }
    }
}