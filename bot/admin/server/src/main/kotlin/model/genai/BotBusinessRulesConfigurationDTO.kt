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

package ai.tock.bot.admin.model.genai

import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfiguration
import ai.tock.bot.admin.bot.businessrules.BotBusinessRulesLexiconGroup
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId

data class BotBusinessRulesConfigurationDTO(
    val id: String? = null,
    val coveredTopics: List<String> = emptyList(),
    val excludedTopics: List<String> = emptyList(),
    val lexiconGroups: List<BotBusinessRulesLexiconGroupDTO> = emptyList(),
) {
    constructor(configuration: BotBusinessRulesConfiguration) : this(
        id = configuration._id.toString(),
        coveredTopics = configuration.coveredTopics,
        excludedTopics = configuration.excludedTopics,
        lexiconGroups = configuration.lexiconGroups.map { BotBusinessRulesLexiconGroupDTO(it) },
    )

    fun toBotBusinessRulesConfiguration(
        namespace: String,
        botId: String,
        existingId: Id<BotBusinessRulesConfiguration>? = null,
    ): BotBusinessRulesConfiguration =
        BotBusinessRulesConfiguration(
            _id = existingId ?: id?.toId() ?: newId(),
            namespace = namespace,
            botId = botId,
            coveredTopics = coveredTopics,
            excludedTopics = excludedTopics,
            lexiconGroups = lexiconGroups.map { it.toBotBusinessRulesLexiconGroup() },
        )
}

data class BotBusinessRulesLexiconGroupDTO(
    val id: Int? = null,
    val terms: List<String> = emptyList(),
) {
    constructor(group: BotBusinessRulesLexiconGroup) : this(
        id = group.id,
        terms = group.terms,
    )

    fun toBotBusinessRulesLexiconGroup(): BotBusinessRulesLexiconGroup =
        BotBusinessRulesLexiconGroup(
            id = requireNotNull(id) { "Lexicon group id must be assigned before conversion" },
            terms = terms,
        )
}
