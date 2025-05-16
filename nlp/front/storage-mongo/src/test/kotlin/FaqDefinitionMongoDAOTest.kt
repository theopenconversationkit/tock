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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.FaqQuery
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.UserLogin
import ai.tock.translator.I18nLabel
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoCollection
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.test.assertEquals

class FaqDefinitionMongoDAOTest : AbstractTest() {

    private val faqDefinitionDao: FaqDefinitionDAO get() = injector.provide()
    private val intentDefinitionDao: IntentDefinitionDAO get() = injector.provide()
    private val classifiedSentencesDao: ClassifiedSentenceDAO get() = injector.provide()

    private val applicationId = "idApplication".toId<ApplicationDefinition>()
    private val botId = "botId"
    private val botId2 = "botId2"
    private val intentId = "idIntent".toId<IntentDefinition>()
    private val intentId2 = "idIntent2".toId<IntentDefinition>()
    private val intentId3 = "idIntent3".toId<IntentDefinition>()

    private val faqId = "faqDefId".toId<FaqDefinition>()
    private val faqId2 = "faqDefId2".toId<FaqDefinition>()
    private val faqId3 = "faqDefId3".toId<FaqDefinition>()
    private val faqId4 = "faqDefId4".toId<FaqDefinition>()

    private val i18nId = "idI18n".toId<I18nLabel>()
    private val i18nId2 = "idI18n2".toId<I18nLabel>()
    private val i18nId3 = "idI18n3".toId<I18nLabel>()
    private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    private val tagList = listOf("TAG1", "TAG2")

    private val namespace = "namespace"
    private val faqCategory = "faq"
    private val userLogin: UserLogin = "whateverLogin"

    private val mockedApplicationDefinition = ApplicationDefinition(_id= applicationId, name = botId,label=botId, namespace = namespace)

    private val faqDefinition = FaqDefinition(faqId, botId, namespace, intentId, i18nId, tagList, true, now, now)
    private val faq2Definition = FaqDefinition(faqId2,  botId,namespace, intentId2, i18nId2, tagList, true, now, now)
    private val faq3Definition = FaqDefinition(faqId3,botId2,namespace, intentId3, i18nId3, tagList, true, now, now)

    private val col: MongoCollection<FaqDefinition> by lazy { FaqDefinitionMongoDAO.col }

    override fun moreBindingModules() = Kodein.Module {
        bind<FaqDefinitionDAO>() with provider { FaqDefinitionMongoDAO }
        bind<ClassifiedSentenceDAO>() with provider { ClassifiedSentenceMongoDAO }
        bind<IntentDefinitionDAO>() with provider { IntentDefinitionMongoDAO }
    }

    @AfterEach
    fun cleanup() {
        faqDefinitionDao.deleteFaqDefinitionById(faqId)
        faqDefinitionDao.deleteFaqDefinitionById(faqId2)
        faqDefinitionDao.deleteFaqDefinitionById(faqId3)
        faqDefinitionDao.deleteFaqDefinitionById(faqId4)
        intentDefinitionDao.deleteIntentById(intentId)
        intentDefinitionDao.deleteIntentById(intentId2)
        intentDefinitionDao.deleteIntentById(intentId3)
        classifiedSentencesDao.deleteSentencesByApplicationId(applicationId)
        clearProperties()
    }

    fun clearProperties() {
        System.clearProperty(TOCK_DOCUMENT_DB_ON_PROPERTY)
    }

    @Test
    fun `Get a FaqDefinition just saved`() {
        faqDefinitionDao.save(faqDefinition)
        assertEquals(
            expected = faqDefinition,
            actual = faqDefinitionDao.getFaqDefinitionByBotIdAndNamespace(botId, namespace).first(),
            message = "There should be something returned with an applicationId"
        )
        assertEquals(
            expected = faqDefinition,
            actual = faqDefinitionDao.getFaqDefinitionByIntentId(intentId),
            message = "There should be something returned with an intentId"
        )
        assertEquals(
            expected = faqDefinition,
            actual = faqDefinitionDao.getFaqDefinitionById(faqId),
            message = "There should be something returned with an faqId"
        )
        assertEquals(
            expected = faqDefinition,
            actual = faqDefinitionDao.getFaqDefinitionByI18nIds(setOf(i18nId))?.first(),
            message = "There should be something returned with an i18nIds"
        )
        assertEquals(
            expected = faqDefinition,
            actual = faqDefinitionDao.getFaqDefinitionByTags(tagList.toSet()).first(),
            message = "There should be something returned with tags"
        )
        assertEquals(1, col.countDocuments())
    }

