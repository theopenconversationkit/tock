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

package ai.tock.bot.admin.utils

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.model.*
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.engine.dialog.SatisfactionStoryEnum
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import org.litote.kmongo.newId

object SatisfactionUtils {


    fun initSatisfactionRatingStory(query: ApplicationScopedQuery): CreateStoryRequest {
        val botList = mutableListOf<BotStoryDefinitionConfigurationStep>()
        for (i in 1..5) {
            val note = BotAdminService.createI18nRequest(
                query.namespace,
                CreateI18nLabelRequest(i.toString(), query.currentLanguage, AnswerConfigurationType.builtin.name)
            )
            val botStory = BotStoryDefinitionConfigurationStep(
                "",
                IntentWithoutNamespace(SatisfactionStoryEnum.STORY_REVIEW_ASK_ID.storyId),
                null,
                emptyList(),
                AnswerConfigurationType.simple,
                AnswerConfigurationType.builtin.name,
                note,
                emptyList(),
                0,
                null,
                null,
                null
            )
            botList.add(botStory)
        }

        val labelRating = BotAdminService.createI18nRequest(
            query.namespace,
            CreateI18nLabelRequest(
                "Comment évaluerez-vous votre expérience avec le Chatbot ?",
                query.currentLanguage,
                AnswerConfigurationType.builtin.name
            )
        )
        val answers =
            listOf<BotAnswerConfiguration>(BotSimpleAnswerConfiguration(listOf(BotSimpleAnswer(labelRating, -1))))
        return CreateStoryRequest(
            BotStoryDefinitionConfiguration(
                SatisfactionStoryEnum.STORY_SATISFACTION_ID.storyId,
                query.applicationName,
                IntentWithoutNamespace(SatisfactionStoryEnum.STORY_SATISFACTION_ID.storyId),
                AnswerConfigurationType.simple,
                labelRating.namespace,
                answers,
                emptyList(),
                botList,
                SatisfactionStoryEnum.STORY_SATISFACTION_ID.storyId,
                AnswerConfigurationType.builtin.name,
                "satisfaction rating story",
                SatisfactionStoryEnum.STORY_SATISFACTION_ID.storyId,
                labelRating.defaultLocale,
                null,
                emptyList(),
                emptySet(),
                emptyList(),
                emptyList(),
                newId(),
                emptyList()
            ), labelRating.defaultLocale, listOf(SatisfactionStoryEnum.STORY_SATISFACTION_ID.storyId)
        )
    }


    fun initSatisfactionCommentStory(query: ApplicationScopedQuery): CreateStoryRequest {
        val labelReview = BotAdminService.createI18nRequest(
            query.namespace,
            CreateI18nLabelRequest(
                "Merci de laisser votre commentaire ou suggestion :",
                query.currentLanguage,
                AnswerConfigurationType.builtin.name
            )
        )

        val answersComment =
            listOf<BotAnswerConfiguration>(BotSimpleAnswerConfiguration(listOf(BotSimpleAnswer(labelReview, -1))))
        return CreateStoryRequest(
            BotStoryDefinitionConfiguration(
                SatisfactionStoryEnum.STORY_REVIEW_ID.storyId,
                query.applicationName,
                IntentWithoutNamespace(SatisfactionStoryEnum.STORY_REVIEW_ID.storyId),
                AnswerConfigurationType.simple,
                labelReview.namespace,
                answersComment,
                emptyList(),
                emptyList(),
                SatisfactionStoryEnum.STORY_REVIEW_ID.storyId,
                AnswerConfigurationType.builtin.name,
                "satisfaction review story",
                "builtinsatisfactionaskforcomment",
                labelReview.defaultLocale,
                null,
                emptyList(),
                emptySet(),
                emptyList(),
                emptyList(),
                newId(),
                emptyList()
            ), labelReview.defaultLocale, listOf(SatisfactionStoryEnum.STORY_REVIEW_ID.storyId)
        )
    }


