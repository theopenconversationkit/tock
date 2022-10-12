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

package ai.tock.bot.processor

import ai.tock.bot.bean.TickConfiguration
import ai.tock.bot.bean.TickSession
import ai.tock.bot.bean.TickStory
import ai.tock.bot.bean.TickUserAction
import ai.tock.bot.processor.bean.UserDialog
import ai.tock.bot.sender.TickSenderDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TickStoryExecutorTest {

    // TODO : how to inject a specific ActionHandlersRepository ?
    // TODO MASS : WIP

    @Test fun testAllDataSet(){

        mapOf(
            "aumax" to listOf("activationcarte", "enrollementmobile"),
            "joignabilite" to listOf("plafondsvirement")
        ).forEach {
            (group, names) -> names.forEach{ testGeneric(group, it) }
        }

//        testGeneric("aumax", "enrollementmobile")

    }

    private fun testGeneric(group: String, name: String){
        val tickStory = getTickStoryConfigurationFromFile(group, name)
        val datasSet = getTickStoryDataSetFromFile(group, name)
        var tickSession = TickSession()

        datasSet.filter { it.enabled }.forEach { dialog ->
            tickSession = processAndCheck(tickSession, tickStory, dialog)
        }
    }

    private fun processAndCheck(tickSession: TickSession, tickStory: TickStory, dialog: UserDialog): TickSession {
        val tickUserAction = TickUserAction(intentName = dialog.message.intent, entities =
        dialog.message.entities)
        val sender = TickSenderDefault()
        // Call the tick story processor
        val (newTickSession, isFinal) =
            TickStoryProcessor(
                tickSession,
                TickConfiguration(
                    stateMachine = tickStory.stateMachine,
                    contexts = tickStory.contexts,
                    actions = tickStory.actions,
                    debug = false
                ),
                sender
            ).process(tickUserAction)

        assertNotNull(
            actual = dialog.dialogManagerResponse.responses.firstOrNull {
                it.size == sender.getHistory().size &&
                        it.containsAll(sender.getHistory())
            },
            message = "${dialog.id} has failed !"
        )


        // TODO MASS : LOGGER
        println("${dialog.id} -> SUCCESS")

        return if(isFinal) TickSession() else newTickSession
    }

    private fun getTickStoryConfigurationFromFile(group: String, name: String): TickStory {
        return Json.decodeFromStream(File("src/test/resources/tickstory/$group/$name/$name-configuration.json").inputStream())
    }

    private fun getTickStoryDataSetFromFile(group: String, name: String): List<UserDialog> {
        return Json.decodeFromStream(File("src/test/resources/tickstory/$group/$name/$name-dataset.json").inputStream())
    }
}