    @Test
    fun `Get a FaqDefinition by application ID`() {
        faqDefinitionDao.save(faqDefinition)
        faqDefinitionDao.save(faq2Definition)
        faqDefinitionDao.save(faq3Definition)

        assertEquals(3, col.countDocuments())

        assertEquals(
            expected = 2,
            actual = faqDefinitionDao.getFaqDefinitionByBotIdAndNamespace(botId, namespace).size,
            message = "There should be something returned with an applicationId"
        )
        assertEquals(
            expected = 1,
            actual = faqDefinitionDao.getFaqDefinitionByBotIdAndNamespace(botId2, namespace).size,
            message = "There should be something returned with an applicationId"
        )
    }

    @Test
    fun `Remove a FaqDefinition just saved`() {
        faqDefinitionDao.save(faqDefinition)
        faqDefinitionDao.deleteFaqDefinitionById(faqId)
        assertEquals(0, col.countDocuments())

        assertEquals(
            expected = null,
            actual = faqDefinitionDao.getFaqDefinitionByIntentId(intentId),
            message = "There should be something returned with an intentId"
        )
        assertEquals(
            expected = null,
            actual = faqDefinitionDao.getFaqDefinitionById(faqId),
            message = "There should be something returned with an faqId"
        )
        assertEquals(
            expected = null,
            actual = faqDefinitionDao.getFaqDefinitionByI18nIds(setOf(i18nId))?.firstOrNull(),
            message = "There should be something returned with an i18nIds"
        )
        assertEquals(
            expected = null,
            actual = faqDefinitionDao.getFaqDefinitionByTags(tagList.toSet()).firstOrNull(),
            message = "There should be something returned with tags"
        )
    }

    @Test
    fun `Get a faqDefinition search filtered by tag`() {
        //prepare a Faq save
        faqDefinitionDao.save(faqDefinition)

        //another faq
        val i18nId2 = "idI18n2".toId<I18nLabel>()
        val tagList2 = listOf("TAG1")

        val otherFaqDefinition =
            FaqDefinition(
                faqId2,
                botId,
                namespace,
                intentId2,
                i18nId2,
                tagList2,
                true,
                now.plusSeconds(1),
                now.plusSeconds(1)
            )
        faqDefinitionDao.save(otherFaqDefinition)

        //some another faq
        val i18nId3 = "idI18n3".toId<I18nLabel>()
        val tagList3 = listOf("TAG2")

        val someOtherFaqDefinition =
            FaqDefinition(
                faqId3,
                botId,
                namespace,
                intentId3,
                i18nId3,
                tagList3,
                true,
                now.plusSeconds(2),
                now.plusSeconds(2)
            )
        faqDefinitionDao.save(someOtherFaqDefinition)

        assertEquals(
            expected = 2,
            actual = faqDefinitionDao.getFaqDefinitionByTags(setOf("TAG1")).size,
            message = "There should be something returned with tags"
        )
    }

    @Test
    fun `Delete faq by application id`() {
        val botId1 = "appID1"
        val botId2 = "appID2"

        faqDefinitionDao.save(faqDefinition.copy(botId = botId1))
        faqDefinitionDao.save(faq2Definition.copy(botId = botId1))
        faqDefinitionDao.save(faq3Definition.copy(botId = botId2))

        assertEquals(3, col.countDocuments())

        assertEquals(2, faqDefinitionDao.getFaqDefinitionByBotIdAndNamespace(botId1, namespace).size)

        faqDefinitionDao.deleteFaqDefinitionByBotIdAndNamespace(botId1,namespace)

        assertEquals(0, faqDefinitionDao.getFaqDefinitionByBotIdAndNamespace(botId1, namespace).size)
        assertEquals(1, faqDefinitionDao.getFaqDefinitionByBotIdAndNamespace(botId2, namespace).size)
    }