    fun initSatisfactionCommentAddedStory(query: ApplicationScopedQuery): CreateStoryRequest {
        val labelReview = BotAdminService.createI18nRequest(
            query.namespace,
            CreateI18nLabelRequest(
                "Merci pour votre retour !",
                query.currentLanguage,
                AnswerConfigurationType.builtin.name
            )
        )

        val answersComment =
            listOf<BotAnswerConfiguration>(BotSimpleAnswerConfiguration(listOf(BotSimpleAnswer(labelReview, -1))))
        return CreateStoryRequest(
            BotStoryDefinitionConfiguration(
                SatisfactionStoryEnum.STORY_REVIEW_ADDED_ID.storyId,
                query.applicationName,
                IntentWithoutNamespace(SatisfactionStoryEnum.STORY_REVIEW_ADDED_ID.storyId),
                AnswerConfigurationType.simple,
                labelReview.namespace,
                answersComment,
                emptyList(),
                emptyList(),
                SatisfactionStoryEnum.STORY_REVIEW_ADDED_ID.storyId,
                AnswerConfigurationType.builtin.name,
                "thank user after satisfaction",
                SatisfactionStoryEnum.STORY_REVIEW_ADDED_ID.storyId,
                labelReview.defaultLocale,
                null,
                emptyList(),
                emptySet(),
                emptyList(),
                emptyList(),
                newId(),
                emptyList()
            ), labelReview.defaultLocale, listOf(SatisfactionStoryEnum.STORY_REVIEW_ADDED_ID.storyId)
        )
    }


    fun initSatisfactionReviewAskStory(query: ApplicationScopedQuery): CreateStoryRequest {
        val botList = mutableListOf<BotStoryDefinitionConfigurationStep>()
        val yesResponse = BotAdminService.createI18nRequest(
            query.namespace,
            CreateI18nLabelRequest("Oui", query.currentLanguage, AnswerConfigurationType.builtin.name)
        )
        val botStoryYes = BotStoryDefinitionConfigurationStep(
            "",
            IntentWithoutNamespace(SatisfactionStoryEnum.STORY_REVIEW_ID.storyId),
            null,
            emptyList(),
            AnswerConfigurationType.simple,
            AnswerConfigurationType.builtin.name,
            yesResponse,
            emptyList(),
            0,
            null,
            null,
            null
        )
        val noReponse = BotAdminService.createI18nRequest(
            query.namespace,
            CreateI18nLabelRequest("Non", query.currentLanguage, AnswerConfigurationType.builtin.name)
        )
        val botStoryNo = BotStoryDefinitionConfigurationStep(
            "",
            IntentWithoutNamespace(SatisfactionStoryEnum.STORY_REVIEW_ADDED_ID.storyId),
            null,
            emptyList(),
            AnswerConfigurationType.simple,
            AnswerConfigurationType.builtin.name,
            noReponse,
            emptyList(),
            0,
            null,
            null,
            null
        )
        botList.addAll(listOf(botStoryNo, botStoryYes))
        val labelRating = BotAdminService.createI18nRequest(
            query.namespace,
            CreateI18nLabelRequest(
                "Voulez-vous laisser un commentaire ?",
                query.currentLanguage,
                AnswerConfigurationType.builtin.name
            )
        )
        val answers =
            listOf<BotAnswerConfiguration>(BotSimpleAnswerConfiguration(listOf(BotSimpleAnswer(labelRating, -1))))
        return CreateStoryRequest(
            BotStoryDefinitionConfiguration(
                SatisfactionStoryEnum.STORY_REVIEW_ASK_ID.storyId,
                query.applicationName,
                IntentWithoutNamespace(SatisfactionStoryEnum.STORY_REVIEW_ASK_ID.storyId),
                AnswerConfigurationType.simple,
                labelRating.namespace,
                answers,
                emptyList(),
                botList,
                SatisfactionStoryEnum.STORY_REVIEW_ASK_ID.storyId,
                AnswerConfigurationType.builtin.name,
                "satisfaction review ask story",
                SatisfactionStoryEnum.STORY_REVIEW_ASK_ID.storyId,
                labelRating.defaultLocale,
                null,
                listOf(
                    StoryDefinitionConfigurationFeature(
                        null,
                        true,
                        switchToStoryId = SatisfactionStoryEnum.STORY_REVIEW_ADDED_ID.storyId,
                        endWithStoryId = null
                    )
                ),
                emptySet(),
                emptyList(),
                emptyList(),
                newId(),
                emptyList()
            ), labelRating.defaultLocale, listOf(SatisfactionStoryEnum.STORY_REVIEW_ASK_ID.storyId)
        )
    }
}