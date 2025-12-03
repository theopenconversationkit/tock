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

package ai.tock.bot.admin.bot.sentencegeneration

import ai.tock.genai.orchestratorclient.requests.Formatter
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorcore.models.llm.LLMSetting
import org.litote.kmongo.Id

data class BotSentenceGenerationConfiguration(
    val _id: Id<BotSentenceGenerationConfiguration>,
    val namespace: String,
    val botId: String,
    val enabled: Boolean = false,
    val nbSentences: Int,
    val llmSetting: LLMSetting,
    val prompt: PromptTemplate? = null,
) {
    @Deprecated("use BotSentenceGenerationConfiguration#prompt")
    fun initPrompt(): PromptTemplate {
        // Temporary stopgap until the next version of Tock,
        // which will remove the prompt at LLMSetting level and use the promptTemplate
        return PromptTemplate(
            formatter = Formatter.JINJA2.id,
            template = llmSetting.prompt!!,
        )
    }
}
