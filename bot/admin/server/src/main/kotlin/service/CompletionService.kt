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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.bot.sentencegeneration.BotSentenceGenerationConfigurationDAO
import ai.tock.bot.admin.model.genai.PlaygroundRequest
import ai.tock.bot.admin.model.genai.SentenceGenerationRequest
import ai.tock.genai.orchestratorclient.requests.CompletionRequest
import ai.tock.genai.orchestratorclient.responses.CompletionResponse
import ai.tock.genai.orchestratorclient.responses.SentenceCompletionResponse
import ai.tock.genai.orchestratorclient.services.CompletionService
import ai.tock.genai.orchestratorcore.mappers.LLMSettingMapper
import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import mu.KLogger
import mu.KotlinLogging


object CompletionService {

    private val logger: KLogger = KotlinLogging.logger {}
    private val completionService: CompletionService get() = injector.provide()
    private val sentenceGenerationConfigurationDAO: BotSentenceGenerationConfigurationDAO get() = injector.provide()

    /**
     * Generate sentences
     * @param request [CompletionRequest] : the playground request
     * @param namespace [String] : the namespace
     * @param botId [String] : the bot id
     * @return [SentenceCompletionResponse]
     */
    fun generate(
        request: PlaygroundRequest,
        namespace: String,
        botId: String
    ): CompletionResponse? {
        return completionService
            .generate(
                CompletionRequest(
                    llmSetting = LLMSettingMapper.toEntity(
                        dto = request.llmSetting,
                        rawByForce = true
                    ),
                    prompt = request.prompt,
                    observabilitySetting = ObservabilityService.getObservabilityConfiguration(
                        namespace,
                        botId,
                        enabled = true
                    )?.setting
                )
            )
    }

    /**
     * Generate sentences
     * @param request [SentenceGenerationRequest] : the sentence generation request
     * @param namespace [String] : the namespace
     * @param botId [String] : the bot id
     * @return [SentenceCompletionResponse]
     */
    fun generateSentences(
        request: SentenceGenerationRequest,
        namespace: String,
        botId: String
    ): SentenceCompletionResponse? {
        // Check if feature is configured
        val sentenceGenerationConfig = sentenceGenerationConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
            ?: WebVerticle.badRequest(
                "No configuration of sentence generation feature is defined yet " +
                        "[namespace: ${namespace}, botId = ${botId}]"
            )

        // Check if feature is enabled
        if (!sentenceGenerationConfig.enabled) {
            WebVerticle.badRequest(
                "The sentence generation feature is disabled " +
                        "[namespace: ${namespace}, botId = ${botId}]"
            )
        }

        // Get LLM Setting and override the temperature
        val llmSetting = sentenceGenerationConfig.llmSetting.copyWithTemperature(request.llmTemperature)

        // Get prompt
        val prompt = sentenceGenerationConfig.prompt ?: sentenceGenerationConfig.initPrompt()

        // Create the inputs map
        val inputs = mapOf(
            "locale" to request.locale,
            "nb_sentences" to sentenceGenerationConfig.nbSentences,
            "sentences" to request.sentences,
            "options" to mapOf<String, Any>(
                "spelling_mistakes" to request.options.spellingMistakes,
                "sms_language" to request.options.smsLanguage,
                "abbreviated_language" to request.options.abbreviatedLanguage
            )
        )

        // call the completion service to generate sentences
        return completionService
            .generateSentences(
                CompletionRequest(
                    llmSetting, prompt.copy(inputs = inputs),
                    ObservabilityService.getObservabilityConfiguration(namespace, botId, enabled = true)?.setting
                )
            )
    }
}
