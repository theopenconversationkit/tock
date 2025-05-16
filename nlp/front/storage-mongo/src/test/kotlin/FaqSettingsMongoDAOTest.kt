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
import ai.tock.nlp.front.service.storage.FaqSettingsDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.FaqSettings
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mongodb.client.MongoCollection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class FaqSettingsMongoDAOTest : AbstractTest() {

    private val faqSettingsDao: FaqSettingsDAO get() = injector.provide()

    private val faqSettingsId = "faqSettingsId".toId<FaqSettings>()
    private val applicationId = "applicationId".toId<ApplicationDefinition>()
    private val storyId = "storyId"
    private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)

    private val faqSettings = FaqSettings(faqSettingsId, applicationId, false, null, now, now)

    private val col: MongoCollection<FaqSettings> by lazy { FaqSettingsMongoDAO.col }

    override fun moreBindingModules() = Kodein.Module {
        bind<FaqSettingsDAO>() with provider { FaqSettingsMongoDAO }
    }

    @AfterEach
    fun cleanup() {
        faqSettingsDao.deleteFaqSettingsById(faqSettingsId)
    }

    @Test
    fun `Save a FaqSettings`() {
        faqSettingsDao.save(faqSettings)
        val settings = faqSettingsDao.getFaqSettingsByApplicationId(applicationId)

        assertEquals(
            expected = faqSettings.copy(_id = settings!!._id),
            actual = settings,
            message = "There should be something returned with an applicationId"
        )
        assertEquals(
            expected = faqSettings.copy(_id = settings._id),
            actual = faqSettingsDao.getFaqSettingsById(settings._id),
            message = "There should be something returned with an faqSettingsId"
        )
        assertEquals(1, col.countDocuments())
    }

    @Test
    fun `Update a FaqSettings`() {
        faqSettingsDao.save(faqSettings)
        val faqSettingsSaved = faqSettingsDao.getFaqSettingsByApplicationId(applicationId)
        val faqSettingsId = faqSettingsSaved!!._id

        assertEquals(expected = faqSettings.satisfactionEnabled, actual = faqSettingsSaved.satisfactionEnabled)
        assertEquals(expected = faqSettings.satisfactionStoryId, actual = faqSettingsSaved.satisfactionStoryId)

        val faqSettings2 = faqSettings.copy(satisfactionEnabled = true, satisfactionStoryId = storyId)
        faqSettingsDao.save(faqSettings2)
        val faqSettingsUpdated = faqSettingsDao.getFaqSettingsById(faqSettingsId)

        assertEquals(expected = faqSettings2.satisfactionEnabled, actual = faqSettingsUpdated?.satisfactionEnabled)
        assertEquals(expected = faqSettings2.satisfactionStoryId, actual = faqSettingsUpdated?.satisfactionStoryId)

        assertEquals(1, col.countDocuments())
    }

    @Test
    fun `Remove a FaqSettings`() {
        faqSettingsDao.save(faqSettings)
        val faqSettingsSaved = faqSettingsDao.getFaqSettingsByApplicationId(applicationId)
        val faqSettingsId = faqSettingsSaved!!._id
        faqSettingsDao.deleteFaqSettingsById(faqSettingsId)

        assertEquals(
            expected = null,
            actual = faqSettingsDao.getFaqSettingsById(faqSettingsId),
            message = "There should be nothing returned with faqSettingsId"
        )
        assertEquals(
            expected = null,
            actual = faqSettingsDao.getFaqSettingsByApplicationId(applicationId),
            message = "There should be nothing returned with applicationId"
        )
        assertEquals(0, col.countDocuments())
    }
}