    @Test
    fun `FaqDefinition search by tag`() {
        //prepare a Faq save
        faqDefinitionDao.save(faqDefinition)

        val firstIntentWithIntentId = IntentDefinition(
            "FAQ name",
            namespace,
            setOf(applicationId),
            emptySet(),
            label = "faqname",
            category = faqCategory,
            _id = intentId
        )

        intentDefinitionDao.save(firstIntentWithIntentId)

        val i18nId = "idI18n3".toId<I18nLabel>()
        val otherTagList = listOf("OTHERTAG")

        val secondFaqDefinition =
            FaqDefinition(
                faqId3,
                botId,
                namespace,
                intentId3,
                i18nId,
                otherTagList,
                true,
                now.plusSeconds(2),
                now.plusSeconds(2)
            )

        val secondIntentWithIntentId3 = IntentDefinition(
            "Faq name 2",
            namespace,
            setOf(applicationId),
            emptySet(),
            label = "faqname2",
            category = faqCategory,
            _id = intentId3
        )

        intentDefinitionDao.save(secondIntentWithIntentId3)
        faqDefinitionDao.save(secondFaqDefinition)

        val tags = faqDefinitionDao.getTags(botId, namespace)
        assertEquals(tags, otherTagList + faqDefinition.tags)
    }

    @Test
    fun `A faqDefinition search with deleted utterances`() {
        val i18nId2 = "idI18n2".toId<I18nLabel>()
        val tagList2 = listOf("TAG1")

        createDataForFaqSearch(faqId2, intentId2, i18nId2, tagList2, applicationId, true)

        //some another faq
        val i18nId3 = "idI18n3".toId<I18nLabel>()
        val tagList3 = listOf("TAG2")

        createDataForFaqSearch(
            faqId3,
            intentId3,
            i18nId3,
            tagList3,
            applicationId,
            true,
            "FAQ name 2",
            utteranceText = "randomText2"
        )

        // third FAQ
        val intentIdtoDel = "intentIdtoDel".toId<IntentDefinition>()
        val i18nIdToDel = "i18nIdtoDel".toId<I18nLabel>()
        val tagListToDel = listOf("TAG2")

        // Faq Definition in deletion with build Worker schedule for example
        val inDeletionFaqDefinition = createDataForFaqSearch(
            faqId,
            intentIdtoDel,
            i18nIdToDel,
            tagListToDel,
            applicationId,
            true,
            "FAQ name in deletion",
            utteranceText = "randomText3"
        )

        //delete the faq
        faqDefinitionDao.deleteFaqDefinitionById(faqId)
        intentDefinitionDao.deleteIntentById(intentIdtoDel)
        val inDeletionFaqDefUtterance = classifiedSentencesDao.getSentences(
            setOf(inDeletionFaqDefinition.intentId),
            Locale.FRENCH,
            ClassifiedSentenceStatus.validated
        )
        classifiedSentencesDao.switchSentencesStatus(inDeletionFaqDefUtterance, ClassifiedSentenceStatus.deleted)

        // search the actual faq
        val searchFound = faqDefinitionDao.getFaqDetailsWithCount(
            //no specific filtering
            createFaqQuery(null, null),
            mockedApplicationDefinition,
            null
        )

        assertEquals(
            expected = 2,
            actual = classifiedSentencesDao.getSentences(
                setOf(intentId3, intentId2, intentIdtoDel),
                Locale.FRENCH,
                ClassifiedSentenceStatus.validated
            ).size,
            message = "There should be two classified sentences"
        )

        assertEquals(
            expected = 1, actual = classifiedSentencesDao.getSentences(
                setOf(intentIdtoDel), Locale.FRENCH,
                ClassifiedSentenceStatus.deleted
            ).size, "There should be one classified sentences deleted"
        )

        assertEquals(
            expected = 2,
            actual = intentDefinitionDao.getIntentByIds(setOf(intentId3, intentId2, intentIdtoDel))?.size,
            message = "There should be two intents"
        )

        assertEquals(searchFound.second, 2, "There should be 2 Faq")

    }

