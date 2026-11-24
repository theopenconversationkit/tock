/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.storage.dynamodb

import ai.tock.shared.changes.ChangeChecks
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoChangesTest {
    @BeforeAll
    fun start() {
        DynamoLocal.start()
    }

    @BeforeEach
    fun reset() {
        DynamoChanges.reset()
    }

    @AfterEach
    fun cleanup() {
        DynamoChanges.reset()
    }

    @Test
    fun `GIVEN a listener WHEN its channel is published THEN it is called at the next check only`() {
        val stories = AtomicInteger()
        val labels = AtomicInteger()
        DynamoChanges.start()
        DynamoChanges.listen(DynamoChanges.STORY) { stories.incrementAndGet() }
        DynamoChanges.listen(DynamoChanges.LABEL) { labels.incrementAndGet() }

        DynamoChanges.check(force = true)
        assertEquals(0, stories.get(), "nothing published since the start")

        DynamoChanges.changed(listOf(DynamoChanges.STORY))
        DynamoChanges.check(force = true)
        assertEquals(1, stories.get())
        assertEquals(0, labels.get(), "the other channels are not called")

        DynamoChanges.check(force = true)
        assertEquals(1, stories.get(), "a publication is seen once")
    }

    @Test
    fun `GIVEN a publication before the start WHEN the runtime starts THEN the listeners are not called`() {
        DynamoChanges.changed(listOf(DynamoChanges.STORY))
        val stories = AtomicInteger()
        DynamoChanges.listen(DynamoChanges.STORY) { stories.incrementAndGet() }

        DynamoChanges.check(force = true)

        assertEquals(0, stories.get(), "the data loaded at startup is already up to date")
    }

    @Test
    fun `GIVEN a recent check WHEN a request starts THEN the versions are not read again before the interval`() {
        val stories = AtomicInteger()
        DynamoChanges.listen(DynamoChanges.STORY) { stories.incrementAndGet() }
        ChangeChecks.runAll()

        DynamoChanges.changed(listOf(DynamoChanges.STORY))
        // the interval is 10 s by default: the requests of the next seconds do not read DynamoDB
        ChangeChecks.runAll()
        assertEquals(0, stories.get())

        DynamoChanges.check(force = true)
        assertEquals(1, stories.get())
    }

    @Test
    fun `GIVEN cached labels WHEN the labels are published THEN the listener is called for each label returned by getLabels`() {
        val namespace = "changes"
        val label =
            I18nLabel(
                "changes_label".toId(),
                namespace,
                "test",
                linkedSetOf(I18nLocalizedLabel(Locale.FRENCH, UserInterfaceType.textChat, "bonjour", validated = true)),
                "bonjour",
            )
        I18nDynamoDAO.publish(label)
        val returned = I18nDynamoDAO.getLabels(namespace).map { it._id }
        val removed = CopyOnWriteArrayList<Id<I18nLabel>>()
        I18nDynamoDAO.listenI18n { removed += it }

        DynamoChanges.changed(listOf(DynamoChanges.LABEL))
        DynamoChanges.check(force = true)

        assertTrue(removed.containsAll(returned), "$removed")
        assertTrue(label._id in removed)
    }
}
