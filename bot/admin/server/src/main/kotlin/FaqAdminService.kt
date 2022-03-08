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

package ai.tock.bot.admin

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.model.*
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.front.service.applicationDAO
import ai.tock.nlp.front.service.faqDefinitionDAO
import ai.tock.nlp.front.service.intentDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.shared.config.*
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.UserLogin
import ai.tock.shared.vertx.WebVerticle
import ai.tock.translator.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.util.*

object FaqAdminService {

    private val logger = KotlinLogging.logger {}

    private val i18nDao: I18nDAO get() = injector.provide()
    private val classifiedSentenceDAO: ClassifiedSentenceDAO get() = injector.provide()
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val front = BotAdminService.front

    private const val FAQ_CATEGORY = "faq"
    private const val UNKNOWN_ANSWER = "UNKNOWN ANSWER"

    /**
     * Save the Frequently asked question into database
     */
    fun saveFAQ(query: FaqDefinitionRequest, userLogin: UserLogin, applicationDefinition: ApplicationDefinition) {
        if (query.utterances.isNotEmpty() && query.title.isNotBlank()
            && query.answer.isNotBlank()
        ) {
            val intent = createOrUpdateIntent(query, applicationDefinition)

            if (intent != null) {
                logger.debug { "Saved intent $intent for FAQ" }
                createOrUpdateUtterances(query, intent._id, userLogin)
                val i18nLabel = manageI18nLabelUpdate(intent._id, query, applicationDefinition)
                val existingFaq = faqDefinitionDAO.getFaqDefinitionByIntentId(intent._id)
                val savedFaq: FaqDefinition
                if (existingFaq != null) {
                    savedFaq = FaqDefinition(
                        _id = existingFaq._id,
                        intentId = existingFaq.intentId,
                        i18nId = existingFaq.i18nId,
                        tags = query.tags,
                        creationDate = existingFaq.creationDate,
                        updateDate = Instant.now()
                    )
                    faqDefinitionDAO.save(savedFaq).also { logger.info { "Updating FAQ \"${intent.label}\"" } }
                } else {
                    savedFaq = FaqDefinition(
                        intentId = intent._id,
                        i18nId = i18nLabel._id,
                        tags = query.tags,
                        creationDate = Instant.now(),
                        updateDate = Instant.now()
                    )
                    faqDefinitionDAO.save(savedFaq).also { logger.info { "Creating FAQ \"${intent.label}\"" } }
                }

                if (query.enabled) {
                    // create the story and intent
                    createOrUpdateStory(query, intent, userLogin, i18nLabel, applicationDefinition)
                    logger.info { "Saved FAQ with story enabled" }
                } else {
                    logger.info { "Saved FAQ without story enabled" }
                }

            } else {
                WebVerticle.badRequest("Intent not found")
            }
        } else {
            WebVerticle.badRequest("Missing argument or trouble in query: $query")
        }
    }

    private fun createOrUpdateStory(
        query: FaqDefinitionRequest,
        intent: IntentDefinition,
        userLogin: UserLogin,
        i18nLabel: I18nLabel,
        applicationDefinition: ApplicationDefinition
    ) {
        val existingStory = storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
            applicationDefinition.namespace,
            applicationDefinition.name,
            intent.name
        )

