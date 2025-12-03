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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.ParameterKey
import ai.tock.bot.engine.config.ReviewParameter.REVIEW_COMMENT_PARAMETER
import ai.tock.bot.engine.config.SatisfactionIntent.REVIEW_ASK
import ai.tock.bot.engine.config.SatisfactionIntent.REVIEW_COMMENT
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.nlp.api.client.model.NlpIntentQualifier

const val SATISFACTION_MODULE_ID: String = "satisfaction_review"

enum class SatisfactionIntent(val id: String) {
    REVIEW_COMMENT("satisfaction_review_comment"),
    REVIEW_ASK("satisfaction_review_ask"),
    REVIEW_ADDED("satisfaction_review_added"),
    NOTE("satisfaction_note"),
    YES("satisfaction_yes"),
    NO("satisfaction_no"),
    SATISFACTION(SATISFACTION_MODULE_ID),
    ;

    val intent: IntentWithoutNamespace = IntentWithoutNamespace(id)
}

private enum class ReviewParameter : ParameterKey { REVIEW_COMMENT_PARAMETER }

private val satisfactionModule =
    BotConfigurationModule(
        SATISFACTION_MODULE_ID,
        listOf(
            BotConfigurationStoryHandlerBase(REVIEW_ASK.id) {
                val storyReview = botDefinition.findStoryDefinitionById(SATISFACTION_MODULE_ID, connectorId)
                val ratingIndex =
                    storyReview.steps.indexOfFirst {
                        (it as? StoryDefinitionConfigurationStep.Step)
                            ?.configuration?.userSentenceLabel?.defaultLabel == userText?.trim()
                    }
                if (ratingIndex >= 0) {
                    dialog.rating = ratingIndex + 1
                    changeContextValue(REVIEW_COMMENT_PARAMETER, false)
                }
            },
            BotConfigurationStoryHandlerBase(REVIEW_COMMENT.id) {
                if (contextValue<Boolean>(REVIEW_COMMENT_PARAMETER) != true) {
                    nextUserActionState =
                        NextUserActionState(
                            listOf(
                                NlpIntentQualifier(REVIEW_COMMENT.id),
                            ),
                        )
                    changeContextValue(REVIEW_COMMENT_PARAMETER, true)
                } else {
                    dialog.review = userText
                    handleAndSwitchStory(botDefinition.findStoryDefinitionById(SatisfactionIntent.REVIEW_ADDED.id, connectorId))
                }
            },
        ),
    )

internal class CoreConfigurationModuleServiceLoader : ConfigurationModuleServiceLoader {
    override fun modules(): Set<BotConfigurationModule> = setOf(satisfactionModule)
}
