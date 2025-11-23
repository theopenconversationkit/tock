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

package service

import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.admin.indicators.IndicatorError
import ai.tock.bot.admin.indicators.IndicatorValue
import ai.tock.bot.admin.model.Valid
import ai.tock.bot.admin.model.indicator.IndicatorResponse
import ai.tock.bot.admin.model.indicator.IndicatorValueRequest
import ai.tock.bot.admin.model.indicator.IndicatorValueResponse
import ai.tock.bot.admin.model.indicator.SaveIndicatorRequest
import ai.tock.bot.admin.model.indicator.UpdateIndicatorRequest
import ai.tock.bot.admin.service.IndicatorService
import ai.tock.bot.test.TConsumer
import ai.tock.bot.test.TFunction
import ai.tock.bot.test.TRunnable
import ai.tock.bot.test.TSupplier
import ai.tock.bot.test.TestCase
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.newId

class IndicatorServiceTest {
    companion object {
        const val NAMESPACE = "myNS"
        const val BOT_ID = "app"
        const val NAME = "Satisfaction"
        const val LABEL = "Satisfaction label"
        const val DESCRIPTION = "Satisfaction description"
        val DIMENSIONS = setOf("satisfaction", "Survey")
        val VALUES =
            listOf(
                "ok" to "OK",
                "ko" to "KO",
            )

        const val NEW_LABEL = "Satisfaction label"
        const val NEW_DESCRIPTION = "new Satisfaction description"
        val NEW_DIMENSIONS = setOf("satisfaction", "Survey", "new dimension")
        val NEW_VALUES =
            listOf(
                "ok" to "OK",
                "ko" to "KO",
                "N/S" to "unknown",
            )

        init {
            tockInternalInjector = KodeinInjector()
            Kodein.Module {
                bind<IndicatorDAO>() with singleton { dao }
            }.also {
                tockInternalInjector.inject(
                    Kodein { import(it) },
                )
            }
        }

        private val dao: IndicatorDAO = mockk(relaxed = true)

        private val slot = slot<Indicator>()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `Save valid indicator that does not exist yet`() {
        val entry: TSupplier<SaveFnEntry> = {
            Triple(NAMESPACE, BOT_ID, saveIndicatorRequest())
        }

        val similarIndicatorNotExist: TRunnable = {
            every { dao.existByNameAndBotId(any(), any(), any()) } returns false
        }

        val captureIndicatorToSave: TRunnable = {
            every { dao.save(capture(slot)) } returns Unit
        }

        val callServiceSave: TFunction<SaveFnEntry?, Unit> = {
            assertNotNull(it)
            IndicatorService.save(it!!.first, it.second, Valid(it.third))
        }

        val daoExistByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { dao.existByNameAndBotId(NAME, NAMESPACE, BOT_ID) }
        }
        val checkIndicatorToPersist: TRunnable = {
            assertTrue(slot.isCaptured)
            val captured = slot.captured
            assertNotNull(captured)
            assertEquals(NAME, captured.name)
            assertEquals(LABEL, captured.label)
            assertEquals(DESCRIPTION, captured.description)
            assertEquals(DIMENSIONS, captured.dimensions)
            assertEquals(VALUES.map { IndicatorValue(it.first, it.second) }.toSet(), captured.values)
        }

        TestCase<SaveFnEntry, Unit>("Save valid indicator that does not exist yet")
            .given("A application name and a valid request", entry)
            .and(
                "Indicator not exist with request name or label and the given application name",
                similarIndicatorNotExist,
            )
            .and("The indicator to persist in database is captured", captureIndicatorToSave)
            .`when`("IndicatorService's save method is called", callServiceSave)
            .then("The dao's existByNameAndBotId must be called exactly once", daoExistByFnIsCalledOnce)
            .and(
                """
                - Indicator to persist must be not null
                - Indicator to persist must have a not null id
                """.trimIndent(),
                checkIndicatorToPersist,
            )
            .run()
    }

    @Test
    fun `Try to save valid indicator that already exist`() {
        val entry: TSupplier<SaveFnEntry> = {
            Triple(NAMESPACE, BOT_ID, saveIndicatorRequest())
        }

        val similarIndicatorNotExist: TRunnable = {
            every { dao.existByNameAndBotId(any(), any(), any()) } returns true
        }

        val callServiceSave: TFunction<SaveFnEntry?, IndicatorError> = {
            assertNotNull(it)
            assertThrows {
                IndicatorService.save(it!!.first, it.second, Valid(it.third))
            }
        }

        val daoExistByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { dao.existByNameAndBotId(NAME, NAMESPACE, BOT_ID) }
        }

        val daoSaveByFnIsNotCalled: TRunnable = {
            verify(exactly = 0) { dao.save(any()) }
        }

