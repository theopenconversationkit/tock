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

package ai.tock.bot.admin.module

import ai.tock.bot.admin.content.AnswerConfigurationContent.Companion.fromLabel
import ai.tock.bot.admin.content.ConfigurationContentModule
import ai.tock.bot.admin.content.StoryDefinitionConfigurationContent
import ai.tock.bot.admin.content.StoryDefinitionConfigurationStepContent
import ai.tock.bot.engine.config.SATISFACTION_MODULE_ID
import ai.tock.bot.engine.config.SatisfactionIntent.NO
import ai.tock.bot.engine.config.SatisfactionIntent.NOTE
import ai.tock.bot.engine.config.SatisfactionIntent.REVIEW_ADDED
import ai.tock.bot.engine.config.SatisfactionIntent.REVIEW_ASK
import ai.tock.bot.engine.config.SatisfactionIntent.REVIEW_COMMENT
import ai.tock.bot.engine.config.SatisfactionIntent.SATISFACTION
import ai.tock.bot.engine.config.SatisfactionIntent.YES

const val SATISFACTION_CATEGORY: String = "satisfaction"

val satisfactionContentModule = ConfigurationContentModule(
        SATISFACTION_MODULE_ID,
        listOf(
                StoryDefinitionConfigurationContent(
                        storyId = SATISFACTION.id,
                        userSentence = "Rate your experience",
                        answers = fromLabel("How would you rate your experience with the Chatbot ?"),
                        category = SATISFACTION_CATEGORY,
                        description = "satisfaction rating story (startup)",
                        steps = (1..5).map { note ->
                            StoryDefinitionConfigurationStepContent(
                                    intent = NOTE.intent,
                                    targetIntent = REVIEW_ASK.intent,
                                    userSentence = note.toString()
                            )
                        },
                ),
                StoryDefinitionConfigurationContent(
                        storyId = REVIEW_ASK.id,
                        answers = fromLabel("Would you like to leave a comment ?"),
                        category = SATISFACTION_CATEGORY,
                        description = "satisfaction review ask",
                        steps = listOf(
                                StoryDefinitionConfigurationStepContent(
                                        intent = YES.intent,
                                        targetIntent = REVIEW_COMMENT.intent,
                                        userSentence = "Yes"
                                ),
                                StoryDefinitionConfigurationStepContent(
                                        intent = NO.intent,
                                        targetIntent = REVIEW_ADDED.intent,
                                        userSentence = "No"
                                )
                        ),
                ),

                StoryDefinitionConfigurationContent(
                        storyId = REVIEW_COMMENT.id,
                        answers = fromLabel("Please leave your comment or suggestion:"),
                        category = SATISFACTION_CATEGORY,
                        description = "satisfaction add comment",
                ),
                StoryDefinitionConfigurationContent(
                        storyId = REVIEW_ADDED.id,
                        answers = fromLabel("Thanks for your feedback !"),
                        category = SATISFACTION_CATEGORY,
                        description = "thank user after satisfaction",
                )
        )
)
