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

package ai.tock.bot.api.client

import ai.tock.bot.api.model.configuration.ClientConfiguration
import ai.tock.bot.api.model.configuration.ResponseContextVersion
import ai.tock.bot.api.model.configuration.StepConfiguration
import ai.tock.bot.api.model.configuration.StoryConfiguration

fun ClientBotDefinition.toConfiguration(): ClientConfiguration =
    ClientConfiguration(
        stories = stories.map {
            it.mapToStoryConfiguration()
        },
        version = ResponseContextVersion.V2
    )

private fun ClientStoryDefinition.mapToStoryConfiguration(): StoryConfiguration {
    return StoryConfiguration(
        this.mainIntent.wrappedIntent().name,
        this.otherStarterIntents.map { it.wrappedIntent().name }.toSet(),
        this.secondaryIntents.map { it.wrappedIntent().name }.toSet(),
        this.steps.map { step ->
            StepConfiguration(
                step.name,
                step.mainIntent.wrappedIntent().name,
                step.otherStarterIntents.map { it.wrappedIntent().name }.toSet(),
                step.secondaryIntents.map { it.wrappedIntent().name }.toSet()
            )
        }
    )
}
