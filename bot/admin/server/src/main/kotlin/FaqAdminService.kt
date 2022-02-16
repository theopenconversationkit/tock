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

import ai.tock.bot.admin.model.CreateI18nLabelRequest
import ai.tock.bot.admin.model.FaqDefinitionRequest
import ai.tock.bot.admin.model.FaqDefinitionSearchResult
import ai.tock.bot.admin.model.FaqSearchRequest
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.front.service.faqDefinitionDAO
import ai.tock.nlp.front.shared.config.*
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.UserLogin
import ai.tock.shared.vertx.WebVerticle
import ai.tock.translator.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.litote.kmongo.Id
import java.time.Instant
import java.util.*

object FaqAdminService {

    private val logger = KotlinLogging.logger {}

    private val front = BotAdminService.front
    private val i18nDao: I18nDAO get() = injector.provide()

    private const val FAQ_CATEGORY = "faq"

    /**
     * @param answer : the answer text
     * @param locale : the language
     * @param applicationId :
     * @param intentId
     * @param sentenceStatus
     * Save sentences for classifiedSentences
     * Add the sentencesStatus parameter in contrary of :
     * @see BotAdminService.saveSentence
     */
    private fun saveFaqSentence(
        answer: String,
        locale: Locale,
        applicationId: Id<ApplicationDefinition>,
        intentId: Id<IntentDefinition>,
        sentenceStatus: ClassifiedSentenceStatus,
        user: UserLogin
    ) {
        if (
            front.search(
                SentencesQuery(
                    applicationId = applicationId,
                    language = locale,
                    search = answer,
                    onlyExactMatch = true,
                    intentId = intentId,
                    status = setOf(
                        ClassifiedSentenceStatus.validated, ClassifiedSentenceStatus.model
                    )
                )
            ).total == 0L
        ) {
            front.save(
                ClassifiedSentence(
                    text = answer,
                    language = locale,
                    applicationId = applicationId,
                    creationDate = Instant.now(),
                    updateDate = Instant.now(),
                    status = sentenceStatus,
                    classification = Classification(intentId, emptyList()),
                    lastIntentProbability = 1.0,
                    lastEntityProbability = 1.0,
                    qualifier = user
                )
            )
        }
    }

    /**
     * Save the Frequently asked question into database
     */
    fun saveFAQ(query: FaqDefinitionRequest, userLogin: UserLogin, applicationDefinition: ApplicationDefinition) {
        if (query.utterances.isNotEmpty() && query.title.isNotBlank()
            && query.answer.isNotBlank()
        ) {
            val intent = createOrUpdateIntent(query, applicationDefinition)

            if (intent != null) {
                val i18nLabel = manageI18nLabelUpdate(intent._id, query, applicationDefinition)
                // TODO: do not save again if the uterrance already exists
                query.utterances.forEach {
                    runBlocking {
                        saveFaqSentence(
                            it,
                            query.language,
                            query.applicationId,
                            intent._id,
                            ClassifiedSentenceStatus.validated,
                            userLogin
                        )
                    }
                }
                //TODO : search if FAQDefinition already exists (done in createOrUpdateIntent ?, just an replace so don't needed ?
                val existingFaq = faqDefinitionDAO.getFaqDefinitionByIntentId(intent._id)
                if (existingFaq != null) {
                    faqDefinitionDAO.save(
                        FaqDefinition(
                            intentId = existingFaq.intentId,
                            i18nId = existingFaq.i18nId,
                            tags = existingFaq.tags,
                            creationDate = existingFaq.creationDate,
                            updateDate = Instant.now()
                        )
                    )
                } else {
                    faqDefinitionDAO.save(
                        FaqDefinition(
                            intentId = intent._id,
                            i18nId = i18nLabel._id,
                            tags = query.tags,
                            creationDate = Instant.now(),
                            updateDate = Instant.now()
                        )
                    )
                }

            } else {
                WebVerticle.badRequest("Intent not found")
            }
        } else {
            WebVerticle.badRequest("Missing argument or trouble in query: $query")
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
                    i18nLabels.filter { it._id == faqQueryResult.i18nId }
                        .map { i18nLabel ->
                            faqQueryResult.toFaqDefinitionDetailed(
                                faqQueryResult,
                                i18nLabel
                            )
                        }
                    // flatten to avoid list of set
                }.flatten().distinct().toSet()
            faqResults.addAll(addToFaqRequestSet(fromTockBotDb, applicationDefinition))
        } else {
            // find details from the i18n found in the faqDefinitionDao
            val fromTockFrontDbOnly =
                faqDetailsWithCount.first.map {
                    i18nDao.getLabelById(it.i18nId)!!.let { i18nLabel ->
                        it.toFaqDefinitionDetailed(
                            it,
                            i18nLabel
                        )
                    }
                }.toSet()
            faqResults.addAll(addToFaqRequestSet(fromTockFrontDbOnly, applicationDefinition))
        }
        logger.debug { "faqResults $faqResults" }

