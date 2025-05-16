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

package ai.tock.bot.admin

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.admin.model.FaqDefinitionRequest
import ai.tock.bot.admin.model.FaqDefinitionSearchResult
import ai.tock.bot.admin.model.FaqSearchRequest
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationFeature
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.service.applicationDAO
import ai.tock.nlp.front.service.faqDefinitionDAO
import ai.tock.nlp.front.service.intentDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.FaqSettingsDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.FaqDefinitionDetailed
import ai.tock.nlp.front.shared.config.FaqQueryResult
import ai.tock.nlp.front.shared.config.FaqSettings
import ai.tock.nlp.front.shared.config.FaqSettingsQuery
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.UNKNOWN_USER_LOGIN
import ai.tock.shared.security.UserLogin
import ai.tock.shared.vertx.WebVerticle.Companion.badRequest
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelFilter
import ai.tock.translator.I18nLabelStateFilter
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.util.Locale

object FaqAdminService {

    private val logger = KotlinLogging.logger {}

    private val i18nDao: I18nDAO get() = injector.provide()
    private val classifiedSentenceDAO: ClassifiedSentenceDAO get() = injector.provide()
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val faqSettingsDAO: FaqSettingsDAO get() = injector.provide()
    private val front = FrontClient

    const val FAQ_CATEGORY = "faq"
    private const val UNKNOWN_ANSWER = "UNKNOWN ANSWER"
    internal const val MISSING_UTTERANCE = "MISSING_UTTERANCE"

    private const val WARN_CANNOT_FIND_LABEL = "Could not found an associated i18nLabel"
    private const val WARN_CANNOT_FIND_UTTERANCE = "Could not found an associated ClassifiedSentence"

    /**
     * Make migration:
     * add namespace attribute (referred to Intent's _id) by namespace attribute (referred to Intent's namespace)
     */
    fun makeMigration() {
        faqDefinitionDAO.makeMigration {
            intentDAO.getIntentById(it)?.namespace
        }
    }

    /**
     * Save the Frequently asked question into database
     */
    fun saveFAQ(
        query: FaqDefinitionRequest, userLogin: UserLogin, application: ApplicationDefinition
    ): FaqDefinitionRequest {
        val faqSettings = faqSettingsDAO.getFaqSettingsByApplicationId(application._id)?.toFaqSettingsQuery()
        val intent = createOrUpdateFaqIntent(query, application)

        createOrUpdateUtterances(query, application, intent._id, userLogin)

        val existingFaqInCurrentApplication =
            faqDefinitionDAO.getFaqDefinitionByIntentIdAndBotIdAndNamespace(intent._id, application.name, application.namespace)

        val faqDefinition: FaqDefinition = prepareCreationOrUpdatingFaqDefinition(
            query, application, intent, existingFaqInCurrentApplication
        )
        faqDefinitionDAO.save(faqDefinition)

        // create the story
        createOrUpdateStory(
            query, intent, userLogin, application, faqSettings
        )

        return FaqDefinitionRequest(
            id = faqDefinition._id.toString(),
            intentId = faqDefinition.intentId.toString(),
            language = query.language,
            applicationName = query.applicationName,
            creationDate = faqDefinition.creationDate,
            updateDate = faqDefinition.updateDate,
            title = intent.label.toString(),
            description = intent.description.toString(),
            utterances = query.utterances,
            tags = query.tags,
            answer = query.answer,
            enabled = query.enabled,
            intentName = intent.name,
            footnotes = query.footnotes
        )
    }

