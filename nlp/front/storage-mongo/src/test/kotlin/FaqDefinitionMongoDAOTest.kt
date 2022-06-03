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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.translator.I18nLabel
import com.mongodb.client.MongoCollection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class FaqDefinitionMongoDAOTest : AbstractTest() {

    private val faqDefinitionDao: FaqDefinitionDAO get() = injector.provide()

    private val intentId = "idIntent".toId<IntentDefinition>()
    private val faqId = "faqDefId".toId<FaqDefinition>()
    private val faqId2 = "faqDefId2".toId<FaqDefinition>()
    private val faqId3 = "faqDefId3".toId<FaqDefinition>()
    private val i18nId = "idI18n".toId<I18nLabel>()
    private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    private val tagList = listOf("TAG1", "TAG2")

    private val faqDefinition = FaqDefinition(faqId, intentId, i18nId, tagList, true, now, now)

    private val col: MongoCollection<FaqDefinition> by lazy { FaqDefinitionMongoDAO.col }

    @AfterEach
    fun cleanup() {
        faqDefinitionDao.deleteFaqDefinitionById(faqId)
        faqDefinitionDao.deleteFaqDefinitionById(faqId2)
        faqDefinitionDao.deleteFaqDefinitionById(faqId3)
    }

    @Test
    fun `Get a FaqDefinition just saved`() {
        faqDefinitionDao.save(faqDefinition)
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
        val intentId2 = "idIntent2".toId<IntentDefinition>()
        val i18nId2 = "idI18n2".toId<I18nLabel>()
        val tagList2 = listOf("TAG1")

        val otherFaqDefinition =
            FaqDefinition(faqId2, intentId2, i18nId2, tagList2, true, now.plusSeconds(1), now.plusSeconds(1))
        faqDefinitionDao.save(otherFaqDefinition)

        //some another faq
        val intentId3 = "idIntent3".toId<IntentDefinition>()
        val i18nId3 = "idI18n3".toId<I18nLabel>()
        val tagList3 = listOf("TAG2")

        val someOtherFaqDefinition =
            FaqDefinition(faqId3, intentId3, i18nId3, tagList3, true, now.plusSeconds(2), now.plusSeconds(2))
        faqDefinitionDao.save(someOtherFaqDefinition)

        assertEquals(
            expected = 2,
            actual = faqDefinitionDao.getFaqDefinitionByTags(setOf("TAG1")).size,
            message = "There should be something returned with tags"
        )
    }
}