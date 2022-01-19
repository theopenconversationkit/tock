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
import ai.tock.bot.admin.model.SearchFaqRequest
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.front.service.intentDAO
import ai.tock.nlp.front.service.faqDefinitionDAO
import ai.tock.nlp.front.shared.config.*
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.UserLogin
import ai.tock.shared.vertx.WebVerticle
import ai.tock.translator.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.time.Instant
import java.util.*
import kotlin.collections.HashSet

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
                val faqDefinition = FaqDefinition(intentId = intent._id, i18nId = i18nLabel._id, tags = query.tags)
                faqDefinitionDAO.save(faqDefinition)
            } else {
                WebVerticle.badRequest("Intent not found")
            }
        } else {
            WebVerticle.badRequest("Missing argument or trouble in query: $query")
        }
    }

    private fun createOrUpdateIntent(
        query: FaqDefinitionRequest,
        applicationDefinition: ApplicationDefinition
    ): IntentDefinition? {
        val newIntent = IntentDefinition(
            query.title,
            applicationDefinition.namespace,
            setOf(applicationDefinition._id),
            entities = emptySet(),
            description = query.description,
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
        val faqItem = faqDefinitionDAO.getQAItemByIntentId(intentId)
        //Remove the existing label in case the value is the same or no more the same
        return if (faqItem != null && faqItem.i18nId.toString().isNotBlank()) {
            //update existing label
            val i18nLabel = I18nLabel(
                faqItem.i18nId,
                applicationDefinition.namespace,
                FAQ_CATEGORY,
                linkedSetOf(I18nLocalizedLabel(query.language, UserInterfaceType.textChat, query.answer))
            )
            i18nDao.save(i18nLabel)
            i18nLabel
        } else {
            BotAdminService.createI18nRequest(
                applicationDefinition.namespace, CreateI18nLabelRequest(
                    query.answer, query.language,
                    FAQ_CATEGORY
                )
            )
        }
    }
}