        val checkError: TConsumer<IndicatorError?> = {
            assertNotNull(it)
            assertTrue(it is IndicatorError.IndicatorAlreadyExists)

            it as IndicatorError.IndicatorAlreadyExists

            assertEquals(NAME, it.name)
            assertEquals(LABEL, it.label)
            assertEquals(NAMESPACE, it.namespace)
            assertEquals(BOT_ID, it.botId)
        }

        TestCase<SaveFnEntry, IndicatorError>("Try to save valid indicator that already exists")
            .given("An application name and a valid request", entry)
            .and("Indicator exists with request name or label and the given application name", similarIndicatorNotExist)
            .`when`("IndicatorService's save method is called", callServiceSave)
            .then("The dao's existByNameAndBotId must be called exactly once", daoExistByFnIsCalledOnce)
            .and("The dao's save must not be called", daoSaveByFnIsNotCalled)
            .and(
                """ 
                - Error is not null
                - Error is of type IndicatorAlreadyExists
                """.trimIndent(),
                checkError,
            )
            .run()
    }

    @Test
    fun `Update existing indicator`() {
        val entry: TSupplier<UpdateFnEntry> = {
            UpdateFnEntry(
                namespace = NAMESPACE,
                botId = BOT_ID,
                name = NAME,
                request = updateIndicatorRequest(),
            )
        }

        val indicatorExist: TRunnable = {
            every { dao.findByNameAndBotId(any(), any(), any()) } returns indicator()
        }

        val captureIndicatorToSave: TRunnable = {
            every { dao.save(capture(slot)) } returns Unit
        }

        val callServiceUpdate: TFunction<UpdateFnEntry?, Unit> = {
            assertNotNull(it)
            IndicatorService.update(
                namespace = it!!.namespace,
                botId = it.botId,
                indicatorName = it.name,
                request = Valid(it.request),
            )
        }

        val daoFindByNameAndBotIdIsCalledOnce: TRunnable = {
            verify(exactly = 1) { dao.findByNameAndBotId(NAME, NAMESPACE, BOT_ID) }
        }

        val checkIndicatorToPersist: TRunnable = {
            assertTrue(slot.isCaptured)
            val captured = slot.captured
            assertNotNull(captured)
            assertEquals(NAME, captured.name)
            assertEquals(NEW_LABEL, captured.label)
            assertEquals(NEW_DESCRIPTION, captured.description)
            assertEquals(NEW_DIMENSIONS, captured.dimensions)
            assertEquals(NEW_VALUES.map { IndicatorValue(it.first, it.second) }.toSet(), captured.values)
        }

        TestCase<UpdateFnEntry, Unit>("Update existing indicator")
            .given("An application name and a valid request", entry)
            .and("Indicator exists with request name the given application name", indicatorExist)
            .and("The indicator to persist in database is captured", captureIndicatorToSave)
            .`when`("IndicatorService's update method is called", callServiceUpdate)
            .then("The dao's findByNameAndBotId must be called exactly once", daoFindByNameAndBotIdIsCalledOnce)
            .and(
                """
                - Indicator to persist must not be null
                - Indicator to persist must not have a null id
                """.trimIndent(),
                checkIndicatorToPersist,
            )
            .run()
    }

    @Test
    fun `Try to update non existing indicator`() {
        val entry: TSupplier<UpdateFnEntry> = {
            UpdateFnEntry(
                namespace = NAMESPACE,
                botId = BOT_ID,
                name = NAME,
                request = updateIndicatorRequest(),
            )
        }

        val indicatorDoNotExist: TRunnable = {
            every { dao.findByNameAndBotId(any(), any(), any()) } returns null
        }

        val callServiceUpdate: TFunction<UpdateFnEntry?, IndicatorError> = {
            assertNotNull(it)
            assertThrows {
                IndicatorService.update(
                    namespace = it!!.namespace,
                    botId = it.botId,
                    indicatorName = it.name,
                    request = Valid(it.request),
                )
            }
        }

        val daoFindByNameAndBotIdIsCalledOnce: TRunnable = {
            verify(exactly = 1) { dao.findByNameAndBotId(NAME, NAMESPACE, BOT_ID) }
        }

        val daoSaveByFnIsNotCalled: TRunnable = {
            verify(exactly = 0) { dao.save(any()) }
        }

        val checkError: TConsumer<IndicatorError?> = {
            assertNotNull(it)
            assertTrue(it is IndicatorError.IndicatorNotFound)

            it as IndicatorError.IndicatorNotFound

            assertEquals(NAME, it.name)
            assertEquals(NAMESPACE, it.namespace)
            assertEquals(BOT_ID, it.botId)
        }

        TestCase<UpdateFnEntry, IndicatorError>("Try to update non existing indicator ")
            .given("An application name and a valid request", entry)
            .and("Indicator not exist with request name the given application name", indicatorDoNotExist)
            .`when`("IndicatorService's update method is called", callServiceUpdate)
            .then("The dao's findByNameAndBotId must be called exactly once", daoFindByNameAndBotIdIsCalledOnce)
            .and("The dao's save method is never called", daoSaveByFnIsNotCalled)
            .and(
                """ 
                - Error is not null
                - Error is of type IndicatorNotFound
                """.trimIndent(),
                checkError,
            )
            .run()
    }

    @Test
    fun `Find indicator by name and bot id`() {
        val entries: TSupplier<Triple<String, String, String>> = {
            Triple(NAME, NAMESPACE, BOT_ID)
        }

        val indicatorExist: TRunnable = {
            every { dao.findByNameAndBotId(any(), any(), any()) } returns indicator()
        }

        val callServiceFindByNameAndBotId: TFunction<Triple<String, String, String>?, IndicatorResponse?> = {
            assertNotNull(it)
            IndicatorService.findByNameAndBotId(it!!.first, it.second, it.third)
        }

        val checkResponse: TConsumer<IndicatorResponse?> = {
            assertNotNull(it)
            assertEquals(NAME, it!!.name)
            assertEquals(LABEL, it.label)
            assertEquals(DESCRIPTION, it.description)
            assertEquals(DIMENSIONS, it.dimensions)
            assertEquals(VALUES.map { value -> IndicatorValueResponse(value.first, value.second) }.toSet(), it.values)
        }

        TestCase<Triple<String, String, String>, IndicatorResponse>("Find indicator by name and bot id")
            .given("A given name and application name", entries)
            .and("An indicator exists with the given entries", indicatorExist)
            .`when`("The IndicatorService findByNameAndBotId method is called", callServiceFindByNameAndBotId)
            .then(
                """"
                - response must not be null
                - 
                """.trimMargin(),
                checkResponse,
            )
    }

    @Test
    fun `Delete successfully an indicator`() {
        val indicatorName: TSupplier<String> = { NAME }

        val deleteSucceed: TRunnable = {
            every { dao.deleteByNameAndApplicationName(any(), any(), any()) } returns true
        }

        val callServiceDelete: TFunction<String?, Boolean> = {
            assertNotNull(it)
            IndicatorService.deleteByNameAndApplicationName(NAME, NAMESPACE, BOT_ID)
        }

        val checkResponse: TConsumer<Boolean?> = {
            assertNotNull(it)
            assertTrue(it!!)
        }

        TestCase<String, Boolean>("Delete successfully an indicator")
            .given("A given identifier", indicatorName)
            .and("The call of dao delete method returns true", deleteSucceed)
            .`when`("The IndicatorService deleteById method is called", callServiceDelete)
            .then("The response should be true", checkResponse)
    }

    @Test
    fun `Try to delete an indicator`() {
        val indicatorName: TSupplier<String> = { NAME }

        val deleteFails: TRunnable = {
            every { dao.deleteByNameAndApplicationName(any(), any(), any()) } returns false
        }

        val callServiceDelete: TFunction<String?, IndicatorError> = {
            assertNotNull(it)
            assertThrows {
                IndicatorService.deleteByNameAndApplicationName(NAME, NAMESPACE, BOT_ID)
            }
        }

        val checkError: TConsumer<IndicatorError?> = {
            assertNotNull(it)
            assertTrue(it!! is IndicatorError.IndicatorDeletionFailed)
        }

        TestCase<String, IndicatorError>("Try to delete  an indicator")
            .given("A given identifier", indicatorName)
            .and("The call of dao delete method returns false", deleteFails)
            .`when`("The IndicatorService deleteById method is called", callServiceDelete)
            .then("An error of type IndicatorDeletionFailed should be returned", checkError)
    }

    private fun indicator() =
        Indicator(
            newId(),
            NAME,
            LABEL,
            DESCRIPTION,
            NAMESPACE,
            BOT_ID,
            DIMENSIONS,
            VALUES.map { IndicatorValue(it.first, it.second) }.toSet(),
        )

    private fun saveIndicatorRequest() =
        SaveIndicatorRequest(
            name = NAME,
            label = LABEL,
            description = DESCRIPTION,
            dimensions = DIMENSIONS,
            values =
                VALUES.map {
                    IndicatorValueRequest(it.first, it.second)
                }.toSet(),
        )

    private fun updateIndicatorRequest() =
        UpdateIndicatorRequest(
            label = NEW_LABEL,
            description = NEW_DESCRIPTION,
            dimensions = NEW_DIMENSIONS,
            values =
                NEW_VALUES.map {
                    IndicatorValueRequest(it.first, it.second)
                }.toSet(),
        )
}

typealias SaveFnEntry = Triple<String, String, SaveIndicatorRequest>

data class UpdateFnEntry(
    val namespace: String,
    val botId: String,
    val name: String,
    val request: UpdateIndicatorRequest,
)