        return toFaqDefinitionSearchResult(query.start, faqResults, faqDetailsWithCount.second)
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
                //TODO : use an utterrance from the faq to determine the status and langage (what about multilanguage?, may not happen from faq creation)
                val currentUterrance = faqDefinition.utterances.map { it }
                val currentLanguage = currentUterrance.map { it.language }.first()
                // find status from ClassifiedSentences Status correspondance
                val currentStatus =
                    currentUterrance.map { it.status }.first()
                        .let { findFaqStatusFromClassifiedSentenceStatus(it) }
                FaqDefinitionRequest(
                    faqDefinition._id.toString(),
                    //TODO : multilangage ?
                    currentLanguage,
                    applicationDefinition._id,
                    faqDefinition.creationDate,
                    faqDefinition.updateDate,
                    faqDefinition.faq.name,
                    faqDefinition.faq.description.orEmpty(),
                    //TODO : not a list but only label per Faq definition to proceed thought things
                    faqDefinition.utterances.map { it.text },
                    faqDefinition.tags,
                    //TODO : what about alternatives ?
                    faqDefinition.i18nLabel.i18n.map { it.label }.firstOrNull().orEmpty(),
                    currentStatus,
                    //TODO : one validated and the others not ?
                    faqDefinition.i18nLabel.i18n.map { it.validated }.first(),
                )
            }.toSet()
    }

    private fun findClassifiedSentenceStatus(status: Boolean): Set<ClassifiedSentenceStatus> {
        return if (status) {
            setOf(ClassifiedSentenceStatus.model, ClassifiedSentenceStatus.validated)
        } else {
            setOf(
                ClassifiedSentenceStatus.inbox,
                ClassifiedSentenceStatus.deleted
            )
        }
    }

    private fun findFaqStatusFromClassifiedSentenceStatus(status: ClassifiedSentenceStatus): FaqStatus {
        return when (status) {
            ClassifiedSentenceStatus.model -> FaqStatus.model
            ClassifiedSentenceStatus.validated -> FaqStatus.model
            ClassifiedSentenceStatus.deleted -> FaqStatus.deleted
            ClassifiedSentenceStatus.inbox -> FaqStatus.draft
            else -> {
                logger.error("Cannot retrieve FaqStatus from ClassifiedSentenceStatus ${status}, default to draft")
                FaqStatus.draft
            }
        }
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
        val newIntent = IntentDefinition(
            query.title.trim(),
            applicationDefinition.namespace,
            setOf(applicationDefinition._id),
            entities = emptySet(),
            // label without accents and withespace and numbers and lowercase
            label= StringUtils.deleteWhitespace(StringUtils.replaceAll(StringUtils.stripAccents(query.title.lowercase()),"[^A-Za-z_-]","")),
            description = query.description.trim(),
            category = FAQ_CATEGORY
        )

        return AdminService.createOrUpdateIntent(applicationDefinition.namespace, newIntent)
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
                linkedSetOf(I18nLocalizedLabel(query.language, UserInterfaceType.textChat, query.answer.trim()))
            )
            i18nDao.saveIfNotExist(listOf(i18nLabel))
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
}