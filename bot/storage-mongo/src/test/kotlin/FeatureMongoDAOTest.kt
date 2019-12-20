/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
import ai.tock.bot.mongo.FeatureMongoDAO.deleteFeature
import ai.tock.bot.mongo.FeatureMongoDAO.disable
import ai.tock.bot.mongo.FeatureMongoDAO.enable
import ai.tock.bot.mongo.FeatureMongoDAO.isEnabled
import ai.tock.bot.mongo.FeatureMongoDAOTest.Feature.a
import ai.tock.bot.mongo.FeatureMongoDAOTest.Feature.b
import ai.tock.bot.mongo.FeatureMongoDAOTest.Feature.c
import ai.tock.shared.internalDefaultZoneId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
internal class FeatureMongoDAOTest : AbstractTest() {

    enum class Feature : FeatureType {
        a, b, c
    }

    val botId = "id"
    val namespace = "namespace"

    @BeforeEach
    fun cleanupFeatures() {
        deleteFeature(botId, namespace, a)
        deleteFeature(botId, namespace, b)
        deleteFeature(botId, namespace, c)
    }

    @Test
    fun `isEnabled returns false WHEN the feature is not present`() {
        assertFalse(isEnabled(botId, namespace, a))
    }

    @Test
    fun `isEnabled persists default feature state WHEN the feature is not present and feature is enabled`() {
        assertFalse(isEnabled(botId, namespace, a, false))

        //default state is not used anymore
        assertFalse(isEnabled(botId, namespace, a, true))
    }

    @Test
    fun `isEnabled persists default feature state WHEN the feature is not present and feature id disabled`() {
        assertTrue(isEnabled(botId, namespace, a, true))

        //default state is not used anymore
        assertTrue(isEnabled(botId, namespace, a, false))
    }

    @Test
    fun `isEnabled returns true WHEN the feature is not present AND default is true`() {
        assertTrue(isEnabled(botId, namespace, a, true))
    }

    @Test
    fun `GIVEN A feature enabled in db THEN isEnabled returns true`() {
        enable(botId, namespace, a)

        assertTrue(isEnabled(botId, namespace, a))
    }

    @Test
    fun `GIVEN A feature enabled in db WHEN the feature is disabled THEN isEnabled returns false`() {
        enable(botId, namespace, a)

        assertTrue(isEnabled(botId, namespace, a))

        disable(botId, namespace, a)

        assertFalse(isEnabled(botId, namespace, a))
    }

    @Test
    fun `GIVEN A feature enabled in db WHEN the feature is deleted THEN isEnabled returns false`() {
        enable(botId, namespace, a)

        assertTrue(isEnabled(botId, namespace, a))

        deleteFeature(botId, namespace, a)

        assertFalse(isEnabled(botId, namespace, a))
    }

    @Test
    fun `isEnabled returns true WHEN the today date is between startDate and endDate and activated`() {
        enable(
            botId,
            namespace,
            a,
            ZonedDateTime.now(internalDefaultZoneId).minusYears(1),
            ZonedDateTime.now(internalDefaultZoneId).plusYears(1)
        )

        assertTrue(isEnabled(botId, namespace, a))
    }

    @Test
    fun `isEnabled returns false WHEN the today date is between startDate and endDate and not activated`() {
        enable(
            botId,
            namespace,
            a,
            ZonedDateTime.now(internalDefaultZoneId).minusYears(1),
            ZonedDateTime.now(internalDefaultZoneId).plusYears(2)
        )
        disable(botId, namespace, a)

        assertFalse(isEnabled(botId, namespace, a))
    }

    @Test
    fun `isEnabled returns true WHEN the today date is after the startDate and there is not dateEnd`() {
        enable(
            botId,
            namespace,
            a,
            ZonedDateTime.now(internalDefaultZoneId).minusYears(1)
        )
        assertTrue(isEnabled(botId, namespace, a))
    }

    @Test
    fun `isEnabled returns false WHEN the today date is before startDate and endDate`() {
        enable(
            botId,
            namespace,
            a,
            ZonedDateTime.now(internalDefaultZoneId).plusYears(1), ZonedDateTime.now(internalDefaultZoneId).plusYears(2)
        )

        assertFalse(isEnabled(botId, namespace, a))
    }
}