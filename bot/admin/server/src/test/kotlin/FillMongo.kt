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

package ai.tock.bot.admin

import ai.tock.bot.BotIoc
import ai.tock.bot.definition.ConnectorDef
import ai.tock.bot.definition.HandlerDef
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.storyDef
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Story
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.nlp.front.ioc.FrontIoc
import ai.tock.shared.Dice
import ai.tock.shared.defaultNamespace
import ai.tock.shared.injector
import ai.tock.shared.provide
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

// helper to fill database with large data
internal object FillMongo {
    class Def(bus: BotBus) : HandlerDef<Connector>(bus) {
        override fun answer() {}
    }

    class Connector(context: Def) : ConnectorDef<Def>(context)

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            FrontIoc.setup(BotIoc.coreModules)
            val userTimelineDAO: UserTimelineDAO = injector.provide()
            for (i in 0..10000000) {
                val playerId = PlayerId(Dice.newId())
                val dialog =
                    Dialog(
                        playerIds = setOf(playerId),
                        stories =
                            mutableListOf(
                                Story(
                                    storyDef<Def>("usage") {},
                                    Intent("usage"),
                                    actions =
                                        mutableListOf(
                                            SendSentence(
                                                playerId,
                                                "63506610cebd8d6b715559f5",
                                                PlayerId("bot", PlayerType.bot),
                                                text = "Hello",
                                            ),
                                        ),
                                ),
                            ),
                    )
                userTimelineDAO.save(
                    UserTimeline(
                        playerId = PlayerId(Dice.newId()),
                        dialogs = mutableListOf(dialog),
                    ),
                    defaultNamespace,
                )
            }

            exitProcess(0)
        }
    }
}
