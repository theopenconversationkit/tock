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

package ai.tock.bot.definition

import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfiguration
import ai.tock.bot.admin.bot.observability.BotObservabilityConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.config.RAGAnswerHandler
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.nlp.BuiltInKeywordListener.deleteKeyword
import ai.tock.bot.engine.nlp.BuiltInKeywordListener.endTestContextKeyword
import ai.tock.bot.engine.nlp.BuiltInKeywordListener.testContextKeyword
import ai.tock.bot.engine.nlp.keywordServices
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.vertx.vertx
import ai.tock.translator.I18nKeyProvider.Companion.generateKey
import ai.tock.translator.I18nLabelValue
import com.github.salomonbrys.kodein.instance
import kotlinx.coroutines.runBlocking
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
    override val flowDefinition: DialogFlowDefinition? = null,
    override val botEnabledListener: (Action) -> Unit = {},
    override val ragExcludedStory: StoryDefinition = defaultRagExcludedStory,
    override val ragStory: StoryDefinition = defaultRagStory,
    override var ragConfiguration: BotRAGConfiguration? = null,
    override var vectorStoreConfiguration: BotVectorStoreConfiguration? = null,
    override var observabilityConfiguration: BotObservabilityConfiguration? = null,
    override var documentCompressorConfiguration: BotDocumentCompressorConfiguration? = null
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
         * The default [ragExcludedStory].
         */
        val defaultRagExcludedStory =
            SimpleStoryDefinition(
                "tock_ragexcluded_story",
                object : SimpleStoryHandlerBase() {
                    override fun action(bus: BotBus) {
                        bus.end(bus.botDefinition.defaultRagExcludedAnswer)
                    }
                },
                setOf(Intent.ragexcluded)
            )

        val defaultRagStory =
            RAGStoryDefinition(
                object : SimpleStoryHandlerBase() {
                    override fun action(bus: BotBus) {
                        bus.markAsUnknown()
                        RAGAnswerHandler.handle(bus)
                    }
                },
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
            // run later to avoid the lock effect :)
            vertx.setTimer(1000) {
                vertx.executeBlocking(
                    {
                        try {
                            runBlocking {
                                userTimelineDao.remove(botDefinition.namespace, userId)
                            }
                        } catch (e: Exception) {
                            logger.error(e)
                        }
                    },
                    false
                )
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
                        if (!handleWithKeywordListeners(bus, text)) {
                            when (text) {
                                deleteKeyword -> deleteKeywordHandler(bus)
                                testContextKeyword -> testContextKeywordHandler(bus)
                                endTestContextKeyword -> endTestContextKeywordHandler(bus)
                                else -> bus.end(bus.baseI18nValue("unknown keyword : {0}", text))
                            }
                        }
                    }
                },
                setOf(Intent.keyword)
            )

        fun handleWithKeywordListeners(bus: BotBus, keyword: String?): Boolean {
            if (keyword != null) {
                keywordServices.firstNotNullOfOrNull { it.keywordHandler(keyword) }?.let { handler ->
                    handler(bus)
                    return true
                }
            }
            return false
        }
    }

    /**
     * Constructor intended to be used by an enum.
     */
    constructor(botId: String, stories: Array<out StoryDefinition>) : this(botId, botId, stories.toList(), botId)

    /**
     * The default unknown answer.
     */
    override val defaultUnknownAnswer: I18nLabelValue get() = i18n("Sorry, I didn't understand :(")

    /**
     * The default ragExcluded answer.
     */
    override val defaultRagExcludedAnswer: I18nLabelValue get() = i18n("Sorry, I can't answer your question (Topic not covered)")

    override fun toString(): String {
        return botId
    }
}
