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

package ai.tock.bot.definition

import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfiguration
import ai.tock.bot.admin.bot.observability.BotObservabilityConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.Intent.Companion.keyword
import ai.tock.bot.definition.Intent.Companion.ragexcluded
import ai.tock.bot.definition.Intent.Companion.unknown
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.EntityType
import ai.tock.shared.booleanProperty
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.withNamespace
import ai.tock.shared.withoutNamespace
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.UserInterfaceType
import java.time.Duration
import java.util.Locale

/**
 * The main interface used to define the behaviour of the bot.
 *
 * New bots should usually not directly extend this class, but instead extend [BotDefinitionBase].
 */
interface BotDefinition : I18nKeyProvider {

    companion object {

        /**
         * Convenient default value in ms to wait before next answer sentence. 1s by default.
         */
        @Volatile
        var defaultBreath: Long = longProperty("tock_bot_breath_ms", 1000L)

        /**
         * The minimum delay between two consecutive messages,
         * in case a message takes longer than [defaultBreath] to prepare and send.
         *
         * 50ms by default.
         */
        val minBreath = Duration.ofMillis(longProperty("tock_bot_min_breath_ms", 50L))

        private val sendChoiceActivateBot = booleanProperty("tock_bot_send_choice_activate", true)

        /**
         * Finds an intent from an intent name and a list of [StoryDefinition].
         * Is no valid intent found, returns [unknown].
         */
        internal fun findIntent(stories: List<StoryDefinition>, intent: String): Intent {
            val targetIntent = Intent(intent)
            return if (stories.any { it.supportIntent(targetIntent) } ||
                stories.any { it.allSteps().any { s -> s.supportIntent(targetIntent) } }
            ) {
                targetIntent
            } else {
                when(intent){
                    keyword.name -> keyword
                    ragexcluded.intentWithoutNamespace().name -> ragexcluded
                    else -> unknown
                }
            }
        }

        /**
         * Finds a [StoryDefinition] from a list of [StoryDefinition] and an intent name.
         * Is no valid [StoryDefinition] found, returns the ragStory if enabled, otherwise returns [unknownStory]
         */
        internal fun findStoryDefinition(
            stories: List<StoryDefinition>,
            intent: String?,
            unknownStory: StoryDefinition,
            keywordStory: StoryDefinition,
            ragExcludedStory: StoryDefinition? = null,
            ragStory: StoryDefinition? = null,
            ragConfiguration: BotRAGConfiguration? = null
        ): StoryDefinition {
            return if (intent == null) {
               unknownStory
            } else {
                val i = findIntent(stories, intent)
                stories.find { it.isStarterIntent(i) }
                    ?: when(intent) {
                        keyword.name -> keywordStory
                        ragexcluded.intentWithoutNamespace().name -> ragExcludedStory ?: unknownStory
                        else -> (if(ragConfiguration?.enabled == true) ragStory else null) ?: unknownStory
                    }
            }
        }
    }

    /**
     * The main bot id. Must be different for each bot.
     */
    val botId: String

    /**
     * The namespace of the bot. It has to be the same namespace than the NLP models.
     */
    val namespace: String

    /**
     * The name of the main nlp model.
     */
    val nlpModelName: String

    /**
     * RAG configuration
     */
    var ragConfiguration: BotRAGConfiguration?

    /**
     * Vector Store configuration
     */
    var vectorStoreConfiguration: BotVectorStoreConfiguration?

    /**
     * Observability configuration
     */
    var observabilityConfiguration: BotObservabilityConfiguration?

    /**
     * Document Compressor configuration
     */
    var documentCompressorConfiguration: BotDocumentCompressorConfiguration?

    /**
     * The list of each story.
     */
    val stories: List<StoryDefinition>

    /**
     * Finds an [Intent] from an intent name.
     */
    fun findIntent(intent: String, applicationId: String): Intent {
        return findIntent(stories, intent)
    }

    /**
     * Finds a [StoryDefinition] from an [Intent].
     */
    fun findStoryDefinition(intent: IntentAware?, applicationId: String): StoryDefinition {
        return if (intent is StoryDefinition) {
            intent
        } else {
            findStoryDefinition(intent?.wrappedIntent()?.name, applicationId)
        }
    }

    /**
     * Search story by storyId.
     */
    fun findStoryDefinitionById(storyId: String, applicationId: String): StoryDefinition = stories.find { it.id == storyId } ?: unknownStory

    /**
     * Search story by storyHandler.
     */
    fun findStoryByStoryHandler(storyHandler: StoryHandler, applicationId: String): StoryDefinition? =
        stories.find { it.storyHandler == storyHandler }

    /**
     * Finds a [StoryDefinition] from an intent name.
     *
     * @param intent the intent name
     * @param applicationId the optional applicationId
     */
    fun findStoryDefinition(intent: String?, applicationId: String): StoryDefinition {
        return findStoryDefinition(
            stories,
            intent,
            unknownStory,
            keywordStory
        )
    }

    /**
     * The unknown story. Used where no valid intent is found.
     */
    val unknownStory: StoryDefinition

    /**
     * The ragExcluded Story. Used where ragexcluded intent is found.
     */
    val ragExcludedStory: StoryDefinition

    /**
     * The ragStory. Used if RAG is enabled.
     */
    val ragStory: StoryDefinition

    /**
     * The default unknown answer.
     */
    val defaultUnknownAnswer: I18nLabelValue

