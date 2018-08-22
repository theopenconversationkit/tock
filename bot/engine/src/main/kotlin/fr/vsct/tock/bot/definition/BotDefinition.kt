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

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.Intent.Companion.keyword
import fr.vsct.tock.bot.definition.Intent.Companion.unknown
import fr.vsct.tock.bot.engine.I18nTranslator
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.api.client.model.EntityType
import fr.vsct.tock.shared.withNamespace
import fr.vsct.tock.shared.withoutNamespace
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelValue
import fr.vsct.tock.translator.UserInterfaceType
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
        const val DEFAULT_BREATH: Long = 1000L

        /**
         * Finds an intent from an intent name and a list of [StoryDefinition].
         * Is no valid intent found, returns [unknown].
         */
        fun findIntent(stories: List<StoryDefinition>, intent: String): Intent {
            return stories.flatMap { it.intents }.find { it.name == intent }
                    ?: if (intent == keyword.name) keyword else unknown
        }

        /**
         * Finds a [StoryDefinition] from a list of [StoryDefinition] and an intent name.
         * Is no valid [StoryDefinition] found, returns the [unknownStory].
         */
        fun findStoryDefinition(
            stories: List<StoryDefinition>,
            intent: String?,
            unknownStory: StoryDefinition,
            keywordStory: StoryDefinition
        ): StoryDefinition {
            return if (intent == null) {
                unknownStory
            } else {
                val i = findIntent(stories, intent)
                stories.find { it.isStarterIntent(i) }
                        ?: if (intent == keyword.name) keywordStory else unknownStory
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
     * The list of each stories.
     */
    val stories: List<StoryDefinition>

    /**
     * Finds an [Intent] from an intent name.
     */
    fun findIntent(intent: String): Intent {
        return findIntent(stories, intent)
    }

    /**
     * Finds a [StoryDefinition] from an [Intent].
     */
    fun findStoryDefinition(intent: IntentAware?): StoryDefinition {
        return if (intent is StoryDefinition) {
            intent
        } else {
            findStoryDefinition(intent?.wrappedIntent()?.name)
        }
    }

    /**
     * Finds a [StoryDefinition] from an intent name.
     */
    fun findStoryDefinition(intent: String?): StoryDefinition {
        return findStoryDefinition(stories, intent, unknownStory, keywordStory)
    }

    /**
     * The unknown story. Used where no valid intent is found.
     */
    val unknownStory: StoryDefinition

    /**
     * The default unknown answer.
     */
    val defaultUnknownAnswer: I18nLabelValue

    /**
     * To handle keywords - used to bypass nlp.
     */
    val keywordStory: StoryDefinition

    /**
     * The hello story. Used for first interaction with no other input.
     */
    val helloStory: StoryDefinition?

    /**
     * The goodbye story. Used when closing the conversation.
     */
    val goodbyeStory: StoryDefinition?

    /**
     * The no input story. When user does nothing!
     */
    val noInputStory: StoryDefinition?

    /**
     * The story that handles [fr.vsct.tock.bot.engine.action.SendLocation] action. If it's null, current intent is used.
     */
    val userLocationStory: StoryDefinition?

    /**
     * The story that handles [fr.vsct.tock.bot.engine.action.SendAttachment] action. If it's null, current intent is used.
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
            "Technical error :( sorry!"
        )
    }

    /**
     * To manage deactivation.
     */
    val botDisabledStory: StoryDefinition?


    /**
     * Is this intent disable the bot?
     */
    fun isBotDisabledIntent(intent: Intent?): Boolean =
        intent != null && botDisabledStory?.isStarterIntent(intent) ?: false

    /**
     * To manage reactivation.
     */
    val botEnabledStory: StoryDefinition?

    /**
     * Is this intent is reactivating the bot?
     */
    fun isBotEnabledIntent(intent: Intent?): Boolean =
        intent != null && botEnabledStory?.isStarterIntent(intent) ?: false

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
            override val targetConnectorType: ConnectorType get() = connectorType
            override val contextId: String? get() = contextId

            override fun i18n(defaultLabel: CharSequence, args: List<Any?>): I18nLabelValue {
                return this@BotDefinition.i18n(defaultLabel, args)
            }
        }

    /**
     * Get the default delay between two answers.
     */
    fun defaultDelay(answerIndex: Int): Long =
        if (answerIndex == 0) 0 else DEFAULT_BREATH
}