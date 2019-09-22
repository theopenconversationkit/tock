/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.admin.story

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import org.litote.kmongo.Id

/**
 * In order to manage story activation and redirection.
 */
data class StoryDefinitionConfigurationFeature(
    val botApplicationConfigurationId: Id<BotApplicationConfiguration>?,
    val enabled: Boolean = true,
    val switchToStoryId: String?
)