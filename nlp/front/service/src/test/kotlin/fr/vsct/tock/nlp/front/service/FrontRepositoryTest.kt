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

import com.nhaarman.mockito_kotlin.whenever
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
class FrontRepositoryTest : AbstractTest() {

    val entityTypes = mutableListOf(EntityTypeDefinition("present"))

    @Before
    fun before() {
        whenever(context.config.getEntityTypes()).thenReturn(entityTypes)
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

            assertEquals("notPresent", FrontRepository.entityTypeByName("notPresent").name)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun entityTypeByName_shouldFail_whenNoEntityTypeIsFound() {
        with(context) {
            assertTrue(FrontRepository.entityTypeExists("present"))
            assertFalse(FrontRepository.entityTypeExists("notPresent"))

            assertEquals("present", FrontRepository.entityTypeByName("present").name)

            assertTrue(FrontRepository.entityTypeExists("present"))
            assertFalse(FrontRepository.entityTypeExists("notPresent"))

            //thow exception
            FrontRepository.entityTypeByName("notPresent")
        }
    }
}