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

package ai.tock.bot.admin.story.dump

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import org.litote.kmongo.Id

data class StoryDefinitionConfigurationFeatureDump(
    val botApplicationConfigurationId: Id<BotApplicationConfiguration>?,
    val enabled: Boolean = true,
    val switchToStoryId: String? = null,
    val endWithStoryId: String? = null
) {
    constructor(def: StoryDefinitionConfigurationFeature) :
        this(
            def.botApplicationConfigurationId,
            def.enabled,
            def.switchToStoryId,
            def.endWithStoryId
        )

    fun toFeature(controller: StoryDefinitionConfigurationDumpController): StoryDefinitionConfigurationFeature? =
        if (controller.keepFeature(this)) {
            StoryDefinitionConfigurationFeature(
                botApplicationConfigurationId,
                enabled,
                switchToStoryId,
                endWithStoryId
            )
        } else {
            null
        }
}