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
import ai.tock.bot.admin.model.SentenceGenerationRequestDTO
import ai.tock.genai.orchestratorclient.requests.Formatter
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorclient.requests.SentenceGenerationQuery
import ai.tock.genai.orchestratorclient.responses.SentenceGenerationResponse
import ai.tock.genai.orchestratorclient.services.CompletionService
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
     * @param request [SentenceGenerationRequestDTO] : the sentence generation request
     * @param namespace [String] : the namespace
     * @param botId [String] : the bot id
     * @return [SentenceGenerationResponse]
     */
    fun generateSentences(request : SentenceGenerationRequestDTO, namespace: String, botId: String ): SentenceGenerationResponse? {
        val sentenceGenerationConfig  = sentenceGenerationConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
            ?: WebVerticle.badRequest("No configuration of sentence generation feature is defined yet [namespace: ${namespace}, botId = ${botId}]")

        val llmSetting = sentenceGenerationConfig.llmSetting
        val inputs = mapOf(
            "locale" to request.locale,
            "nb_sentences" to request.nbSentences,
            "sentences" to request.sentences,
            "options" to mapOf<String, Any>(
                "spelling_mistakes" to request.options.spellingMistakes,
                "sms_language" to request.options.smsLanguage,
                "abbreviated_language" to request.options.spellingMistakes
            )
        )
        val prompt = PromptTemplate(formatter = Formatter.JINJA2, template = llmSetting.prompt, inputs = inputs)

        return completionService
            .generateSentences(SentenceGenerationQuery(llmSetting, prompt))
    }

}
