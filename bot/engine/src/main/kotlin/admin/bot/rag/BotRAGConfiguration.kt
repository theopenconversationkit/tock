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

package ai.tock.bot.admin.bot.rag

import ai.tock.genai.orchestratorclient.requests.Formatter
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorcore.models.em.EMSetting
import ai.tock.genai.orchestratorcore.models.llm.LLMSetting
import org.litote.kmongo.Id

data class BotRAGConfiguration(
    val _id: Id<BotRAGConfiguration>,
    val namespace: String,
    val botId: String,
    val enabled: Boolean,
    val questionCondensingLlmSetting: LLMSetting? = null,
    val questionCondensingPrompt: PromptTemplate? = null,
    val questionAnsweringLlmSetting: LLMSetting? = null,
    val questionAnsweringPrompt: PromptTemplate? = null,
    @Deprecated("use BotRAGConfiguration#questionAnsweringLlmSetting")
    val llmSetting: LLMSetting? = null,
    val emSetting: EMSetting,
    val indexSessionId: String? = null,
    val noAnswerSentence: String,
    val noAnswerStoryId: String? = null,
    val documentsRequired: Boolean = true,
    val debugEnabled: Boolean = false,
    val maxDocumentsRetrieved: Int = 4,
    val maxMessagesFromHistory: Int = 5,
) {
    @Deprecated("use BotRAGConfiguration#questionAnsweringLlmSetting")
    fun initQuestionAnsweringPrompt(): PromptTemplate {
        // Temporary stopgap until the next version of Tock,
        // which will remove the prompt at LLMSetting level and use the promptTemplate
        return PromptTemplate(
            formatter = Formatter.F_STRING.id,
            template = getQuestionAnsweringLLMSetting().prompt!!,
        )
    }

    @Deprecated("use BotRAGConfiguration#questionAnsweringLlmSetting")
    fun getQuestionAnsweringLLMSetting(): LLMSetting = (questionAnsweringLlmSetting ?: llmSetting)!!
}
