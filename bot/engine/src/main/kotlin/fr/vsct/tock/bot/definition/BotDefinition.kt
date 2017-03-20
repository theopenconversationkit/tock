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
import fr.vsct.tock.bot.i18n.I18n
import fr.vsct.tock.bot.i18n.I18nKeyProvider
import fr.vsct.tock.bot.i18n.I18nLabelKey
import ft.vsct.tock.nlp.api.client.model.NlpEngineType

/**
 *
 */
interface BotDefinition : I18nKeyProvider {

    val botId: String
    val namespace: String
    val nlpApplication: String

    val unknownStory: StoryDefinition

    fun errorAction(playerId: PlayerId, applicationId: String, recipientId: PlayerId): Action {
        return SendSentence(
                playerId,
                applicationId,
                recipientId,
                "Technical error :( sorry!"
        )
    }

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


    val stories: List<StoryDefinition>
    val engineType: NlpEngineType


    fun findIntent(intent: String): Intent {
        return stories.flatMap { it.intents }.find { it.name == intent } ?: Intent.unknown
    }

    fun findStoryDefinition(intent: Intent?): StoryDefinition {
        return findStoryDefinition(intent?.name)
    }

    fun findStoryDefinition(intent: String?): StoryDefinition {
        return if (intent == null) {
            unknownStory
        } else {
            val i = findIntent(intent)
            stories.find { it.isStarterIntent(i) } ?: unknownStory
        }
    }

    override fun i18nKeyFromLabel(defaultLabel: String, vararg args: Any?): I18nLabelKey {
        val prefix = javaClass.kotlin.simpleName?.replace("Definition", "") ?: ""
        return i18nKey("${prefix}_${I18n.getKeyFromDefaultLabel(defaultLabel)}", prefix, defaultLabel, *args)
    }
}