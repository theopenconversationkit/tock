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

package ai.tock.bot.admin.bot.llm


import ai.tock.bot.admin.bot.llm.settings.EMSetting
import ai.tock.bot.admin.bot.llm.settings.LLMSetting
import ai.tock.bot.admin.bot.llm.settings.azureopenai.AzureOpenAIEMSetting
import ai.tock.bot.admin.bot.llm.settings.azureopenai.AzureOpenAILLMSetting
import ai.tock.bot.admin.bot.llm.settings.azureopenai.AzureOpenAIVersion
import ai.tock.bot.admin.bot.llm.settings.openai.OpenAIEMSetting
import ai.tock.bot.admin.bot.llm.settings.openai.OpenAIEmbeddingModel
import ai.tock.bot.admin.bot.llm.settings.openai.OpenAILLMSetting
import ai.tock.bot.admin.bot.llm.settings.openai.OpenAILanguageModel

object RagValidationService {

    // TODO MASS : improve the validation. Workshop ?
    fun validate(ragConfig: BotRAGConfiguration): Set<String> =
        validateLLMSetting(ragConfig.llmSetting) + validateEMSetting(ragConfig.emSetting)

    private fun validateLLMSetting(setting: LLMSetting): Set<String> {
        return when(setting){
            is OpenAILLMSetting -> validateOpenAILLMSetting(setting)
            is AzureOpenAILLMSetting -> validateAzureOpenAILLMSetting(setting)
            else -> setOf("Unknown LLM setting")
        }
    }

    private fun validateEMSetting(setting: EMSetting): Set<String> {
        return when(setting){
            is OpenAIEMSetting -> validateOpenAIEMSetting(setting)
            is AzureOpenAIEMSetting -> validateAzureOpenAIEMSetting(setting)
            else -> setOf("Unknown EM setting")
        }
    }


    private fun validateOpenAILLMSetting(setting: OpenAILLMSetting): Set<String> =
        validate(setting)

    private fun validateOpenAIEMSetting(setting: OpenAIEMSetting): Set<String> =
        validate(setting)

    private fun validateAzureOpenAILLMSetting(setting: AzureOpenAILLMSetting): Set<String> {
        val errors = mutableSetOf<String>()

        errors.addAll(validate(setting))

        AzureOpenAIVersion.findByVersion(setting.apiVersion)
            ?: errors.add("Unknown API version : ${setting.apiVersion}")

        return errors
    }

    private fun validateAzureOpenAIEMSetting(setting: AzureOpenAIEMSetting): Set<String> {
        val errors = mutableSetOf<String>()

        errors.addAll(validate(setting))

        AzureOpenAIVersion.findByVersion(setting.apiVersion)
            ?: errors.add("Unknown API version : ${setting.apiVersion}")

        return errors
    }

    private fun validate(setting: LLMSetting): Set<String> {
        val errors = mutableSetOf<String>()

        if(setting.apiKey.isBlank()) {
            errors.add("The API key is not provided")
        }

        OpenAILanguageModel.findById(setting.model)
            ?: errors.add("Unknown model : ${setting.model}")

        if (setting.temperature.isBlank()) {
            errors.add("The temperature is not provided")
        } else if (setting.temperature.toDouble() !in 0.0..1.0) {
            errors.add("The temperature is not correct [0..1]")
        }

        if (setting.prompt.isBlank()) {
            errors.add("The prompt is not provided")
        }


        return errors
    }

    private fun validate(setting: EMSetting): Set<String> {
        val errors = mutableSetOf<String>()

        if(setting.apiKey.isBlank()) {
            errors.add("The API key is not provided")
        }

        OpenAIEmbeddingModel.findById(setting.model)
            ?: errors.add("Unknown model : ${setting.model}")

        return errors
    }
}