    /**
     * Return the FAQ intent.
     * @throws [BadRequestException] if no intent is found.
     * @param query FaqDefinitionRequest
     * @param application ApplicationDefinition
     */
    private fun createOrUpdateFaqIntent(
        query: FaqDefinitionRequest,
        application: ApplicationDefinition
    ): IntentDefinition {
        return if (query.id != null) {
            // Existing FAQ
            val intent = findFaqDefinitionIntent(query.id.toId())
            intent ?: badRequest("Faq (id:${query.id}) intent not found !")
            // Update intent label and description when updating FAQ title
            val intentUpdated = AdminService.createOrUpdateIntent(
                application.namespace,
                intent.copy(
                    label = query.title,
                    description = query.description
                )
            )
            intentUpdated ?: badRequest("Trouble when updating intent : ${query.intentName}")
        } else {
            // New FAQ
            createIntent(query, application) ?: badRequest("Trouble when creating intent : ${query.intentName}")
        }
    }

    /**
     * Create or update the FaqDefinition and save the changes
     */
    private fun prepareCreationOrUpdatingFaqDefinition(
        query: FaqDefinitionRequest,
        application: ApplicationDefinition,
        intent: IntentDefinition,
        existingFaq: FaqDefinition?
    ): FaqDefinition {
        //updating existing faq or creating faq
        return if (existingFaq != null) {
            logger.info { "Updating FAQ \"${intent.label}\"" }
            FaqDefinition(
                _id = existingFaq._id,
                botId = existingFaq.botId,
                namespace = existingFaq.namespace,
                intentId = existingFaq.intentId,
                i18nId = existingFaq.i18nId,
                tags = query.tags,
                enabled = query.enabled,
                creationDate = existingFaq.creationDate,
                updateDate = Instant.now()
            )
        } else {
            logger.info { "Creating FAQ \"${intent.label}\"" }
            FaqDefinition(
                botId = application.name,
                intentId = intent._id,
                namespace = application.namespace,
                i18nId = query.answer._id,
                tags = query.tags,
                enabled = query.enabled,
                creationDate = Instant.now(),
                updateDate = Instant.now()
            )
        }
    }

    /**
     * Create or Update the story associated to the FAQ
     */
    private fun createOrUpdateStory(
        query: FaqDefinitionRequest,
        intent: IntentDefinition,
        userLogin: UserLogin,
        applicationDefinition: ApplicationDefinition,
        faqFaqSettingsQuery: FaqSettingsQuery?
    ) {
        val existingStory = storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
            applicationDefinition.namespace, applicationDefinition.name, intent.name
        )

        val storyDefinitionConfiguration = prepareStoryCreationOrUpdate(
            query, intent, applicationDefinition, existingStory, faqFaqSettingsQuery
        )

