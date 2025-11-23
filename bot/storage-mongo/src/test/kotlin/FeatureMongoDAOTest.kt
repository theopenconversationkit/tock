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

package ai.tock.bot.mongo

import ai.tock.bot.engine.feature.FeatureType
import ai.tock.bot.mongo.FeatureMongoDAOTest.Feature.feature
import ai.tock.bot.mongo.Feature_.Companion._id
import ai.tock.shared.internalDefaultZoneId
import com.mongodb.client.result.DeleteResult
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class FeatureMongoDAOTest {
    @Suppress("ktlint:standard:enum-entry-name-case")
    enum class Feature : FeatureType {
        feature,
    }

    private data class FeatureID(val id: String, val key: String)

    private val botId = "id"
    private val namespace = "namespace"
    private val applicationId = "applicationId"
    private val id =
        FeatureID(
            "id,namespace,ai.tock.bot.mongo.FeatureMongoDAOTest\$Feature,feature",
            "ai.tock.bot.mongo.FeatureMongoDAOTest\$Feature,feature",
        )
    private val idWithApplicationId =
        FeatureID(
            "id,namespace,ai.tock.bot.mongo.FeatureMongoDAOTest\$Feature,feature+applicationId",
            "ai.tock.bot.mongo.FeatureMongoDAOTest\$Feature,feature+applicationId",
        )
    private val idWithOtherApplicationId =
        FeatureID(
            "id,namespace,ai.tock.bot.mongo.FeatureMongoDAOTest\$Feature,feature+otherApplicationId",
            "ai.tock.bot.mongo.FeatureMongoDAOTest\$Feature,feature+otherApplicationId",
        )

    private val collection: CoroutineCollection<ai.tock.bot.mongo.Feature> = mockk()
    private val col = mockk<com.mongodb.reactivestreams.client.MongoCollection<ai.tock.bot.mongo.Feature>>()
    private val cache = mockk<MongoFeatureCache>()
    private val featureDAO by lazy { FeatureMongoDAO(cache, col) }
    private var features = mutableListOf<ai.tock.bot.mongo.Feature>()

    @AfterEach
    fun cleanupFeatures() {
        unmockkStatic("org.litote.kmongo.MongoCollectionsKt", "kotlin.collections.KMongoIterableKt")
        unmockkStatic("org.litote.kmongo.coroutine.CoroutineCollectionKt")
        clearMocks(collection, col, cache)
    }

    @BeforeEach
    internal fun setUp() {
        mockkStatic("org.litote.kmongo.MongoCollectionsKt", "kotlin.collections.KMongoIterableKt")
        mockkStatic("org.litote.kmongo.coroutine.CoroutineCollectionKt")
        features.clear()
        every { col.coroutine } returns collection
        coEvery { collection.save(any()) } returns mockk()
        every { cache.setState(any(), any()) } just Runs

        every { col.find(any<Bson>()) } returns mockk(relaxed = true)
        // every { col.find(any<String>()) } returns mockk(relaxed = true)
    }

    @Nested
    @DisplayName("IsEnabled()")
    inner class IsEnabled {
        @Nested
        @DisplayName("Caching")
        inner class Cache {
            @BeforeEach
            internal fun setUp() {
                `given no data in cache for`(id)
                `given no data in cache for`(idWithApplicationId)
            }

            @Test
            fun `cache connector feature`() =
                runBlocking {
                    `given no data persisted for`(id)
                    `given data persisted for`(idWithApplicationId, true)

                    featureDAO.isEnabled(botId, namespace, feature, applicationId)

                    `assert that cache is set to`(idWithApplicationId, true)
                }

            @Test
            fun `cache global feature`() =
                runBlocking {
                    `given data persisted for`(id, true)
                    `given no data persisted for`(idWithApplicationId)

                    featureDAO.isEnabled(botId, namespace, feature, applicationId)

                    `assert that cache is set to`(id, true)
                }

            @Test
            fun `cache global and connector feature`() =
                runBlocking {
                    `given data persisted for`(id, true)
                    `given data persisted for`(idWithApplicationId, true)

                    featureDAO.isEnabled(botId, namespace, feature, applicationId)

                    `assert that cache is set to`(id, true)
                    `assert that cache is set to`(idWithApplicationId, true)
                }

            @Test
            fun `cache global and all connector features`() =
                runBlocking {
                    `given data persisted for`(id, true)
                    `given data persisted for`(idWithApplicationId, true)
                    `given data persisted for`(idWithOtherApplicationId, true)

                    featureDAO.isEnabled(botId, namespace, feature, applicationId)

                    `assert that cache is set to`(id, true)
                    `assert that cache is set to`(idWithApplicationId, true)
                }

            private fun `given no data in cache for`(featureID: FeatureID) {
                every { cache.stateOf(featureID.id) } returns null
            }

            private fun `assert that cache is set to`(
                featureID: FeatureID,
                enabled: Boolean,
            ) {
                verify { cache.setState(featureID.id, Feature(featureID.id, featureID.key, enabled, botId, namespace)) }
            }

            private fun `given no data persisted for`(featureID: FeatureID) {
                coEvery { collection.findOne(_id eq featureID.id) } returns null
            }

            private fun `given data persisted for`(
                featureID: FeatureID,
                value: Boolean,
            ) {
                val feature = Feature(featureID.id, featureID.key, value, botId, namespace)
                coEvery { collection.findOne(_id eq featureID.id) } returns feature
                features.add(feature)
                val publisherList: CoroutineFindPublisher<ai.tock.bot.mongo.Feature> = mockk()
                coEvery { publisherList.toList() } returns features
                coEvery { collection.find(any<Bson>()) } returns publisherList
                coEvery { collection.find(any<String>()) } returns publisherList
            }
        }

        @Nested
        @DisplayName("Requesting for a global feature")
        inner class Global {
            @Test
            fun `existing enabled global feature`() =
                runBlocking {
                    `given data for`(id, true)
                    `given no data for`(idWithApplicationId)

                    assertTrue(featureDAO.isEnabled(botId, namespace, feature, false))
                }

            @Test
            fun `feature between activation period`() =
                runBlocking {
                    `given data for`(
                        id,
                        true,
                        now(internalDefaultZoneId).minusYears(1),
                        now(internalDefaultZoneId).plusYears(1),
                    )

                    assertTrue(featureDAO.isEnabled(botId, namespace, feature, false))
                }

            @Test
            fun `disabled feature between activation period`() =
                runBlocking {
                    `given data for`(
                        id,
                        false,
                        now(internalDefaultZoneId).minusYears(1),
                        now(internalDefaultZoneId).plusYears(1),
                    )

                    assertFalse(featureDAO.isEnabled(botId, namespace, feature, true))
                }

            @Test
            fun `feature in activation period with no end date`() =
                runBlocking {
                    `given data for`(
                        id,
                        true,
                        now(internalDefaultZoneId).minusYears(1),
                    )

                    assertTrue(featureDAO.isEnabled(botId, namespace, feature, false))
                }

            @Test
            fun `feature before activation period`() =
                runBlocking {
                    `given data for`(
                        id,
                        true,
                        now(internalDefaultZoneId).plusYears(1),
                        now(internalDefaultZoneId).plusYears(2),
                    )

                    assertFalse(featureDAO.isEnabled(botId, namespace, feature, true))
                }

            @Test
            fun `feature with graduation for user with hash under 50`() =
                runBlocking {
                    `given data for`(id, true, null, null, 50)

                    assertTrue(featureDAO.isEnabled(botId, namespace, feature, false, "f"))
                }

            @Test
            fun `feature with graduation for user with hash over 50`() =
                runBlocking {
                    `given data for`(id, true, null, null, 50)

                    assertFalse(featureDAO.isEnabled(botId, namespace, feature, false, "a"))
                }

            @Test
            fun `non existing enabled global feature but connector feature exists`() {
                runBlocking {
                    `given no data for`(id)
                    `given data for`(idWithApplicationId, true)

                    assertFalse(featureDAO.isEnabled(botId, namespace, feature, false))

                    `assert that feature is persisted with`(id, false)
                }
            }

            @Test
            fun `non existing feature is disabled by default`() {
                runBlocking {
                    `given no data for`(id)
                    `given no data for`(idWithApplicationId)

                    assertFalse(featureDAO.isEnabled(botId, namespace, feature))

                    `assert that feature is persisted with`(id, false)
                }
            }

            @Test
            fun `non existing feature with enabled state by default`() {
                runBlocking {
                    `given no data for`(id)
                    `given no data for`(idWithApplicationId)

                    assertTrue(featureDAO.isEnabled(botId, namespace, feature, true))

                    `assert that feature is persisted with`(id, true)
                }
            }

            @Test
            fun `non existing feature with disabled state by default`() {
                runBlocking {
                    `given no data for`(id)
                    `given no data for`(idWithApplicationId)

                    assertFalse(featureDAO.isEnabled(botId, namespace, feature, false))

                    `assert that feature is persisted with`(id, false)
                }
            }
        }

        @Test
        fun delete() =
            runBlocking {
                coEvery { collection.deleteOneById(id.id) } returns DeleteResult.acknowledged(1)

                featureDAO.deleteFeature(botId, namespace, feature)

                coVerify(exactly = 1) { collection.deleteOneById(id.id) }
            }
    }

    @Nested
    @DisplayName("Requesting for a connector specific feature")
    inner class Connector {
        @Test
        fun `existing enabled connector feature`() =
            runBlocking {
                `given no data for`(id)
                `given data for`(idWithApplicationId, true)

                assertTrue(featureDAO.isEnabled(botId, namespace, feature, applicationId, false))
            }

        @Test
        fun `non existing connector feature but existing global feature`() =
            runBlocking {
                `given data for`(id, true)
                `given no data for`(idWithApplicationId)

                assertTrue(featureDAO.isEnabled(botId, namespace, feature, applicationId, false))
            }

        @Test
        fun delete() =
            runBlocking {
                coEvery { collection.deleteOneById(idWithApplicationId.id) } returns DeleteResult.acknowledged(1)

                featureDAO.deleteFeature(botId, namespace, feature, applicationId)

                coVerify(exactly = 1) { collection.deleteOneById(idWithApplicationId.id) }
            }
    }

    private fun `assert that feature is persisted with`(
        featureID: FeatureID,
        enabled: Boolean,
    ) {
        coVerify(exactly = 1) { collection.save(Feature(featureID.id, featureID.key, enabled, botId, namespace)) }
    }

    private fun `given no data for`(featureID: FeatureID) {
        mockRetrieveData(featureID, null)
    }

    private fun `given data for`(
        featureID: FeatureID,
        enabled: Boolean,
        start: ZonedDateTime? = null,
        end: ZonedDateTime? = null,
        graduation: Int? = null,
    ) {
        mockRetrieveData(
            featureID,
            Feature(featureID.id, featureID.key, enabled, botId, namespace, start, end, graduation),
        )
    }

    private fun mockRetrieveData(
        featureID: FeatureID,
        feature: ai.tock.bot.mongo.Feature?,
    ) {
        every { cache.stateOf(featureID.id) } returns feature
        coEvery { collection.findOne(_id eq featureID.id) } returns feature
        feature?.also { features.add(it) }
        val publisherList: CoroutineFindPublisher<ai.tock.bot.mongo.Feature> = mockk()
        coEvery { publisherList.toList() } returns features
        coEvery { collection.find(any<Bson>()) } returns publisherList
        coEvery { collection.find(any<String>()) } returns publisherList
    }
}