        if (query.enabled) {
            val storyDefinitionConfiguration =
                createOrUpdateCurrentStory(query, intent, i18nLabel, applicationDefinition, existingStory)
            val savedStory = BotAdminService.saveStory(
                intent.namespace,
                BotStoryDefinitionConfiguration(storyDefinitionConfiguration, i18nLabel.defaultLocale, false),
                userLogin, intent
            )
            logger.debug { "Saved story : $savedStory" }
        } else {
            if (existingStory != null) {
                BotAdminService.deleteStory(intent.namespace, existingStory.storyId)
                    .also { logger.info { "Story disabled \"${existingStory.storyId}\"" } }
            }
        }
    }

    private fun createOrUpdateCurrentStory(
        query: FaqDefinitionRequest,
        intent: IntentDefinition,
        i18nLabel: I18nLabel,
        applicationDefinition: ApplicationDefinition,
        existingStory: StoryDefinitionConfiguration?
    ): StoryDefinitionConfiguration {
        val newSimpleStory: StoryDefinitionConfiguration
        if (existingStory != null) {
            newSimpleStory = StoryDefinitionConfiguration(
                existingStory.storyId,
                applicationDefinition.name,
                existingStory.intent,
                AnswerConfigurationType.simple,
                listOf(SimpleAnswerConfiguration(listOf(SimpleAnswer(I18nLabelValue(i18nLabel), -1, null)))),
                1,
                applicationDefinition.namespace,
                existingStory.mandatoryEntities,
                existingStory.steps,
                intent.name,
                FAQ_CATEGORY,
                intent.description!!,
                query.utterances.first(),
                i18nLabel.defaultLocale,
                existingStory.configurationName,
                existingStory.features,
                existingStory._id,
                existingStory.tags,
                existingStory.configuredAnswers,
                existingStory.configuredSteps
            )
        } else {
            newSimpleStory = StoryDefinitionConfiguration(
                intent.name,
                applicationDefinition.name,
                IntentWithoutNamespace(intent.name),
                AnswerConfigurationType.simple,
                listOf(SimpleAnswerConfiguration(listOf(SimpleAnswer(I18nLabelValue(i18nLabel), -1, null)))),
                1,
                applicationDefinition.namespace,
                emptyList(),
                emptyList(),
                intent.label!!,
                FAQ_CATEGORY,
                intent.description!!,
                query.utterances.first(),
                i18nLabel.defaultLocale
            )
        }

        return newSimpleStory
    }

    /**
     * Create or updates questions uterrances for the specified intent
     */
    private fun createOrUpdateUtterances(
        query: FaqDefinitionRequest,
        intentId: Id<IntentDefinition>,
        userLogin: UserLogin
    ) {
        val sentences: Pair<List<String>, List<ClassifiedSentence>> =
            checkSentencesToAddOrDelete(query.utterances, query.language, query.applicationId, intentId)

        val notYetPresentSentences: List<String> = sentences.first
        notYetPresentSentences.forEach {
            runBlocking {
                saveFaqSentence(
                    it,
                    query.language,
                    query.applicationId,
                    intentId,
                    ClassifiedSentenceStatus.validated,
                    userLogin
                )
            }
        }

        logger.info { "Create ${notYetPresentSentences.size} new utterances for FAQ" }

        val noMorePresentSentences: List<ClassifiedSentence> = sentences.second
        classifiedSentenceDAO.switchSentencesStatus(noMorePresentSentences, ClassifiedSentenceStatus.deleted)
    }

    /**
     * Check the Classified Sentences to Add or delete
     * @return a Pair with the notYetPresentSentences String and noMorePresentSentences
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
        )

        val allCurrentSentences = allSentences.sentences

        var existingSentences: Set<ClassifiedSentence> = HashSet()
        var notYetPresentSentences: Set<String> = HashSet()

        utterances.forEach { utterance ->
            val existing = allCurrentSentences.firstOrNull { it.text == utterance }
            if (existing != null) {
                existingSentences = existingSentences.plusElement(existing)
            } else {
                notYetPresentSentences = notYetPresentSentences.plusElement(utterance)
            }
        }

        val noMorePresentSentences: Set<ClassifiedSentence> =
            allCurrentSentences.toSet().subtract(existingSentences).toSet()

        return Pair(notYetPresentSentences.toList(), noMorePresentSentences.toList())
    }

    /**
     * @param utterance : the utterance text
     * @param locale : the language
     * @param applicationId :
     * @param intentId
     * @param sentenceStatus
     * Save sentences for classifiedSentences
     * Add the sentencesStatus parameter in contrary of :
     * @see BotAdminService.saveSentence
     */
    private fun saveFaqSentence(
        utterance: String,
        locale: Locale,
        applicationId: Id<ApplicationDefinition>,
        intentId: Id<IntentDefinition>,
        sentenceStatus: ClassifiedSentenceStatus,
        user: UserLogin
    ) {
        if (
            classifiedSentenceDAO.search(
                SentencesQuery(
                    applicationId = applicationId,
                    language = locale,
                    search = utterance,
                    onlyExactMatch = true,
                    intentId = intentId,
                    status = setOf(
                        ClassifiedSentenceStatus.validated, ClassifiedSentenceStatus.model
                    )
                )
            ).total == 0L
        ) {
            classifiedSentenceDAO.save(
                ClassifiedSentence(
                    text = utterance,
                    language = locale,
                    applicationId = applicationId,
                    creationDate = Instant.now(),
                    updateDate = Instant.now(),
                    status = sentenceStatus,
                    classification = Classification(intentId, emptyList()),
                    lastIntentProbability = 1.0,
                    lastEntityProbability = 1.0,
                    qualifier = user
                ).also { logger.info { "Saving classified sentence $it" } }
            )

        }
    }

    fun searchTags(applicationId: String): List<String> {
        return faqDefinitionDAO.getTags(applicationId)
    }

    /**
     * Search and find FAQ and their details in database and convert them to
     */
    fun searchFAQ(query: FaqSearchRequest, applicationDefinition: ApplicationDefinition): FaqDefinitionSearchResult {
        val faqResults = LinkedHashSet<FaqDefinitionRequest>()
        //find predicates from i18n answers
        val i18nLabels = query.search?.let { findPredicatesFrom18nLabels(applicationDefinition, query.search) }
        val i18nIds = i18nLabels?.map { it._id }?.toList()

        //first is the List<FaqQueryResult>
        //second is the total count
        val faqDetailsWithCount = faqDefinitionDAO.getFaqDetailsWithCount(
            query.toFaqQuery(query, FaqStatus.draft),
            applicationDefinition._id.toString(),
            i18nIds
        )

        // find details from the previous query with i18n filters
        if (i18nLabels != null && i18nLabels.isNotEmpty()) {
            val fromTockBotDb = faqDetailsWithCount.first
                .map { faqQueryResult ->
                    faqQueryResult.toFaqDefinitionDetailed(
                        faqQueryResult,
                        i18nLabels.firstOrNull { it._id == faqQueryResult.i18nId }
                            ?: unknownI18n(applicationDefinition).also { logger.warn { "Could not found label for \"${it.i18n}\"" } }
                    )
                }.toSet()

            faqResults.addAll(addToFaqRequestSet(fromTockBotDb, applicationDefinition))
        } else {
            // find details from the i18n found in the faqDefinitionDao
            val fromTockFrontDbOnly =
                faqDetailsWithCount.first.map {
                    i18nDao.getLabelById(it.i18nId).let { i18nLabel ->
                        it.toFaqDefinitionDetailed(
                            it,
                            (i18nLabel
                                ?: unknownI18n(applicationDefinition).also { logger.warn { "Could not found label for \"${it.i18n}\"" } })
                        )
                    }
                }.toSet()
            faqResults.addAll(addToFaqRequestSet(fromTockFrontDbOnly, applicationDefinition))
        }
        logger.debug { "faqResults $faqResults" }

        return toFaqDefinitionSearchResult(query.start, faqResults, faqDetailsWithCount.second)
    }

    private fun unknownI18n(applicationDefinition: ApplicationDefinition): I18nLabel {
        val supportedLocale = applicationDefinition.supportedLocales.first()
        val fakeLocalizedLabel = LinkedHashSet<I18nLocalizedLabel>()
        fakeLocalizedLabel.add(I18nLocalizedLabel(supportedLocale, UserInterfaceType.textChat, UNKNOWN_ANSWER))
        return I18nLabel(
            newId(),
            applicationDefinition.namespace,
            FAQ_CATEGORY,
            fakeLocalizedLabel,
            UNKNOWN_ANSWER,
            supportedLocale
        )
    }

    private fun toFaqDefinitionSearchResult(
        start: Long,
        faqSet: Set<FaqDefinitionRequest>,
        count: Long
    ): FaqDefinitionSearchResult {
        logger.debug { "FaqSet $faqSet" }
        return FaqDefinitionSearchResult(count, start, start + faqSet.toList().size, faqSet.toList())
    }

    /**
     * Create a set or FaqDefinitionRequest from FaqDefinitionDetailed
     * @sample FaqDefinitionRequest
     */
    private fun addToFaqRequestSet(
        setFaqDetailed: Set<FaqDefinitionDetailed>,
        applicationDefinition: ApplicationDefinition
    ): Set<FaqDefinitionRequest> {

        return setFaqDetailed
            // filter empty uterrances if any empty data to not create them
            .filter { it.utterances.isNotEmpty() }
            // map the FaqDefinition to the set
            .map { faqDefinition ->
                val currentUtterance = faqDefinition.utterances.map { it }
                val currentLanguage = currentUtterance.map { it.language }.first()
                FaqDefinitionRequest(
                    faqDefinition._id.toString(),
                    faqDefinition.intentId.toString(),
                    currentLanguage,
                    applicationDefinition._id,
                    faqDefinition.creationDate,
                    faqDefinition.updateDate,
                    faqDefinition.faq.label.orEmpty(),
                    faqDefinition.faq.description.orEmpty(),
                    faqDefinition.utterances.map { it.text },
                    faqDefinition.tags,
                    faqDefinition.i18nLabel.i18n.map { it.label }.firstOrNull().orEmpty(),
                    faqDefinition.i18nLabel.i18n.map { it.validated }.first(),
                )
            }.toSet()
    }

    private fun findPredicatesFrom18nLabels(
        applicationDefinition: ApplicationDefinition,
        search: String,
    ): List<I18nLabel> {
        return i18nDao.getLabels(
            applicationDefinition.namespace,
            I18nLabelFilter(search, FAQ_CATEGORY, I18nLabelStateFilter.VALIDATED)
        )
    }

    private fun createOrUpdateIntent(
        query: FaqDefinitionRequest,
        applicationDefinition: ApplicationDefinition
    ): IntentDefinition? {
        val name: String
        val intentId = createOrFindFaqDefinitionIntentId(query)
        // name must not be modified if it already exists
        val foundCurrentIntent = AdminService.front.getIntentById(intentId)
        name = foundCurrentIntent?.name ?: StringUtils.deleteWhitespace(
            RegExUtils.replaceAll(
                StringUtils.stripAccents(query.title.lowercase()),
                "[^A-Za-z_-]",
                ""
            )
        )

        val intent = IntentDefinition(
            name = name,
            namespace = applicationDefinition.namespace,
            applications = setOf(applicationDefinition._id),
            entities = emptySet(),
            // label without accents and whitespace and numbers and lowercase
            label = query.title.trim(),
            description = query.description.trim(),
            category = FAQ_CATEGORY,
            _id = intentId,
        )

        return AdminService.createOrUpdateIntent(applicationDefinition.namespace, intent)
    }


    private fun createOrFindFaqDefinitionIntentId(query: FaqDefinitionRequest): Id<IntentDefinition> {
        return if (query.id != null) {
            faqDefinitionDAO.getFaqDefinitionById(query.id.toId())!!.intentId
        } else {
            newId()
        }
    }

    /**
     * Create or update I18nLabelUpdate
     */
    private fun manageI18nLabelUpdate(
        intentId: Id<IntentDefinition>,
        query: FaqDefinitionRequest,
        applicationDefinition: ApplicationDefinition
    ): I18nLabel {
        val faqItem = faqDefinitionDAO.getFaqDefinitionByIntentId(intentId)
        //Remove the existing label in case the value is the same or no more the same
        return if (faqItem != null && faqItem.i18nId.toString().isNotBlank()) {
            //update existing label
            val i18nLabel = I18nLabel(
                faqItem.i18nId,
                applicationDefinition.namespace,
                FAQ_CATEGORY,
                linkedSetOf(I18nLocalizedLabel(query.language, UserInterfaceType.textChat, query.answer.trim())),
                query.answer.trim(),
                query.language
            )
            i18nDao.save(listOf(i18nLabel))
            i18nLabel
        } else {
            BotAdminService.createI18nRequest(
                applicationDefinition.namespace, CreateI18nLabelRequest(
                    query.answer.trim(), query.language,
                    FAQ_CATEGORY
                )
            )
        }
    }

    fun deleteFaqDefinition(namespace: String, faqDefinitionId: String): Boolean {
        val faqDefinition = faqDefinitionDAO.getFaqDefinitionById(faqDefinitionId.toId())
        if (faqDefinition != null) {
            val intent = intentDAO.getIntentById(faqDefinition.intentId)
            if (intent != null) {
                logger.info { "Deleting FAQ Definition \"${intent.label}\"" }

                val applicationDefinitionIds = intent.applications.map { it }
                if (applicationDefinitionIds.isNotEmpty() && applicationDefinitionIds.size == 1) {
                    val applicationDefinition = applicationDAO.getApplicationById(applicationDefinitionIds.first())
                    if (applicationDefinition != null) {
                        front.removeIntentFromApplication(applicationDefinition, faqDefinition.intentId)
                        faqDefinitionDAO.deleteFaqDefinitionById(faqDefinition._id)
                        i18nDao.deleteByNamespaceAndId(namespace, faqDefinition.i18nId)

                        val existingStory = storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
                            applicationDefinition.namespace,
                            applicationDefinition.name,
                            intent.name
                        )
                        if (existingStory != null) {
                            BotAdminService.deleteStory(existingStory.namespace, existingStory._id.toString())
                        }

                        return true
                    }
                } else {
                    throw NotImplementedError("Multiple application definition found for intent not IMPLEMENTED")
                }
            }
        }
        return false
    }
}