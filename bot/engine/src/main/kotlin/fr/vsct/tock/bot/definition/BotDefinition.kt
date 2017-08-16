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

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.api.client.model.EntityType
import fr.vsct.tock.shared.withNamespace
import fr.vsct.tock.shared.withoutNamespace
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelKey
import fr.vsct.tock.translator.Translator

/**
 * The main interface of the bot.
 *
 * New bots should usually not directly extend this class, but instead extend [BotDefinitionBase].
 */
interface BotDefinition : I18nKeyProvider {

    companion object {

        fun findIntent(stories: List<StoryDefinition>, intent: String): Intent {
            return stories.flatMap { it.intents }.find { it.name == intent } ?: Intent.unknown
        }

        fun findStoryDefinition(stories: List<StoryDefinition>, intent: String?, unknownStory: StoryDefinition): StoryDefinition {
            return if (intent == null) {
                unknownStory
            } else {
                val i = findIntent(stories, intent)
                stories.find { it.isStarterIntent(i) } ?: unknownStory
            }
        }

    }

    /**
     * The main bot id. Have to be different for each bot.
     */
    val botId: String

    /**
     * The namespace of the bot. Have to be the same namespace than the NLP models.
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
     * This is the method called by the bot after a NLP request.
     * Overrides it if you need more control on intent choice.
     */
    fun findIntentForBot(intent: String, context: IntentContext): Intent {
        return findIntent(intent)
    }

    fun findIntent(intent: String): Intent {
        return findIntent(stories, intent)
    }

    fun findStoryDefinition(intent: Intent?): StoryDefinition {
        return findStoryDefinition(intent?.name)
    }

    fun findStoryDefinition(intent: String?): StoryDefinition {
        return findStoryDefinition(stories, intent, unknownStory)
    }

    /**
     * The unknown story. Used where no valid intent is found.
     */
    val unknownStory: StoryDefinition

    /**
     * The hello story. Used for first interaction with no other input.
     */
    val helloStory: StoryDefinition?

    /**
     * The story that handles [fr.vsct.tock.bot.engine.action.SendLocation] action. If it's null, current intent is used.
     */
    val userLocationStory: StoryDefinition?

    /**
     * The story that handles [fr.vsct.tock.bot.engine.action.SendAttachment] action. If it's null, current intent is used.
     */
    val handleAttachmentStory: StoryDefinition?

    /**
     * To handle custom events. Default implementation does nothing.
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
     * Called when error occurs. By default send "technical error".
     */
    fun errorActionFor(userAction: Action): Action {
        return errorAction(
                userAction.recipientId,
                userAction.applicationId,
                userAction.playerId
        )
    }

    /**
     * To manage deactivation.
     */
    val botDisabledStory: StoryDefinition?

    fun isDisabledIntent(intent: Intent?): Boolean = intent != null && botDisabledStory?.isStarterIntent(intent) ?: false

    val botEnabledStory: StoryDefinition?

    fun isEnabledIntent(intent: Intent?): Boolean = intent != null && botEnabledStory?.isStarterIntent(intent) ?: false

    override fun i18nKeyFromLabel(defaultLabel: String, args: List<Any?>): I18nLabelKey {
        val prefix = javaClass.kotlin.simpleName?.replace("Definition", "") ?: ""
        return i18nKey("${prefix}_${Translator.getKeyFromDefaultLabel(defaultLabel)}", namespace, prefix, defaultLabel, args)
    }

    /**
     * Returns the entity with the specified name and optional role.
     */
    fun entity(name: String, role: String? = null): Entity =
            Entity(
                    EntityType(name.withNamespace(namespace)),
                    role ?: name.withoutNamespace(namespace))
}