    @Test
    fun `A faqDefinition search with name with deleted utterances`() {

        val i18nId2 = "idI18n2".toId<I18nLabel>()
        val tagList2 = listOf("TAG1")

        createDataForFaqSearch(faqId2, intentId2, i18nId2, tagList2, applicationId, true)

        //some another faq
        val i18nId3 = "idI18n3".toId<I18nLabel>()
        val tagList3 = listOf("TAG2")
        val faqName2 = "FAQ name 2"

        createDataForFaqSearch(
            faqId3,
            intentId3,
            i18nId3,
            tagList3,
            applicationId,
            true,
            faqName2,
            utteranceText = "randomText2"
        )

        // third FAQ
        val intentIdtoDel = "intentIdtoDel".toId<IntentDefinition>()
        val i18nIdToDel = "i18nIdtoDel".toId<I18nLabel>()
        val tagListToDel = listOf("TAG2")

        // Faq Definition in deletion with build Worker schedule for example
        val inDeletionFaqDefinition = createDataForFaqSearch(
            faqId,
            intentIdtoDel,
            i18nIdToDel,
            tagListToDel,
            applicationId,
            true,
            "FAQ name in deletion",
            utteranceText = "randomText3"
        )

        //delete the faq
        faqDefinitionDao.deleteFaqDefinitionById(faqId)
        intentDefinitionDao.deleteIntentById(inDeletionFaqDefinition.intentId)
        val classifiedSentenceToDelete = classifiedSentencesDao.getSentences(
            setOf((inDeletionFaqDefinition.intentId)),
            Locale.FRENCH,
            ClassifiedSentenceStatus.validated
        )
        //delete the classified sentence for the faq
        classifiedSentencesDao.switchSentencesStatus(classifiedSentenceToDelete, ClassifiedSentenceStatus.deleted)

        // search the actual faq
        val searchFound = faqDefinitionDao.getFaqDetailsWithCount(
            //filtering on faqName2
            createFaqQuery(null, faqName2),
            mockedApplicationDefinition,
            null
        )

        assertEquals(
            expected = 2,
            actual = classifiedSentencesDao.getSentences(
                setOf(intentId3, intentId2, intentIdtoDel),
                Locale.FRENCH,
                ClassifiedSentenceStatus.validated
            ).size,
            message = "There should be two classified sentences"
        )

        assertEquals(
            expected = 1, actual = classifiedSentencesDao.getSentences(
                setOf(intentIdtoDel), Locale.FRENCH,
                ClassifiedSentenceStatus.deleted
            ).size, "There should be one classified sentences deleted"
        )

        assertEquals(
            expected = 2,
            actual = intentDefinitionDao.getIntentByIds(setOf(intentId3, intentId2, intentIdtoDel))?.size,
            message = "There should be two intents"
        )

        assertEquals(searchFound.second, 1, "There should be 1 Faq with the text 'FAQ name 2'")
    }

    @Test
    fun `FaqDefinition search must be ordered by creationDate`() {

        val i18nId2 = "idI18n2".toId<I18nLabel>()
        val tagList2 = listOf("TAG1")

        val firstFaq = createDataForFaqSearch(faqId2, intentId2, i18nId2, tagList2, applicationId, true)

        //some another faq
        val i18nId3 = "idI18n3".toId<I18nLabel>()
        val tagList3 = listOf("TAG2")
        val faqName2 = "FAQ name 2"

        createDataForFaqSearch(
            faqId3,
            intentId3,
            i18nId3,
            tagList3,
            applicationId,
            true,
            faqName2,
            utteranceText = "randomText2",
            instant = now.plusSeconds(2)
        )

        // third FAQ
        val intentId4 = "intentId4".toId<IntentDefinition>()
        val i18nId4 = "i18nId4".toId<I18nLabel>()
        val tagList4 = listOf("TAG")

        // the last faq to be created
        val lastFaq = createDataForFaqSearch(
            faqId4,
            intentId4,
            i18nId4,
            tagList4,
            applicationId,
            true,
            "FAQ name 3",
            utteranceText = "randomText3",
            instant = now.plusSeconds(3)
        )

        // search the actual faq
        val searchFound = faqDefinitionDao.getFaqDetailsWithCount(
            //no specific filtering
            createFaqQuery(null, null),
            mockedApplicationDefinition,
            null
        )

        assertEquals(
            expected = 3,
            actual = classifiedSentencesDao.getSentences(
                setOf(intentId3, intentId2, intentId4),
                Locale.FRENCH,
                ClassifiedSentenceStatus.validated
            ).size,
            message = "There should be 3 classified sentences"
        )

        assertEquals(
            expected = 3,
            actual = intentDefinitionDao.getIntentByIds(setOf(intentId3, intentId2, intentId4))?.size,
            message = "There should be two intents"
        )

        assertEquals(
            expected = 3,
            actual = searchFound.second,
            message = "There should be three faq"
        )

        //The last faq in the search list should be the first created because of the creation date order
        assertEquals(
            //compare list of faq
            searchFound.first.last()._id,
            firstFaq._id,
            "The last faq in the list should be the first created because of the creation date order"
        )

        assertEquals(
            searchFound.first.last().botId,
            firstFaq.botId,
            "botId is different than expected"
        )

        assertEquals(
            searchFound.first.last().intentId,
            firstFaq.intentId,
            "intentId is different than expected"
        )

        assertEquals(
            searchFound.first.last().namespace,
            firstFaq.namespace,
            "namespace is different than expected"
        )

        //The first faq in the list should be the last created because of the creation date order
        assertEquals(
            searchFound.first.first()._id,
            lastFaq._id,
            "The first faq in the list should be the last created because of the creation date order"
        )

        assertEquals(
            searchFound.first.first().intentId,
            lastFaq.intentId,
            "intentId is different than expected"
        )

        assertEquals(
            searchFound.first.first().botId,
            lastFaq.botId,
            "The first faq in the list should be the last created because of the creation date order"
        )

        assertEquals(
            searchFound.first.first().namespace,
            lastFaq.namespace,
            "namespace is different than expected"
        )

    }

