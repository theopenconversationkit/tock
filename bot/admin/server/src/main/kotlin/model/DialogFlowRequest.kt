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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import org.litote.kmongo.Id
import java.time.LocalDateTime

/**
 *
 */
data class DialogFlowRequest(
    val botId: String,
    val botConfigurationName: String?,
    val botConfigurationId: Id<BotApplicationConfiguration>?,
    val from: LocalDateTime? = null,
    val to: LocalDateTime? = null,
    val includeTestConfigurations: Boolean = false,
    val intent: String? = null
) : ApplicationScopedQuery()
