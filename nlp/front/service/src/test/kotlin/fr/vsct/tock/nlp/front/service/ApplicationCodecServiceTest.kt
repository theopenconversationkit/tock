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
import fr.vsct.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import fr.vsct.tock.nlp.front.shared.codec.DumpType
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQueryResult
import fr.vsct.tock.shared.defaultLocale
import io.mockk.every
import io.mockk.verify
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.Id
import org.litote.kmongo.newId
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

    private val logger = KotlinLogging.logger {}

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

        val dump = ApplicationCodecService.exportSentences(appId, DumpType.full)

        assertEquals(0, dump.sentences.size)
    }

    @Test
    fun `importing existing app with a new locale adds the locale to the app`() {
        val newLocale = if (Locale.ITALIAN == defaultLocale) Locale.ENGLISH else Locale.ITALIAN
        val app = app.copy(supportedLocales = setOf(newLocale))
        val dump = ApplicationDump(app)

        val report = ApplicationCodecService.import(namespace, dump)
        assertTrue(report.modified)
        verify {
            context.config.save(match<ApplicationDefinition> {
                it.supportedLocales.contains(newLocale)
                        && it.supportedLocales.contains(defaultLocale)
            })
        }
    }

    @Test
    fun `importing existing app with defaultModelMayExist option set to true removes the default locale`() {
        val newLocale = if (Locale.ITALIAN == defaultLocale) Locale.ENGLISH else Locale.ITALIAN
        val app = app.copy(supportedLocales = setOf(newLocale))
        val dump = ApplicationDump(app)

        val report =
            ApplicationCodecService.import(namespace, dump, ApplicationImportConfiguration(defaultModelMayExist = true))
        assertTrue(report.modified)
        verify {
            context.config.save(match<ApplicationDefinition> {
                it.supportedLocales.contains(newLocale)
                        && !it.supportedLocales.contains(defaultLocale)
            })
        }
    }

    @Test
    fun `importing shared intent does not add unknown intent id to the app`() {
        val newLocale = if (Locale.ITALIAN == defaultLocale) Locale.ENGLISH else Locale.ITALIAN
        val otherIntent =
            intent2Definition.copy(_id = newId(), name = "other", sharedIntents = setOf(defaultIntentDefinition._id))
        val andOtherIntent =
            intent2Definition.copy(_id = newId(), name = "andOther", sharedIntents = setOf(otherIntent._id))

        logger.debug { otherIntent }
        logger.debug { andOtherIntent }

        val app = app.copy(
            supportedLocales = setOf(newLocale),
            intents = setOf(defaultIntentDefinition._id, otherIntent._id, andOtherIntent._id)
        )
        val dump = ApplicationDump(
            app,
            intents = listOf(
                defaultIntentDefinition.copy(sharedIntents = setOf(otherIntent._id)),
                otherIntent,
                andOtherIntent
            )
        )

        val report = ApplicationCodecService.import(namespace, dump)
        assertTrue(report.modified)
        assertEquals(2, report.intentsImported.size)
        verify {
            context.config.save(match<IntentDefinition> {
                it.name == defaultIntentDefinition.name
                        && it._id == defaultIntentDefinition._id
                        && it.sharedIntents.isEmpty()
            })
        }
        var newOtherIntentId: Id<IntentDefinition>? = null
        verify {
            context.config.save(match<IntentDefinition> {
                (it.name == otherIntent.name
                        && it._id != otherIntent._id
                        && it.sharedIntents.size == 1
                        && it.sharedIntents.contains(defaultIntentDefinition._id)
                        )
                    .apply { if (this) newOtherIntentId = it._id }

            })
        }
        verify {
            context.config.save(match<IntentDefinition> {
                it.name == andOtherIntent.name
                        && it._id != andOtherIntent._id
                        && it.sharedIntents.size == 1
                        && it.sharedIntents.contains(newOtherIntentId)

            })
        }
    }
}