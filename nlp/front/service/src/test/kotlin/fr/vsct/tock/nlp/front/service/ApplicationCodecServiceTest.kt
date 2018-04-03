/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.front.service

import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.codec.DumpType
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.SentencesQueryResult
import fr.vsct.tock.shared.defaultLocale
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.verify
import org.litote.kmongo.toId
import java.time.Instant.now
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
class ApplicationCodecServiceTest : AbstractTest() {

    @BeforeEach
    fun before() {
        every { context.config.getApplicationByNamespaceAndName(any(), any()) } returns app
    }

    @Test
    fun `import existing app_does not create app`() {
        val dump = ApplicationDump(app)

        val report = ApplicationCodecService.import(namespace, dump)
        assertFalse(report.modified)
    }

    @Test
    fun `export sentence_fails WHEN sentence intent is unknown`() {
        val appId = "id".toId<ApplicationDefinition>()
        val app = ApplicationDefinition("test", "test", _id = appId)
        val sentences = listOf(
            ClassifiedSentence(
                "text",
                defaultLocale,
                appId,
                now(),
                now(),
                ClassifiedSentenceStatus.model,
                Classification("unknwIntentId".toId(), emptyList()),
                null,
                null
            )
        )

        every { context.config.getApplicationById(appId) } returns app
        every { context.config.getIntentsByApplicationId(appId) } returns emptyList()
        every { context.config.search(any()) } returns SentencesQueryResult(1, sentences)

        val dump = ApplicationCodecService.exportSentences(appId, null, null, DumpType.full)

        assertEquals(0, dump.sentences.size)
    }

    @Test
    fun `import existing app_with a new locale add the locale to the app`() {
        val app = app.copy(supportedLocales = setOf(Locale.ITALIAN))
        val dump = ApplicationDump(app)

        val report = ApplicationCodecService.import(namespace, dump)
        assertTrue(report.modified)
        verify { context.config.save(app) }
    }
}