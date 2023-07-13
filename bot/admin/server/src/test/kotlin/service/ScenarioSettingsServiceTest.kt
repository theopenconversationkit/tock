/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.scenario.ScenarioSettings
import ai.tock.bot.admin.scenario.ScenarioSettingsDAO
import ai.tock.bot.test.*
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ScenarioSettingsQuery
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val ID = "id"

private const val DB_RESULT_APP_NAME = "dbAppId"
private const val APP_NAME = "appId"

private const val QUERY_REPEAT_NB = 1

private const val QUERY_REDIRECT_STORY = "storyId"

private const val DB_RESULT_REPEAT_NB = 2
class ScenarioSettingsServiceTest {

    companion object {
        init {
            tockInternalInjector = KodeinInjector()
            Kodein.Module {
                bind<ScenarioSettingsDAO>() with singleton { dao }

            }.also {
                tockInternalInjector.inject(
                    Kodein { import(it) }
                )
            }
        }

        private val dao: ScenarioSettingsDAO = mockk(relaxed = true)
        private val settingsSlot = slot<ScenarioSettings>()
    }


    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `save already existing settings`() {

        val args : TSupplier<Pair<ApplicationDefinition, ScenarioSettingsQuery>> = {
            appDef() to query()
        }

        val mockBehaviours : TRunnable = {
            every { dao.getScenarioSettingsByBotId(any()) } returns dbResult()
            every { dao.save(capture(settingsSlot)) } returns Unit
        }

        val callService : TFunction<Pair<ApplicationDefinition, ScenarioSettingsQuery>?, Unit> = {
            ScenarioSettingsService.save(it!!.first, it.second)
        }

        val checkSlot : TConsumer<Unit?> = {
            assertTrue { settingsSlot.isCaptured }
            with(settingsSlot.captured) {
                assertEquals(DB_RESULT_APP_NAME, botId)
                assertEquals(QUERY_REPEAT_NB, actionRepetitionNumber)
                assertEquals(QUERY_REDIRECT_STORY, redirectStoryId)
            }
        }

        TestCase<Pair<ApplicationDefinition, ScenarioSettingsQuery>, Unit>("save already existing settings")
            .given("the provided applicationDefinition and query", args)
            .and("""
                - dao return a not null scenarioSettings with the applicationId
                - settings is captured when dao.save is called
            """.trimIndent(), mockBehaviours)
            .`when`("the save method is called", callService)
            .then("the captured slot should be captured and the captured settings should have the right property values", checkSlot)
            .run()
    }

    @Test
    fun `save non existing settings`() {

        val args : TSupplier<Pair<ApplicationDefinition, ScenarioSettingsQuery>> = {
            appDef() to query()
        }

        val mockBehaviours : TRunnable = {
            settingsSlot.clear()
            every { dao.getScenarioSettingsByBotId(any()) } returns null
            every { dao.save(capture(settingsSlot)) } returns Unit
        }

        val callService : TFunction<Pair<ApplicationDefinition, ScenarioSettingsQuery>?, Unit> = {
            ScenarioSettingsService.save(it!!.first, it.second)
        }

        val checkSlot : TConsumer<Unit?> = {
            assertTrue { settingsSlot.isCaptured }
            with(settingsSlot.captured) {
                assertEquals(APP_NAME, botId)
                assertEquals(QUERY_REPEAT_NB, actionRepetitionNumber)
                assertEquals(QUERY_REDIRECT_STORY, redirectStoryId)
            }
        }

        TestCase<Pair<ApplicationDefinition, ScenarioSettingsQuery>, Unit>("save non existing settings")
            .given("the provided applicationDefinition and query", args)
            .and("""
                - dao return a null scenarioSettings with the applicationId
                - settings is captured when dao.save is called
            """.trimIndent(), mockBehaviours)
            .`when`("the save method is called", callService)
            .then("the captured slot should be captured and the captured settings should have the right property values", checkSlot)
            .run()
    }

    @Test
    fun `get existing scenario settings by application id`() {

        val appId : TSupplier<String> = {
            DB_RESULT_APP_NAME
        }

        val mockBehaviours : TRunnable = {
            every { dao.getScenarioSettingsByBotId(any()) } returns dbResult()
        }

        val callService : TFunction<String?, ScenarioSettings?> = {
            ScenarioSettingsService.getScenarioSettingsByBotId(it!!)
        }

        val checkResult : TConsumer<ScenarioSettings?> = {
            assertNotNull(it)
            assertEquals(DB_RESULT_APP_NAME, it.botId)
            assertEquals(ID, it._id.toString())
        }

        TestCase<String, ScenarioSettings?>("get existing scenario settings by application id")
            .given("the provided application id is APP_ID", appId)
            .and("dao returns a non null result when the application id is APP_ID", mockBehaviours)
            .`when`("getScenarioSettingsByApplicationId method is called", callService)
            .then("the result is not null", checkResult)
            .run()
    }

    @Test
    fun `try to get non existing scenario settings by application id`() {

        val appId : TSupplier<String> = {
            DB_RESULT_APP_NAME
        }

        val mockBehaviours : TRunnable = {
            every { dao.getScenarioSettingsByBotId(DB_RESULT_APP_NAME) } returns null
        }

        val callService : TFunction<String?, ScenarioSettings?> = {
            ScenarioSettingsService.getScenarioSettingsByBotId(it!!)
        }

        val checkResult : TConsumer<ScenarioSettings?> = {
            assertNull(it)
        }

        TestCase<String, ScenarioSettings?>("try to get non existing scenario settings by application id")
            .given("the provided application id is APP_ID", appId)
            .and("dao returns a null result when the application id is APP_ID", mockBehaviours)
            .`when`("getScenarioSettingsByApplicationId method is called", callService)
            .then("the result is null", checkResult)
            .run()
    }

    private fun dbResult(): ScenarioSettings = ScenarioSettings(
        _id = ID.toId(),
        actionRepetitionNumber = DB_RESULT_REPEAT_NB,
        updateDate = Instant.now(),
        creationDate = Instant.now(),
        botId = DB_RESULT_APP_NAME
    )

    private fun appDef() = ApplicationDefinition(
        _id = APP_NAME.toId(),
        name = APP_NAME,
        namespace = "namespace"
    )

    private fun query() = ScenarioSettingsQuery(QUERY_REPEAT_NB, QUERY_REDIRECT_STORY)
}