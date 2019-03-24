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
import fr.vsct.tock.translator.I18nKeyProvider.Companion.generateKey
import fr.vsct.tock.translator.I18nLabelValue
import mu.KotlinLogging

/**
 * Base implementation of [BotDefinition].
 */
open class BotDefinitionBase(
        override val botId: String,
        override val namespace: String,
        override val stories: List<StoryDefinition>,
        override val nlpModelName: String = botId,
        override val unknownStory: StoryDefinition = defaultUnknownStory,
        override val helloStory: StoryDefinition? = null,
        override val goodbyeStory: StoryDefinition? = null,
        override val noInputStory: StoryDefinition? = null,
        override val botDisabledStory: StoryDefinition? = null,
        override val botEnabledStory: StoryDefinition? = null,
        override val userLocationStory: StoryDefinition? = null,
        override val handleAttachmentStory: StoryDefinition? = null,
        override val eventListener: EventListener = EventListenerBase(),
        override val keywordStory: StoryDefinition = defaultKeywordStory,
        override val conversation: DialogFlowDefinition? = null
) : BotDefinition {

    companion object {
        private val logger = KotlinLogging.logger {}

        /**
         * The default [unknownStory].
         */
        val defaultUnknownStory =
                SimpleStoryDefinition(
                        "tock_unknown_story",
                        object : SimpleStoryHandlerBase() {
                            override fun action(bus: BotBus) {
                                bus.markAsUnknown()
                                bus.end(bus.botDefinition.defaultUnknownAnswer)
                            }
                        },
                        setOf(Intent.unknown)
                )

        /**
         * Returns a (potential) keyword from the [BotBus].
         */
        fun getKeyword(bus: BotBus): String? {
            return if (bus.action is SendSentence) {
                (bus.action as SendSentence).stringText
            } else {
                null
            }
        }

        /**
         * The default handler used to handle test context initialization.
         */
        fun testContextKeywordHandler(bus: BotBus, sendEnd: Boolean = true) {
            bus.userTimeline.dialogs.add(
                    Dialog(
                            setOf(bus.userId, bus.botId)
                    )
            )
            bus.botDefinition.testBehaviour.setup(bus)
            if (sendEnd) {
                bus.end(bus.baseI18nValue("test context activated (user state cleaned)"))
            }
        }

        /**
         * The default handler used to cleanup test context.
         */
        fun endTestContextKeywordHandler(bus: BotBus, sendEnd: Boolean = true) {
            bus.userTimeline.dialogs.add(
                    Dialog(
                            setOf(bus.userId, bus.botId)
                    )
            )
            bus.botDefinition.testBehaviour.cleanup(bus)
            if (sendEnd) {
                bus.end(bus.baseI18nValue("test context disabled"))
            }
        }

        /**
         * The default handler used to delete the current user.
         */
        fun deleteKeywordHandler(bus: BotBus, sendEnd: Boolean = true) {
            bus.handleDelete()
            if (sendEnd) {
                bus.end(
                        bus.baseI18nValue(
                                "user removed - {0} {1}",
                                bus.userTimeline.userPreferences.firstName,
                                bus.userTimeline.userPreferences.lastName
                        )
                )
            }
        }

        private fun BotBus.baseI18nValue(
                defaultLabel: String,
                vararg args: Any?
        ): I18nLabelValue = i18nValue(botDefinition.namespace, defaultLabel, *args)

        private fun i18nValue(
                namespace: String,
                defaultLabel: String,
                vararg args: Any?
        ): I18nLabelValue =
                I18nLabelValue(
                        generateKey(namespace, "keywords", defaultLabel),
                        namespace,
                        "keywords",
                        defaultLabel,
                        args.toList()
                )

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

        /**
         * The default [keywordStory].
         */
        val defaultKeywordStory =
                SimpleStoryDefinition(
                        "tock_keyword_story",
                        object : SimpleStoryHandlerBase() {
                            override fun action(bus: BotBus) {
                                val text = getKeyword(bus)
                                when (getKeyword(bus)) {
                                    deleteKeyword -> deleteKeywordHandler(bus)
                                    testContextKeyword -> testContextKeywordHandler(bus)
                                    endTestContextKeyword -> endTestContextKeywordHandler(bus)
                                    else -> bus.end(bus.baseI18nValue("unknown keyword : {0}"), text)
                                }
                            }
                        },
                        setOf(Intent.keyword)
                )
    }

    /**
     * Constructor intended to be used by an enum.
     */
    constructor(botId: String, stories: Array<out StoryDefinition>) : this(botId, botId, stories.toList(), botId)

    /**
     * The default unknown answer.
     */
    override val defaultUnknownAnswer: I18nLabelValue get() = i18n("Sorry, I didn't understand :(")

    override fun toString(): String {
        return botId
    }

}