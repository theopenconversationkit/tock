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

package ai.tock.bot.admin.story

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import org.litote.kmongo.Id

/**
 * In order to manage story activation, redirection and handling with configured "end story".
 */
data class StoryDefinitionConfigurationFeature(
    val botApplicationConfigurationId: Id<BotApplicationConfiguration>?,
    val enabled: Boolean = true,
    val switchToStoryId: String?,
    val endWithStoryId: String?
) {
    constructor(
        botApplicationConfigurationId: Id<BotApplicationConfiguration>?,
        enabled: Boolean = true,
        switchToStoryId: String?
    ) : this(botApplicationConfigurationId, enabled, switchToStoryId, null)

    constructor(
        botApplicationConfigurationId: Id<BotApplicationConfiguration>?,
        enabled: Boolean = true,
        endingRedirection: Boolean,
        switchToStoryId: String?
    ) : this(
        botApplicationConfigurationId,
        enabled,
        switchToStoryId.takeUnless { endingRedirection },
        switchToStoryId.takeIf { endingRedirection }
    )

    internal fun supportConfiguration(conf: BotApplicationConfiguration?): Boolean =
        botApplicationConfigurationId == null || (conf != null && supportDedicatedConfiguration(conf))

    internal fun supportDedicatedConfiguration(conf: BotApplicationConfiguration): Boolean =
        botApplicationConfigurationId == conf._id ||
            botApplicationConfigurationId == conf.targetConfigurationId
}
