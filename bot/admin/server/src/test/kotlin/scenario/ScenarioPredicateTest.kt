/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin.scenario

import ai.tock.shared.vertx.ConflictException
import ai.tock.shared.vertx.InternalServerException
import ai.tock.shared.vertx.NotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ScenarioPredicateTest {

    private val ID1 = "id_test_1"
    private val ID2 = "id_test_2"

    @Test
    fun `GUIVEN scenario whith no id WHEN checkToCreate THEN no exception throw`() {
        val scenario = createScenarioForId(null)
        assertDoesNotThrow { scenario.checkToCreate() }
    }

    @Test
    fun `GUIVEN scenario with id WHEN checkToCreate THEN exception is throw`() {
        val scenario = createScenarioForId(ID1)
        assertThrows<ConflictException> { scenario.checkToCreate() }
    }

    @Test
    fun `GUIVEN scenario id same as uri WHEN checkToUpdate THEN no exception throw`() {
        val scenario = createScenarioForId(ID1)
        assertDoesNotThrow { scenario.checkToUpdate(ID1) }
    }

    @Test
    fun `GUIVEN scenario id null WHEN checkToUpdate THEN exception is throw`() {
        val scenario = createScenarioForId(null)
        assertThrows<ConflictException> { scenario.checkToUpdate(ID1) }
    }

    @Test
    fun `GUIVEN scenario id different as uri WHEN checkToUpdate THEN exception is throw`() {
        val scenario = createScenarioForId(ID2)
        assertThrows<ConflictException> { scenario.checkToUpdate(ID1) }
    }

    @Test
    fun `GUIVEN scenario is not null WHEN checkIsNotNullForId THEN no exception is throw`() {
        val scenario = createScenarioForId(null)
        assertDoesNotThrow { scenario.checkIsNotNullForId("marcus") }
    }

    @Test
    fun `GUIVEN scenario is null WHEN checkIsNotNullForId THEN exception is throw`() {
        val scenario = null
        assertThrows<NotFoundException> { scenario.checkIsNotNullForId("marcus") }
    }

    @Test
    fun `GUIVEN scenario is not null WHEN mustExist THEN no exception is throw`() {
        val scenario = createScenarioForId(ID1)
        assertDoesNotThrow { scenario.mustExist(true) }
    }

    @Test
    fun `GUIVEN scenario is null WHEN mustExist THEN exception is throw`() {
        val scenario = createScenarioForId(ID1)
        assertThrows<NotFoundException> { scenario.mustExist(false) }
    }

    @Test
    fun `GUIVEN scenario is not null with id null WHEN mustExist THEN exception is throw`() {
        val scenario = createScenarioForId(null)
        assertThrows<NotFoundException> { scenario.mustExist(false) }
    }

    @Test
    fun `GUIVEN scenario id is not null WHEN checkScenarioFromDatabase THEN no exception is throw`() {
        val scenario = createScenarioForId(ID1)
        assertDoesNotThrow { scenario.checkScenarioFromDatabase() }
    }

    @Test
    fun `GUIVEN scenario id is null WHEN checkScenarioFromDatabase THEN exception is throw`() {
        val scenario = createScenarioForId(null)
        assertThrows<InternalServerException> { scenario.checkScenarioFromDatabase() }
    }

    private fun createScenarioForId(id: String?): Scenario {
        return Scenario(id = id, name = "test", applicationId = "test", state = "test")
    }
}