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

package ai.tock.bot

import ai.bot.tock.bean.Dialog
import ai.tock.bot.bean.TickConfiguration
import ai.tock.bot.bean.TickSession
import ai.tock.bot.bean.TickStory
import ai.tock.bot.bean.TickStoryValidation
import ai.tock.bot.bean.TickUserAction
import ai.tock.bot.bean.TickStorySettings
import ai.tock.bot.bean.unknown.TickUnknownConfiguration
import ai.tock.bot.processor.Success
import ai.tock.bot.processor.TickStoryProcessor
import ai.tock.bot.sender.TickSenderDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KLogger
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TickStoryExecutorTest {


    private val logger: KLogger = KotlinLogging.logger {}

    @Test fun testAllDataSet(){
        mapOf(
            "max" to listOf("activation-carte", "enrollement-mobile"),
            "joignabilite" to listOf("virement", "prise-rdv")
        ).forEach {
            (group, names) -> names.forEach{ testGeneric(group, it) }
        }
    }

    @Test fun testUnique(){
        testGeneric("max", "activation-carte")
    }

    private fun testGeneric(group: String, name: String){
        val tickStory = getTickStoryConfigurationFromFile(group, name)
        val datasSet = getTickStoryDataSetFromFile(group, name)
        var tickSession = TickSession()

        logger.info { "< $group | $name >" }
        datasSet.filter { it.enabled }.forEach { dialog ->
            tickSession = processAndCheck(tickSession, tickStory, dialog)
        }
    }

    private fun processAndCheck(tickSession: TickSession, tickStory: TickStory, dialog: Dialog): TickSession {
        val tickUserAction = TickUserAction(intentName = dialog.message.intent, entities =
        dialog.message.entities)

        val sender = TickSenderDefault()
        // Call the tick story processor

        val result = TickStoryProcessor(
            tickSession,
            TickConfiguration(
                stateMachine = tickStory.stateMachine,
                contexts = tickStory.contexts,
                actions = tickStory.actions,
                intentsContexts = tickStory.intentsContexts,
                unknownHandleConfiguration = TickUnknownConfiguration(tickStory.unknownAnswerConfigs),
                storySettings = TickStorySettings(2),
                debug = false
            ),
            sender,
            false
        ).process(tickUserAction) as Success



        assertNotNull(
            actual = dialog.response.answers.firstOrNull { it == sender.getHistory() },
            message = "${tickStory.name} [${dialog.id}] -> FAILED ! \n${sender.getHistory().joinToString("\n")}\n"
        )

        logger.info {"${tickStory.name} [${dialog.id}] -> SUCCESS" }

        return if(result.isFinal) TickSession() else result.session
    }


    @OptIn(ExperimentalSerializationApi::class)
    private fun getTickStoryConfigurationFromFile(group: String, name: String): TickStory {
        val tickStory = Json.decodeFromStream<TickStory>(
            File("src/test/resources/tick-story/$group/$name/$name-configuration.json")
                .inputStream()
        )
        // Validate a tick story configuration
        assertTrue("The configuration of tick story <$group/$name> is not correct !") { TickStoryValidation.validateTickStory(tickStory).isEmpty() }

        return tickStory
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getTickStoryDataSetFromFile(group: String, name: String): List<Dialog> {
        return Json.decodeFromStream(File("src/test/resources/tick-story/$group/$name/$name-dataset.json").inputStream())
    }
}