    private fun `A faqDefinition search with empty utterance`() {
        val createdFaq = createDataForFaqSearch(numberOfUtterances = 0)

        // search the actual faq
        val searchFound = faqDefinitionDao.getFaqDetailsWithCount(
            //no specific filtering
            createFaqQuery(null, null),
            mockedApplicationDefinition,
            null
        )

        assertEquals(searchFound.first.size, searchFound.second.toInt())
        assertEquals(searchFound.first.size, 1)
        assertEquals(searchFound.first.first()._id, createdFaq._id)
        assertEquals(searchFound.first.first().utterances, emptyList())
    }

    @Test
    fun `A faqDefinition search with empty utterance with DocumentDB`() {
        System.setProperty(TOCK_DOCUMENT_DB_ON_PROPERTY, "true")
        `A faqDefinition search with empty utterance`()
    }

    @Test
    fun `A faqDefinition search with deleted utterances with DocumentDB`() {
        System.setProperty(TOCK_DOCUMENT_DB_ON_PROPERTY, "true")
        `A faqDefinition search with deleted utterances`()
    }

    @Test
    fun `A faqDefinition search with name with deleted utterances with DocumentDB`() {
        System.setProperty(TOCK_DOCUMENT_DB_ON_PROPERTY, "true")
        `A faqDefinition search with name with deleted utterances`()
    }

    @Test
    fun `FaqDefinition search must be ordered by creationDate with DocumentDB`() {
        System.setProperty(TOCK_DOCUMENT_DB_ON_PROPERTY, "true")
        `FaqDefinition search must be ordered by creationDate`()
    }

    /**
     * Create data For Faq Search with associated data for collections in FaqDefinition, IntentDefinition, ClassifiedSentences
     * With default data for each parameter
     * @return a FaqDefinition
     */
    private fun createDataForFaqSearch(
        faqId: Id<FaqDefinition> = this.faqId,
        intentId: Id<IntentDefinition> = this.intentId,
        i18nId: Id<I18nLabel> = this.i18nId,
        tagList: List<String> = emptyList(),
        applicationId: Id<ApplicationDefinition> = this.applicationId,
        enabled: Boolean = true,
        faqName: String = "Faq Name",
        numberOfUtterances: Int = 1,
        utteranceText: String = "randomText",
        classifiedSentenceStatus: ClassifiedSentenceStatus = ClassifiedSentenceStatus.validated,
        instant: Instant = now
    ): FaqDefinition {

        val faqDefinition =
            FaqDefinition(faqId,  botId, namespace, intentId, i18nId, tagList, enabled, instant, instant)

        val createdIntent = IntentDefinition(
            faqName,
            namespace,
            setOf(applicationId),
            emptySet(),
            label = StringUtils.lowerCase(faqName),
            category = faqCategory,
            _id = intentId
        )

        intentDefinitionDao.save(createdIntent)

        //create utterance according to the number of utterance
        for (number: Int in 1..numberOfUtterances) {
            val utterance = createUtterance("$utteranceText $number", intentId, classifiedSentenceStatus)
            classifiedSentencesDao.save(utterance)
        }

        faqDefinitionDao.save(faqDefinition)

        return faqDefinition
    }

    private fun createFaqQuery(enabled: Boolean?, search: String?): FaqQuery {
        return FaqQuery(
            0,
            10,
            search,
            null,
            emptyList(),
            enabled,
            userLogin,
            null,
            applicationId.toString(),
            namespace
        )
    }

    private fun createUtterance(text: String, intentId: Id<IntentDefinition>, status: ClassifiedSentenceStatus) =
        ClassifiedSentence(
            text = text,
            language = Locale.FRENCH,
            applicationId = applicationId,
            creationDate = Instant.now(),
            updateDate = Instant.now(),
            status = status,
            classification = Classification(intentId, emptyList()),
            lastIntentProbability = 1.0,
            lastEntityProbability = 1.0,
            qualifier = userLogin
        )
}
