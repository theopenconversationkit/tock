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

package ai.tock.nlp.front.service

import ai.tock.nlp.front.shared.codec.ApplicationDump
import ai.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import ai.tock.nlp.front.shared.codec.DumpType
import ai.tock.nlp.front.shared.codec.SentenceDump
import ai.tock.nlp.front.shared.codec.SentenceEntityDump
import ai.tock.nlp.front.shared.codec.SentencesDump
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.shared.defaultLocale
import ai.tock.translator.I18nLabel
import io.mockk.every
import io.mockk.verify
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
class ApplicationCodecServiceTest : AbstractTest() {

    private val logger = KotlinLogging.logger {}
    val now = now().truncatedTo(ChronoUnit.MILLIS)

    @BeforeEach
    fun before() {
        every { context.config.getApplicationByNamespaceAndName(any(), any()) } returns app
    }

    @Test
    fun `import existing app does not create app`() {
        val dump = ApplicationDump(app)

        val report = ApplicationCodecService.import(namespace, dump)
        assertFalse(report.modified)
    }

    @Test
    fun `export sentence fails WHEN sentence intent is unknown`() {
        val appId = "id".toId<ApplicationDefinition>()
        val app = ApplicationDefinition("test", namespace = "test", _id = appId)
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
            context.config.save(
                match<ApplicationDefinition> {
                    it.supportedLocales.contains(newLocale) &&
                        it.supportedLocales.contains(defaultLocale)
                }
            )
        }
    }

    @Test
    fun `GIVEN existing app WHEN importing with a new faq SHOULD import the new faq`() {

        val faq = FaqDefinition(
            botId = app.name,
            namespace = namespace,
            intentId= defaultIntentDefinition._id,
            i18nId= newId<I18nLabel>(),
            tags = listOf("TAG1", "TAG2"),
            enabled = false,
            creationDate= now,
            updateDate = now,
        )
        val dump = ApplicationDump(
            application = app,
            intents = listOf(defaultIntentDefinition),
            faqs = listOf(faq)
        )

        every { context.config.getFaqDefinitionByIntentId(any())} returns null
        val report = ApplicationCodecService.import(namespace, dump)
        assertTrue(report.modified)
        verify {
            context.config.save(
                match<FaqDefinition> {
                    it.tags.containsAll(listOf("TAG1", "TAG2"))
                    it.namespace === "namespace"
                    it.i18nId.toString().startsWith(namespace)
                }
            )
        }
    }

    @Test
    fun `GIVEN existing app WHEN importing with a new faq with other namespace for i18nId SHOULD update the i18n according to the current application`(){

        val faq = FaqDefinition(
            botId = app.name,
            namespace = namespace,
            intentId= defaultIntentDefinition._id,
            i18nId= "otherNamespace_faq_theI18nd".toId(),
            tags = listOf("TAG1", "TAG2"),
            enabled = false,
            creationDate= now,
            updateDate = now,
        )
        val dump = ApplicationDump(
            application = app,
            intents = listOf(defaultIntentDefinition),
            faqs = listOf(faq)
        )

        every { context.config.getFaqDefinitionByIntentId(any())} returns null
        val report = ApplicationCodecService.import(namespace, dump)
        assertTrue(report.modified)
        verify {
            context.config.save(
                match<FaqDefinition> {
                    it.tags.containsAll(listOf("TAG1", "TAG2"))
                    it.namespace === namespace
                    it.i18nId.toString().startsWith(namespace)
                }
            )
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
            context.config.save(
                match<ApplicationDefinition> {
                    it.supportedLocales.contains(newLocale) &&
                        !it.supportedLocales.contains(defaultLocale)
                }
            )
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
            context.config.save(
                match<IntentDefinition> {
                    it.name == defaultIntentDefinition.name &&
                        it._id == defaultIntentDefinition._id &&
                        it.sharedIntents.isEmpty()
                }
            )
        }
        var newOtherIntentId: Id<IntentDefinition>? = null
        verify {
            context.config.save(
                match<IntentDefinition> {
                    (
                        it.name == otherIntent.name &&
                            it._id != otherIntent._id &&
                            it.sharedIntents.size == 1 &&
                            it.sharedIntents.contains(defaultIntentDefinition._id)
                        )
                        .apply { if (this) newOtherIntentId = it._id }
                }
            )
        }
        verify {
            context.config.save(
                match<IntentDefinition> {
                    it.name == andOtherIntent.name &&
                        it._id != andOtherIntent._id &&
                        it.sharedIntents.size == 1 &&
                        it.sharedIntents.contains(newOtherIntentId)
                }
            )
        }
    }

    @Test
    fun `import sentence flagged of a Locale with country use the Locale language only`() {
        val dump = SentencesDump(
            app.qualifiedName,
            sentences = listOf(SentenceDump("a", "a", now, emptyList(), Locale.FRANCE))
        )
        ApplicationCodecService.importSentences(app.namespace, dump)
        verify {
            context.config.save(match<ClassifiedSentence> { it.language == Locale.FRENCH })
        }
    }

    @Test
    fun `import sentence with sub entities create the sub entities`() {
        val dump = SentencesDump(
            app.qualifiedName,
            sentences = listOf(
                SentenceDump(
                    "a",
                    "a",
                    now,
                    listOf(
                        SentenceEntityDump(
                            "test:e1",
                            "r1",
                            listOf(
                                SentenceEntityDump(
                                    "test:e2",
                                    "r2",
                                    listOf(
                                        SentenceEntityDump(
                                            "test:e3",
                                            "r3",
                                            emptyList(),
                                            0,
                                            1
                                        )
                                    ),
                                    0,
                                    1
                                )
                            ),
                            0,
                            1
                        )
                    ),
                    Locale.FRANCE
                )
            )
        )
        val report = ApplicationCodecService.importSentences(app.namespace, dump)
        // note that the entity namespace has changed
        assertEquals(setOf("namespace:e1", "namespace:e2", "namespace:e3"), report.entitiesImported)
    }
}
