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

// https://platform.openai.com/docs/models/continuous-model-upgrades
enum class OpenAILanguageModel(val id: String){
    // GPT-4
    GPT_4         ("gpt-4"),
    GPT_4_0314    ("gpt-4-0314"),
    GPT_4_0613    ("gpt-4-0613"),
    GPT_4_32K     ("gpt-4-32k"),
    GPT_4_32K_0314("gpt-4-32k-0314"),
    GPT_4_32K_0613("gpt-4-32k-0613"),

    // GTP-3.5
    GPT_35_TURBO         ("gpt-3.5-turbo"),
    GPT_35_TURBO_0613    ("gpt-3.5-turbo-0613"),
    GPT_35_TURBO_16K     ("gpt-3.5-turbo-16k"),
    GPT_35_TURBO_16K_0613("gpt-3.5-turbo-16k-0613"),
    GPT_35_TURBO_INSTRUCT("gpt-3.5-turbo-instruct"),

    // GPT base
    GPT_BASE_BABBAGE("babbage-002"),
    GPT_BASE_DAVINCI("davinci-002");

    companion object {
        fun findById(id: String): OpenAILanguageModel? {
            return OpenAILanguageModel.entries.firstOrNull { it.id == id }
        }
    }
}