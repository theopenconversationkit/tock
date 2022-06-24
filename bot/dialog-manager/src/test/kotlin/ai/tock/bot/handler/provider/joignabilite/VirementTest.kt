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

package ai.tock.bot.handler.provider.joignabilite

import ai.tock.bot.bean.TickConfiguration
import ai.tock.bot.bean.TickSession
import ai.tock.bot.bean.TickStory
import ai.tock.bot.bean.TickUserAction
import ai.tock.bot.processor.TickStoryProcessor
import ai.tock.bot.processor.bean.UserDialog
import ai.tock.bot.sender.TickSenderDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// TODO : this is a draft. a remplacer par le test genirique à base du dataset

class VirementTest {

    // TODO : how to inject a specific ActionHandlersRepository ?

    @Test fun test1(){
        val tickStory = getTickStoryConfigurationFromFile("joignabilite", "virement")
        var tickSession = TickSession()
        val tickUserAction = TickUserAction(intentName = "i_probleme_virement", entities = mapOf())
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

        tickSession = newTickSession

        val expectedHistory = listOf(
            "Send message [ID : app_tick_Désolé, le service n'est actuellement pas disponible sur l'application, je vais vous mettre en relation avec un conseiller qui pourra traiter votre demande]",
            "End message [ID : app_tick_Fin de la Story, clean les contextes]"
        )

        assertEquals(expectedHistory.size, sender.getHistory().size)
    }



    private fun getTickStoryConfigurationFromFile(group: String, name: String): TickStory {
        return Json.decodeFromStream(File("src/test/resources/tickstory/$group/$name/$name-configuration.json").inputStream())
    }

}