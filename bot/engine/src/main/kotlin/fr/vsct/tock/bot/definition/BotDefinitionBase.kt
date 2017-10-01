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

package fr.vsct.tock.bot.definition

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.nlp.BuiltInKeywordListener.deleteKeyword
import fr.vsct.tock.bot.engine.nlp.BuiltInKeywordListener.endTestContextKeyword
import fr.vsct.tock.bot.engine.nlp.BuiltInKeywordListener.testContextKeyword
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.vertx.vertx
import mu.KotlinLogging

/**
 * Base implementation of [BaseDefinition].
 */
open class BotDefinitionBase(override val botId: String,
                             override val namespace: String,
                             override val stories: List<StoryDefinition>,
                             override val nlpModelName: String = botId,
                             override val unknownStory: StoryDefinition = defaultUnknownStory,
                             override val helloStory: StoryDefinition? = null,
                             override val botDisabledStory: StoryDefinition? = null,
                             override val botEnabledStory: StoryDefinition? = null,
                             override val userLocationStory: StoryDefinition? = null,
                             override val handleAttachmentStory: StoryDefinition? = null,
                             override val eventListener: EventListener = EventListenerBase(),
                             override val keywordStory: StoryDefinition = defaultKeywordStory) : BotDefinition {

    companion object {
        private val logger = KotlinLogging.logger {}

        val defaultUnknownStory =
                SimpleStoryDefinition(
                        "tock_unknown_story",
                        object : StoryHandlerBase() {
                            override fun action(bus: BotBus) {
                                bus.end("Sorry, I didn't understand :(")
                            }
                        },
                        setOf(Intent.unknown))

        val defaultKeywordStory =
                SimpleStoryDefinition(
                        "tock_keyword_story",
                        object : StoryHandlerBase() {
                            override fun action(bus: BotBus) {
                                if (bus.action is SendSentence) {
                                    val text = (bus.action as SendSentence).text
                                    when (text) {
                                        deleteKeyword -> {
                                            bus.handleDelete()
                                            bus.end(
                                                    "user removed - {0} {1}",
                                                    bus.userTimeline.userPreferences.firstName,
                                                    bus.userTimeline.userPreferences.lastName)
                                        }
                                        testContextKeyword -> {
                                            bus.userTimeline.dialogs.add(
                                                    Dialog(
                                                            setOf(bus.userId, bus.botId)))
                                            bus.userPreferences.test = true
                                            bus.end("test context activated")
                                        }
                                        endTestContextKeyword -> {
                                            bus.userTimeline.dialogs.add(
                                                    Dialog(
                                                            setOf(bus.userId, bus.botId)))
                                            bus.userPreferences.test = false
                                            bus.end("test context desactivated")
                                        }
                                        else -> bus.end("unknown keyword : $text")
                                    }
                                    return
                                } else {
                                    error("keyword story only handle text for now")
                                }
                            }

                            private fun BotBus.handleDelete() {
                                val userTimelineDao: UserTimelineDAO by injector.instance()
                                //run later to avoid the lock effect :)
                                vertx.setTimer(1000) {
                                    vertx.executeBlocking<Unit>({
                                        try {
                                            userTimelineDao.remove(userId)
                                        } catch (e: Exception) {
                                            logger.error(e)
                                        } finally {
                                            it.complete()
                                        }
                                    }, false, {})
                                }

                            }
                        },
                        setOf(Intent.keyword))
    }
}