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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.bot.sentencegeneration.BotSentenceGenerationConfigurationDAO
import ai.tock.bot.admin.model.genai.PlaygroundRequest
import ai.tock.bot.admin.model.genai.SentenceGenerationRequest
import ai.tock.bot.admin.model.genai.model.genai.SentenceParsingRequest
import ai.tock.genai.orchestratorclient.requests.CompletionRequest
import ai.tock.genai.orchestratorclient.requests.Formatter
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorclient.responses.CompletionResponse
import ai.tock.genai.orchestratorclient.responses.SentenceCompletionResponse
import ai.tock.genai.orchestratorclient.responses.SentenceParsingResponse
import ai.tock.genai.orchestratorclient.services.CompletionService
import ai.tock.genai.orchestratorcore.mappers.LLMSettingMapper
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.config.ValidatedSentence
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.WebVerticle
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.Id

object CompletionService {
    private val logger: KLogger = KotlinLogging.logger {}
    private val appConfiguration: ApplicationConfiguration get() = injector.provide()
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
        botId: String,
    ): CompletionResponse? {
        return completionService
            .generate(
                CompletionRequest(
                    llmSetting =
                        LLMSettingMapper.toEntity(
                            dto = request.llmSetting,
                            rawByForce = true,
                        ),
                    prompt = request.prompt,
                    observabilitySetting =
                        ObservabilityService.getObservabilityConfiguration(
                            namespace,
                            botId,
                            enabled = true,
                        )?.setting,
                ),
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
        botId: String,
    ): SentenceCompletionResponse? {
        // Check if feature is configured
        val sentenceGenerationConfig =
            sentenceGenerationConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
                ?: WebVerticle.badRequest(
                    "No configuration of sentence generation feature is defined yet " +
                        "[namespace: $namespace, botId = $botId]",
                )

        // Check if feature is enabled
        if (!sentenceGenerationConfig.enabled) {
            WebVerticle.badRequest(
                "The sentence generation feature is disabled " +
                    "[namespace: $namespace, botId = $botId]",
            )
        }

        // Get LLM Setting and override the temperature
        val llmSetting = sentenceGenerationConfig.llmSetting.copyWithTemperature(request.llmTemperature)

        // Get prompt
        val prompt = sentenceGenerationConfig.prompt ?: sentenceGenerationConfig.initPrompt()

        // Create the inputs map
        val inputs =
            mapOf(
                "locale" to request.locale,
                "nb_sentences" to sentenceGenerationConfig.nbSentences,
                "sentences" to request.sentences,
                "options" to
                    mapOf<String, Any>(
                        "spelling_mistakes" to request.options.spellingMistakes,
                        "sms_language" to request.options.smsLanguage,
                        "abbreviated_language" to request.options.abbreviatedLanguage,
                    ),
            )

        // call the completion service to generate sentences
        return completionService
            .generateSentences(
                CompletionRequest(
                    llmSetting,
                    prompt.copy(inputs = inputs),
                    ObservabilityService.getObservabilityConfiguration(namespace, botId, enabled = true)?.setting,
                ),
            )
    }

    /**
     * Parse sentence
     * @param request [SentenceGenerationRequest] : the sentence generation request
     * @param namespace [String] : the namespace
     * @param botId [String] : the bot id
     * @return [SentenceCompletionResponse]
     */
    fun parseSentence(
        request: SentenceParsingRequest,
        namespace: String,
        botId: String,
        appId: Id<ApplicationDefinition>,
    ): SentenceParsingResponse? {
        // Check if feature is configured
        // TODO MASS new config sentenceParsingConfig
        val sentenceGenerationConfig =
            sentenceGenerationConfigurationDAO.findByNamespaceAndBotId(namespace, botId)
                ?: WebVerticle.badRequest(
                    "No configuration of sentence generation feature is defined yet " +
                        "[namespace: $namespace, botId = $botId]",
                )

        // Check if feature is enabled
        if (!sentenceGenerationConfig.enabled) {
            WebVerticle.badRequest(
                "The sentence generation feature is disabled " +
                    "[namespace: $namespace, botId = $botId]",
            )
        }

        // Get LLM Setting and override the temperature
        val llmSetting = sentenceGenerationConfig.llmSetting

        val result =
            appConfiguration.search(
                query =
                    SentencesQuery(
                        applicationId = appId,
                        size = 1000,
                        status = setOf(ClassifiedSentenceStatus.validated, ClassifiedSentenceStatus.model),
                    ),
            )

        val intents = appConfiguration.getIntentsByApplicationId(appId)

        val intentById = intents.associateBy { it._id }

        val sentences: List<ValidatedSentence> =
            result.sentences.mapNotNull { sentence ->
                val intentName = intentById[sentence.classification.intentId]?.name
                intentName?.let {
                    ValidatedSentence(
                        text = sentence.text,
                        intent = it,
                    )
                }
            }

        // Get prompt
        val prompt =
            PromptTemplate(
                formatter = Formatter.JINJA2.id,
                template =
                    """
                    ## NLU déterministe

                    Tu es un **moteur NLU déterministe**.
                    Tu simules le fonctionnement d’un **modèle NLU classique basé sur la similarité sémantique**.

                    ### Objectif

                    Comparer une phrase utilisateur à un ensemble de **phrases validées**, chacune associée à une **intention existante**, et déterminer :

                    * l’intention la plus proche sémantiquement,
                    * les intentions secondaires possibles si un lien sémantique existe.

                    ### Fonctionnement attendu

                    * Tu compares la phrase utilisateur **uniquement** aux phrases validées fournies.
                    * Chaque phrase validée est associée à **une intention existante**.
                    * Tu choisis l’intention **la plus proche sémantiquement**.
                    * Tu **n’inventes jamais** de nouvelles intentions.
                    * Tu **n’utilises que** les intentions présentes dans les données fournies.
                    * Si aucun rapprochement sémantique pertinent n’existe, l’intention principale doit être **"unknown"**.

                    ### Contraintes STRICTES

                    * Ne jamais créer d’intention inexistante.
                    * Ne jamais reformuler ou enrichir une intention.
                    * Ne jamais expliquer ton raisonnement.
                    * La sortie doit être **STRICTEMENT du JSON valide**, sans aucun texte hors JSON.

                    ## Schéma de sortie STRICT (JSON uniquement)

                    ```json
                    {
                      "language": "",
                      "intent": "",
                      "similarity": "HIGH" | "STRONG" | "MEDIUM" | "LOW" | "AMBIGUOUS",
                      "score": 0,
                      "suggestions": [
                        {
                          "intent": "",
                          "similarity": "HIGH" | "STRONG" | "MEDIUM" | "LOW" | "AMBIGUOUS",
                          "score": 0
                        }
                      ]
                    }
                    ```

                    ## Définition des champs

                    ### `language`

                    * Détecte automatiquement la langue de la phrase utilisateur.

                    ### `intent`

                    * L’intention **la plus proche sémantiquement** parmi les intentions existantes.
                    * `"unknown"` si aucun rapprochement pertinent n’est trouvé.

                    ### `suggestions`

                    * Contient les **autres intentions possibles** ayant un lien sémantique réel.
                    * Maximum **5 éléments**.
                    * Ne jamais inclure l’intention principale.
                    * Liste vide si aucune autre intention pertinente n’existe.

                    ## Similarité sémantique (classification)

                    * **HIGH**
                      Sens quasi équivalent, reformulation directe possible sans perte de sens ni d’intention.

                    * **STRONG**
                      Intention identique et même objectif utilisateur, avec une **légère nuance**
                      (angle différent, précision supplémentaire, implicite vs explicite).

                    * **MEDIUM**
                      Thème commun, mais intention ou portée partiellement différente.

                    * **LOW**
                      Lien sémantique lointain mais existant.

                    * **AMBIGUOUS**
                      Sens très proche, mais **plusieurs intentions possibles**, rendant la décision incertaine.

                    ## Score déterministe (OBLIGATOIRE)

                    Le champ `score` est **strictement dérivé** du champ `similarity`.

                    Aucune autre valeur n’est autorisée.

                    | similarity  | score  |
                    |-------------|--------|
                    | LOW         | 0.0    |
                    | AMBIGUOUS   | 0.5    |
                    | MEDIUM      | 0.6    |
                    | STRONG      | 0.8    |
                    | HIGH        | 1.0    |

                    > Le score doit toujours être **mécaniquement recalculable** à partir de `similarity`.

                    ## Règles de décision

                    1. **Correspondance forte unique**

                        * Une phrase validée est très proche sémantiquement.
                        * → reprendre son intention
                        * → `similarity = HIGH` ou `STRONG`

                    2. **Correspondances multiples**

                        * Plusieurs intentions sont proches.
                        * → choisir la plus pertinente comme `intent`
                        * → placer les autres dans `suggestions` avec leur `similarity` et `score`.

                    3. **Absence de rapprochement pertinent**

                        * Aucun lien sémantique clair.
                        * → `intent = "unknown"`
                        * → `suggestions = []`
                        * → `similarity = LOW`
                        * → `score = 0.0`

                    ## Données fournies (mémoire NLU)

                    Phrases validées (intention + exemples) :
                    ```
                    {{examples}}
                    ```

                    ## Phrase utilisateur à analyser
                    ```
                    {{sentence}}
                    ```

                    """.trimIndent(),
                inputs =
                    mapOf(
                        "sentence" to request.sentence,
                        "examples" to sentences,
                    ),
            )

        // call the completion service to generate sentences
        return completionService
            .parseSentence(
                CompletionRequest(
                    llmSetting,
                    prompt,
                    ObservabilityService.getObservabilityConfiguration(namespace, botId, enabled = true)?.setting,
                ),
            )
    }
}
