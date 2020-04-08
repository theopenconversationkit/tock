/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.bot.api.client

import ai.tock.bot.api.model.configuration.ClientConfiguration
import ai.tock.bot.api.model.configuration.StoryConfiguration
import ai.tock.bot.api.model.configuration.StepConfiguration

fun ClientBotDefinition.toConfiguration(): ClientConfiguration =
    ClientConfiguration(
        stories.map { s ->
            StoryConfiguration(
                s.mainIntent.wrappedIntent().name,
                s.otherStarterIntents.map { it.wrappedIntent().name }.toSet(),
                s.secondaryIntents.map { it.wrappedIntent().name }.toSet(),
                s.steps.map { step ->
                    StepConfiguration(
                        step.name,
                        step.mainIntent.wrappedIntent().name,
                        step.otherStarterIntents.map { it.wrappedIntent().name }.toSet(),
                        step.secondaryIntents.map { it.wrappedIntent().name }.toSet()
                    )
                }
            )
        }
    )