        BotAdminService.saveStory(
            applicationDefinition.namespace,
            BotStoryDefinitionConfiguration(storyDefinitionConfiguration, query.answer.defaultLocale, false),
            userLogin,
            intent

        ).also {
            val enabledLog: String = if (query.enabled) "enabled" else "disabled"
            logger.info { "Saved FAQ with story \"${it?.intent?.name}\" $enabledLog" }
        }
    }

    /**
     * Update FAQ story with the settings (Add or Remove the ending rule)
     */
    fun updateAllFaqStoryWithSettings(
        applicationDefinition: ApplicationDefinition, faqSettings: FaqSettings
    ) {

        val listFaq = faqDefinitionDAO.getFaqDefinitionByBotIdAndNamespace(
            applicationDefinition.name,
            applicationDefinition.namespace
        )

        listFaq.forEach {
            val currentIntent = it.intentId.let {
                AdminService.front.getIntentById(it)
            }

            val existingStory = currentIntent?.let {
                storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
                    applicationDefinition.namespace, applicationDefinition.name, currentIntent.name
                )
            }

            existingStory?.let {
                storyDefinitionDAO.save(
                    existingStory.copy(
                        features = prepareEndingFeatures(
                            existingStory, faqSettings
                        )
                    )
                )
            }
        }
    }

    private fun prepareEndingFeatures(
        existingStory: StoryDefinitionConfiguration, faqSettings: FaqSettings
    ): List<StoryDefinitionConfigurationFeature> {
        val features = mutableListOf<StoryDefinitionConfigurationFeature>()
        features.addAll(existingStory.features)

        features.removeIf { feature -> feature.endWithStoryId != null }
        logger.info { "Remove all features ending from FAQ Story '${existingStory.storyId}'" }

        if (faqSettings.satisfactionEnabled) {
            features.add(StoryDefinitionConfigurationFeature(null, true, null, faqSettings.satisfactionStoryId))
            logger.info { "Add the feature ending '${faqSettings.satisfactionStoryId}' to FAQ Story '${existingStory.storyId}'" }
        }

        return features
    }

    /**
     * Create or update the story
     * @param existingStory : the optional existing story
     */
    private fun prepareStoryCreationOrUpdate(
        query: FaqDefinitionRequest,
        intent: IntentDefinition,
        applicationDefinition: ApplicationDefinition,
        existingStory: StoryDefinitionConfiguration?,
        faqFaqSettingsQuery: FaqSettingsQuery?
    ): StoryDefinitionConfiguration {
        val features = prepareStoryFeatures(query.enabled, faqFaqSettingsQuery)
        return if (existingStory != null) {
            StoryDefinitionConfiguration(
                existingStory.storyId,
                applicationDefinition.name,
                existingStory.intent,
                AnswerConfigurationType.simple,
                listOf(
                    SimpleAnswerConfiguration(
                        listOf(
                            SimpleAnswer(
                                key = I18nLabelValue(query.answer),
                                delay = -1,
                                mediaMessage = null,
                                footnotes = query.footnotes
                            )
                        )
                    )
                ),
                1,
                applicationDefinition.namespace,
                existingStory.mandatoryEntities,
                existingStory.steps,
                query.title.trim(),
                FAQ_CATEGORY,
                query.description.trim(),
                query.utterances.first(),
                query.answer.defaultLocale,
                existingStory.configurationName,
                features,
                existingStory._id,
                existingStory.tags,
                existingStory.configuredAnswers,
                existingStory.configuredSteps
            )
        } else {
            StoryDefinitionConfiguration(
                intent.name,
                applicationDefinition.name,
                IntentWithoutNamespace(intent.name),
                AnswerConfigurationType.simple,
                listOf(
                    SimpleAnswerConfiguration(
                        listOf(
                            SimpleAnswer(
                                key = I18nLabelValue(query.answer),
                                delay = -1,
                                mediaMessage = null,
                                footnotes = query.footnotes
                            )
                        )
                    )
                ),
                1,
                applicationDefinition.namespace,
                emptyList(),
                emptyList(),
                intent.label!!,
                FAQ_CATEGORY,
                intent.description!!,
                query.utterances.first(),
                query.answer.defaultLocale,
                features = features
            )
        }
    }

    /**
     * Prepare the story features for the faq
     * @param : enabled : is the Story Enabled : enable the feature concerning the activation rule feature
     * @param : faqFaqSettingsQuery : enable the feature concerning the ending rule feature
     */
    private fun prepareStoryFeatures(
        enabled: Boolean, faqFaqSettingsQuery: FaqSettingsQuery?
    ): MutableList<StoryDefinitionConfigurationFeature> {
        val features = mutableListOf<StoryDefinitionConfigurationFeature>()
        //activation rule feature
        features.add(
            StoryDefinitionConfigurationFeature(
                null, enabled, null, null
            )
        )
        //ending rule feature
        faqFaqSettingsQuery?.let {
            features.add(
                StoryDefinitionConfigurationFeature(
                    null, true, null, it.satisfactionStoryId
                )
            )
        }
        return features
    }

    /**
     * Create or updates questions utterances for the specified intent
     */
    private fun createOrUpdateUtterances(
        query: FaqDefinitionRequest, app: ApplicationDefinition, intentId: Id<IntentDefinition>, userLogin: UserLogin
    ) {
        val sentences: Pair<List<String>, List<ClassifiedSentence>> =
            checkSentencesToAddOrDelete(query.utterances, query.language, app._id, intentId)

        val notYetPresentSentences: List<String> = sentences.first
        notYetPresentSentences.forEach { utterance ->
            BotAdminService.saveSentence(utterance, query.language, app._id, intentId, userLogin)
                .also { logger.info { "Saving classified sentence" } }
        }

        logger.info { "Create ${notYetPresentSentences.size} new utterances for FAQ" }

        // double check : filter only sentence on current applicationId
        val noMorePresentSentences: List<ClassifiedSentence> = sentences.second.filter {
            it.applicationId == app._id
        }
        //pass the empty list if needed
        classifiedSentenceDAO.switchSentencesStatus(noMorePresentSentences, ClassifiedSentenceStatus.deleted)
    }

    /**
     * Check the Classified Sentences to Add or delete
     * And also check presence of a shared intent
     * @return a Pair with on the `first` notYetPresentSentences String and on the `second` noMorePresentSentences
     */
    private fun checkSentencesToAddOrDelete(
        utterances: List<String>,
        locale: Locale,
        applicationId: Id<ApplicationDefinition>,
        intentId: Id<IntentDefinition>,
    ): Pair<List<String>, List<ClassifiedSentence>> {

        val allSentences = classifiedSentenceDAO.search(
            SentencesQuery(
                applicationId = applicationId,
                language = locale,
                intentId = intentId,
                status = setOf(ClassifiedSentenceStatus.validated, ClassifiedSentenceStatus.model),
                //use secondary database
                onlyExactMatch = true,
                //skip limit on search (specified in the search function)
                size = null
            )
        ).sentences

        var existingSentences: Set<ClassifiedSentence> = HashSet()
        var notYetPresentSentences: Set<String> = HashSet()

        utterances.forEach { utterance ->
            val existing = allSentences.firstOrNull { it.text == utterance }
            if (existing != null) {
                existingSentences = existingSentences.plusElement(existing)
            } else {
                notYetPresentSentences = notYetPresentSentences.plusElement(utterance)
            }
        }

        val noMorePresentSentences: Set<ClassifiedSentence> =
            allSentences.toSet().subtract(existingSentences)
                //filter on current applicationId in case of shared intents
                .filter { it.applicationId == applicationId }
                .toSet()

        return Pair(notYetPresentSentences.toList(), noMorePresentSentences.toList())
    }

    fun searchTags(botId: String, namespace: String): List<String> {
        return faqDefinitionDAO.getTags(botId, namespace)
    }

    /**
     * Search and find FAQ and their details in database and convert them to FaqDefinitionSearchResult
     */
    fun searchFAQ(query: FaqSearchRequest, applicationDefinition: ApplicationDefinition): FaqDefinitionSearchResult {
        //find predicates from i18n answers if search not empty
        val i18nLabels = query.search?.let { findPredicatesFrom18nLabels(applicationDefinition, query.search) }
        val i18nIds = i18nLabels?.map { it._id }?.toList()

        //first is the List<FaqQueryResult>
        //second is the total count
        val faqDetailsWithCount = faqDefinitionDAO.getFaqDetailsWithCount(
            query.toFaqQuery(), applicationDefinition, i18nIds
        )

        // Set the i18Label associated with the Faq if exists. Else, set the UNKNOWN_ANSWER.
        val fromTockBotDb =
            mapI18LabelFaqAndConvertToFaqDefinitionRequest(faqDetailsWithCount.first, applicationDefinition)

        // if no data search from tock front Db
        val fromTockFrontDb = if (fromTockBotDb.isEmpty()) {
            searchLabelsFromTockFrontDb(faqDetailsWithCount.first, applicationDefinition)
        } else emptySet()

        val faqResultsTmp = fromTockBotDb.ifEmpty { fromTockFrontDb }

        // feed story data with name and description
        val faqResults = feedFaqDataStory(faqResultsTmp, applicationDefinition)

        logger.debug { "faqResults $faqResults" }
        return toFaqDefinitionSearchResult(query.start, faqResults, faqDetailsWithCount.second)
    }

    /**
     * Feed faq data story especially with story name and description
     *
     * @param faqs: list of FAQ
     * @param applicationDefinition: application definition
     */
    private fun feedFaqDataStory(
        faqs: Set<FaqDefinitionRequest>,
        applicationDefinition: ApplicationDefinition
    ): Set<FaqDefinitionRequest> {
        val intentNames = faqs.map { it.intentName }
        //creates a map with intentName
        val faqStoriesByIntentName: Map<String, StoryDefinitionConfiguration> =
            BotAdminService.findConfiguredStoriesByBotIdAndIntent(
                applicationDefinition.namespace,
                applicationDefinition.name,
                intentNames
            ).associateBy { it.intent.name }

        //lambda to filter a faq on intent name
        val faqWithStoryIntentName: (FaqDefinitionRequest) -> Boolean = {
            faqStoriesByIntentName.keys.contains(it.intentName)
        }

        return faqs
            .filter(faqWithStoryIntentName)
            .map { faq -> faq.updatedFaqSearch(faqStoriesByIntentName) }
            // give back all other elements
            .union(faqs.filterNot(faqWithStoryIntentName))
    }

    /**
     * lambda to update Faq with story name, description and footnotes
     * @param : faqStoriesByIntentName : Map<String, StoryDefinitionConfiguration>
     * @return : FaqDefinitionRequest
     * @see FaqDefinitionRequest
     */
    private val updatedFaqSearch: FaqDefinitionRequest.(Map<String, StoryDefinitionConfiguration>) -> FaqDefinitionRequest =
        {
            // intentName is not empty since it is present in the map
            it[this.intentName]!!.let { story ->
                val footnotes =
                    (story.answers.find { answer -> answer is SimpleAnswerConfiguration } as? SimpleAnswerConfiguration)
                        ?.answers?.firstOrNull()?.footnotes

                this.copy(title = story.name, description = story.description, footnotes = footnotes)
            }
        }

    /**
     * Set the i18Label associated with the Faq if exists. Else, set the UNKNOWN_ANSWER.
     * Then convert FaqQueryResult to FaqDefinitionRequest
     * @param faqQueryResults: the faq query result from the database query
     * @param applicationDefinition: application definition
     */
    private fun mapI18LabelFaqAndConvertToFaqDefinitionRequest(
        faqQueryResults: List<FaqQueryResult>,
        applicationDefinition: ApplicationDefinition
    ): Set<FaqDefinitionRequest> {
        val fromTockBotDb = faqQueryResults.map { faqQueryResult ->
            faqQueryResult.toFaqDefinitionDetailed(faqQueryResult,
                i18nDao.getLabelById(faqQueryResult.i18nId)
                    ?: unknownI18n(applicationDefinition).also { logger.warn { WARN_CANNOT_FIND_LABEL } })
        }.toSet()

        return convertFaqDefinitionDetailedToFaqDefinitionRequest(
            fromTockBotDb, applicationDefinition
        )
    }

    /**
     * Search from the faqQuery the i18Labels associated with the Faq
     * The search begin from Tock front database to find the associated one in the i18nCollection
     * @param faqQuery: the faq query result from the database query
     * @param applicationDefinition: application definition
     */
    private fun searchLabelsFromTockFrontDb(
        faqQuery: List<FaqQueryResult>,
        applicationDefinition: ApplicationDefinition,
    ): Set<FaqDefinitionRequest> {
        val fromTockFrontDbOnly = faqQuery.map {
            i18nDao.getLabelById(it.i18nId).let { i18nLabel ->
                it.toFaqDefinitionDetailed(
                    it,
                    (i18nLabel ?: unknownI18n(applicationDefinition).also { logger.warn { WARN_CANNOT_FIND_LABEL } })
                )
            }
        }.toSet()

        return LinkedHashSet(
            convertFaqDefinitionDetailedToFaqDefinitionRequest(
                fromTockFrontDbOnly, applicationDefinition
            )
        )
    }

    /**
     * Check the existing utterances and create a new fake one if there are missing utterances to show the unexpected MISSING_UTTERANCE
     */
    private fun checkAndInformAnyMissingUtterances(
        applicationDefinition: ApplicationDefinition,
        intentId: Id<IntentDefinition>,
        utterances: List<ClassifiedSentence>
    ): List<ClassifiedSentence> {
        return utterances.ifEmpty {
            listOf(
                fakeMissingUtterance(
                    applicationDefinition, intentId
                )
            ).also { logger.warn { WARN_CANNOT_FIND_UTTERANCE } }
        }
    }

    /**
     * Create a fake unknown i18n label named UNKNOWN_ANSWER
     */
    private fun unknownI18n(applicationDefinition: ApplicationDefinition): I18nLabel {
        val supportedLocale = applicationDefinition.supportedLocales.first()
        val fakeLocalizedLabel = LinkedHashSet<I18nLocalizedLabel>()
        fakeLocalizedLabel.add(I18nLocalizedLabel(supportedLocale, UserInterfaceType.textChat, UNKNOWN_ANSWER))
        return I18nLabel(
            newId(), applicationDefinition.namespace, FAQ_CATEGORY, fakeLocalizedLabel, UNKNOWN_ANSWER, supportedLocale
        )
    }

    /**
     *  Fake the missing utterance when data is not found
     */
    private fun fakeMissingUtterance(
        applicationDefinition: ApplicationDefinition,
        intentId: Id<IntentDefinition>,
    ): ClassifiedSentence {
        val supportedLocale = applicationDefinition.supportedLocales.first()

        return ClassifiedSentence(
            text = MISSING_UTTERANCE,
            language = supportedLocale,
            applicationId = applicationDefinition._id,
            creationDate = Instant.now(),
            updateDate = Instant.now(),
            status = ClassifiedSentenceStatus.inbox,
            classification = Classification(intentId, emptyList()),
            lastIntentProbability = 1.0,
            lastEntityProbability = 1.0,
            qualifier = UNKNOWN_USER_LOGIN
        )
    }

    private fun toFaqDefinitionSearchResult(
        start: Long, faqSet: Set<FaqDefinitionRequest>, count: Long
    ): FaqDefinitionSearchResult {
        logger.debug { "FaqSet $faqSet" }
        return FaqDefinitionSearchResult(count, start, start + faqSet.toList().size, faqSet.toList())
    }

    /**
     * Create a set or FaqDefinitionRequest from FaqDefinitionDetailed
     * @sample FaqDefinitionRequest
     */
    private fun convertFaqDefinitionDetailedToFaqDefinitionRequest(
        detailedFaqs: Set<FaqDefinitionDetailed>,
        applicationDefinition: ApplicationDefinition,
    ): Set<FaqDefinitionRequest> {
        // filter empty utterances if any empty data to not create them
        return detailedFaqs
            // map the FaqDefinition to the set
            .map { faqDefinition ->
                val currentUtterance = checkAndInformAnyMissingUtterances(
                    applicationDefinition, faqDefinition.intentId, faqDefinition.utterances
                )
                val currentLanguage = currentUtterance.map { it.language }.first()
                FaqDefinitionRequest(
                    id = faqDefinition._id.toString(),
                    intentId = faqDefinition.intentId.toString(),
                    language = currentLanguage,
                    applicationName = applicationDefinition.name,
                    creationDate = faqDefinition.creationDate,
                    updateDate = faqDefinition.updateDate,
                    title = faqDefinition.faq.label.orEmpty(),
                    description = faqDefinition.faq.description.orEmpty(),
                    utterances = currentUtterance.map { it.text },
                    tags = faqDefinition.tags,
                    answer = faqDefinition.i18nLabel,
                    enabled = faqDefinition.enabled,
                    intentName = faqDefinition.faq.name
                )
            }.toSet()

    }

    private fun findPredicatesFrom18nLabels(
        applicationDefinition: ApplicationDefinition,
        search: String,
    ): List<I18nLabel> {
        return i18nDao.getLabels(
            applicationDefinition.namespace, I18nLabelFilter(search, FAQ_CATEGORY, I18nLabelStateFilter.VALIDATED)
        )
    }

    private fun createIntent(
        query: FaqDefinitionRequest, applicationDefinition: ApplicationDefinition
    ): IntentDefinition? {
        val name: String =
            getIntentName(query) ?: badRequest("Trouble when creating/updating intent : Intent name is missing")

        val intent = IntentDefinition(
            name = name,
            namespace = applicationDefinition.namespace,
            applications = setOf(applicationDefinition._id),
            entities = emptySet(),
            // label without accents and whitespace and numbers and lowercase
            label = query.title.trim(),
            description = query.description.trim(),
            category = FAQ_CATEGORY
        )

        logger.debug { "Saved intent $intent for FAQ" }
        return AdminService.createOrUpdateIntent(applicationDefinition.namespace, intent)
    }

    private fun getIntentName(query: FaqDefinitionRequest): String? {
        return if (query.id != null) {
            // On edit mode
            findFaqDefinitionIntent(query.id.toId())?.name
        } else {
            query.intentName
        }
    }

    private fun findFaqDefinitionIntent(faqId: Id<FaqDefinition>): IntentDefinition? {
        return faqDefinitionDAO.getFaqDefinitionById(faqId)?.let {
            intentDAO.getIntentById(it.intentId)
        }
    }

    fun deleteFaqDefinition(namespace: String, faqDefinitionId: String): Boolean {
        val faqDefinition = faqDefinitionDAO.getFaqDefinitionById(faqDefinitionId.toId())
        if (faqDefinition != null) {
            val intent = intentDAO.getIntentById(faqDefinition.intentId)
            if (intent != null) {
                logger.info { "Deleting FAQ Definition \"${intent.label}\"" }
                val application =
                    applicationDAO.getApplicationByNamespaceAndName(namespace, faqDefinition.botId) ?:
                     badRequest("Application not found for namespace $namespace and name ${faqDefinition.botId}")

                return deleteOneFaqDefinition(application, faqDefinition, namespace, intent.name)
            }
        }
        return false
    }

    /**
     * Delete a FaqDefinition when associated with only on application without shared intents
     * @param application The application
     * @param faqDefinition The FaqDefinition
     * @param namespace The application namespace
     * @param intentName Then intent name
     *
     */
    private fun deleteOneFaqDefinition(
        application: ApplicationDefinition,
        faqDefinition: FaqDefinition,
        namespace: String,
        intentName: String
    ): Boolean {

        front.removeIntentFromApplication(application, faqDefinition.intentId)
        faqDefinitionDAO.deleteFaqDefinitionById(faqDefinition._id)

        storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
            application.namespace, application.name, intentName
        )?.let {
            BotAdminService.deleteStory(it.namespace, it._id.toString())
        }

        return true
    }

    fun getSettings(applicationDefinition: ApplicationDefinition): FaqSettingsQuery? {
        return faqSettingsDAO.getFaqSettingsByApplicationId(applicationDefinition._id)?.toFaqSettingsQuery()
    }

    fun saveSettings(
        applicationDefinition: ApplicationDefinition, faqSettingsQuery: FaqSettingsQuery
    ): FaqSettingsQuery {
        val faqSettings = faqSettingsDAO.getFaqSettingsByApplicationId(applicationDefinition._id)

        val faqSettingsUpdated = (faqSettings ?: FaqSettings(
            applicationId = applicationDefinition._id, creationDate = Instant.now(), updateDate = Instant.now()
        )).copy(
            satisfactionEnabled = faqSettingsQuery.satisfactionEnabled,
            satisfactionStoryId = faqSettingsQuery.satisfactionStoryId,
            updateDate = Instant.now()
        )

        faqSettingsDAO.save(faqSettingsUpdated)
        updateAllFaqStoryWithSettings(applicationDefinition, faqSettingsUpdated)

        return faqSettingsUpdated.toFaqSettingsQuery()
    }
}