    /**
     * The default rag excluded answer.
     */
    val defaultRagExcludedAnswer: I18nLabelValue

    /**
     * To handle keywords - used to bypass nlp.
     */
    val keywordStory: StoryDefinition

    /**
     * The hello story. Used for first interaction with no other input.
     */
    val helloStory: StoryDefinition?

    /**
     * Provides default Story when no context is known - default to [helloStory] or first [stories].
     */
    val defaultStory: StoryDefinition get() = helloStory ?: stories.first()

    /**
     * The goodbye story. Used when closing the conversation.
     */
    val goodbyeStory: StoryDefinition?

    /**
     * The no input story. When user does nothing!
     */
    val noInputStory: StoryDefinition?

    /**
     * The story that handles [ai.tock.bot.engine.action.SendLocation] action. If it's null, current intent is used.
     */
    val userLocationStory: StoryDefinition?

    /**
     * The story that handles [ai.tock.bot.engine.action.SendAttachment] action. If it's null, current intent is used.
     */
    val handleAttachmentStory: StoryDefinition?

    /**
     * To handle custom events.
     */
    val eventListener: EventListener

    /**
     * Called when error occurs. By default send "technical error".
     */
    fun errorAction(playerId: PlayerId, applicationId: String, recipientId: PlayerId): Action {
        return SendSentence(
            playerId,
            applicationId,
            recipientId,
            property("tock_technical_error", "Technical error :( sorry!")
        )
    }

    /**
     * To manage deactivation.
     */
    @Deprecated("use botDisabledStories list")
    val botDisabledStory: StoryDefinition?

    /**
     * List of deactivation stories.
     */
    val botDisabledStories: List<StoryDefinition> get() = emptyList()

    /**
     * Does this action trigger bot deactivation ?
     */
    fun disableBot(timeline: UserTimeline, dialog: Dialog, action: Action): Boolean =
        action.state.notification ||
            dialog.state.currentIntent?.let { botDisabledStory?.isStarterIntent(it) } ?: false ||
            hasDisableTagIntent(dialog)

    /**
     * Returns true if the dialog current intent is a disabling intent.
     */
    fun hasDisableTagIntent(dialog: Dialog) =
        dialog.state.currentIntent?.let { botDisabledStories.any { story -> story.isStarterIntent(it) } } ?: false

    /**
     * To manage reactivation.
     */
    @Deprecated("use botEnabledStories list")
    val botEnabledStory: StoryDefinition?

    /**
     * List of reactivation stories.
     */
    val botEnabledStories: List<StoryDefinition> get() = emptyList()

    /**
     * Does this action trigger bot activation ?
     */
    fun enableBot(timeline: UserTimeline, dialog: Dialog, action: Action): Boolean =
        dialog.state.currentIntent?.let { botEnabledStory?.isStarterIntent(it) } ?: false ||
            dialog.state.currentIntent?.let { botEnabledStories.any { story -> story.isStarterIntent(it) } } ?: false ||
            // send choice can reactivate disabled bot (is the intent is not a disabled intent)
            (
                sendChoiceActivateBot &&
                    action is SendChoice &&
                    !action.state.notification &&
                    !(dialog.state.currentIntent?.let { botDisabledStory?.isStarterIntent(it) } ?: false)
                )

    /**
     *  Listener invoked when bot is enabled.
     */
    val botEnabledListener: (Action) -> Unit get() = {}

    /**
     * If this method returns true, the action will be added in the stored history.
     *
     * By default, actions where the bot is not only [ai.tock.bot.engine.dialog.EventState.notification]
     * are added in the bot history.
     */
    fun hasToPersistAction(timeline: UserTimeline, action: Action): Boolean = !action.state.notification

    /**
     * Returns a [TestBehaviour]. Used in Integration Tests.
     */
    val testBehaviour: TestBehaviour get() = TestBehaviourBase()

    override fun i18n(defaultLabel: CharSequence, args: List<Any?>): I18nLabelValue {
        val category = javaClass.kotlin.simpleName?.replace("Definition", "") ?: ""
        return I18nLabelValue(
            I18nKeyProvider.generateKey(namespace, category, defaultLabel),
            namespace,
            category,
            defaultLabel,
            args
        )
    }

    /**
     * Returns the entity with the specified name and optional role.
     */
    fun entity(name: String, role: String? = null): Entity =
        Entity(
            EntityType(name.withNamespace(namespace)),
            role ?: name.withoutNamespace(namespace)
        )

    /**
     * Returns an [I18nTranslator] for the specified [userLocale] and [connectorType].
     */
    fun i18nTranslator(
        userLocale: Locale,
        connectorType: ConnectorType,
        userInterfaceType: UserInterfaceType = connectorType.userInterfaceType,
        contextId: String? = null
    ): I18nTranslator =
        object : I18nTranslator {
            override val userLocale: Locale get() = userLocale
            override val userInterfaceType: UserInterfaceType get() = userInterfaceType
            override val sourceConnectorType: ConnectorType get() = connectorType
            override val targetConnectorType: ConnectorType get() = connectorType
            override val contextId: String? get() = contextId

            override fun i18n(defaultLabel: CharSequence, args: List<Any?>): I18nLabelValue {
                return this@BotDefinition.i18n(defaultLabel, args)
            }
        }

    /**
     * Get the default delay between two answers.
     */
    fun defaultDelay(answerIndex: Int): Long = if (answerIndex == 0) 0 else defaultBreath

    val flowDefinition: DialogFlowDefinition? get() = null
}
