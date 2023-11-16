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

package ai.tock.bot.admin.bot.llm.settings.openai

// https://learn.microsoft.com/en-us/azure/ai-services/openai/concepts/models
enum class OpenAIModelDetail(
    val modelName: String,
    val modelId: String,
    val version: String?=null
) {
    // Version 0314 of gpt-4 and gpt-4-32k will be retired no earlier than July 5, 2024
    GPT_4_0314("GPT-4", "gpt-4", "0314"),
    GPT_4_0613("GPT-4", "gpt-4", "0613"),
    GPT_4_32K_0314("GPT-4","gpt-4-32k", "0314"),
    GPT_4_32K_0613("GPT-4","gpt-4-32k", "0613"),

    // Version 0301 of gpt-35-turbo will be retired no earlier than July 5, 2024
    GPT_35_TURBO_0301("GPT-3.5", "gpt-35-turbo", "0301"),
    GPT_35_TURBO_0613("GPT-3.5", "gpt-3.5-turbo", "0613"),

    GPT_35_TURBO_16K_0301("GPT-3.5","gpt-35-turbo-16k", "0301"),
    GPT_35_TURBO_16K_0613("GPT-3.5", "gpt-3.5-turbo-16k", "0613"),
    GPT_35_TURBO_INSTRUCT("GPT-3.5",    "gpt-35-turbo", "instruct"),

    GPT_BASE_BABBAGE("GPT base", "babbage-002"),
    GPT_BASE_DAVINCI("GPT base","davinci-002"),

    EMBEDDING_ADA_002_V1("Embeddings", "text-embedding-ada-002", "1"),
    EMBEDDING_ADA_002_V2("Embeddings", "ext-embedding-ada-002", "2");

    companion object {
        fun findByName(name: String): OpenAIModelDetail? {
            return entries.firstOrNull { it.name == name }
        }
    }
}
