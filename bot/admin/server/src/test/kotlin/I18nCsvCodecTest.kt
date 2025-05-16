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

package ai.tock.bot.admin

import ai.tock.shared.tockInternalInjector
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType.textChat
import ai.tock.translator.UserInterfaceType.voiceAssistant
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.util.Locale.FRENCH
import kotlin.test.assertEquals

/**
 *
 */
class I18nCsvCodecTest {

    companion object {
        val i18nDAO: I18nDAO = mockk(relaxed = true)

        init {
            tockInternalInjector = KodeinInjector()
            val module = Kodein.Module {
                bind<I18nDAO>() with provider { i18nDAO }
            }
            tockInternalInjector.inject(
                Kodein {
                    import(module)
                }
            )
        }

        val namespace = "mynamespace"
        val category = "departuresarrivals"
        val id = namespace + "_" + category + "_départs_suivants"
        val export = """Label;Category;Language;Interface;Id;Validated;Connector;Alternatives
Départs suivants;departuresarrivals;fr;textChat;mynamespace_departuresarrivals_départs_suivants;false;
Départs suivants;departuresarrivals;fr;voiceAssistant;mynamespace_departuresarrivals_départs_suivants;true;"""
        val exportMultiple = """Label;Category;Language;Interface;Id;Validated;Connector;Alternatives
Départs suivants;departuresarrivals;fr;textChat;id1;true;
Départs suivants;departuresarrivals;fr;voiceAssistant;id2;true;"""
        val exportWithNs = """Label;Namespace;Category;Language;Interface;Id;Validated;Connector;Alternatives
Départs suivants;mynamespace;departuresarrivals;fr;textChat;mynamespace_departuresarrivals_départs_suivants;false;
Départs suivants;mynamespace;departuresarrivals;fr;voiceAssistant;mynamespace_departuresarrivals_départs_suivants;true;"""
        val exportMultipleWithNs = """Label;Namespace;Category;Language;Interface;Id;Validated;Connector;Alternatives
Départs suivants;mynamespace;departuresarrivals;fr;textChat;id1;true;
Départs suivants;mynamespace;departuresarrivals;fr;voiceAssistant;id2;true;"""
        val exportValidatedSameId = """Label;Namespace;Category;Language;Interface;Id;Validated;Connector;Alternatives
Départs suivants;mynamespace;departuresarrivals;fr;textChat;id1;true;
Départs suivants;mynamespace;departuresarrivals;fr;voiceAssistant;id1;true;"""
        val exportValidatedDifferentId = """Label;Namespace;Category;Language;Interface;Id;Validated;Connector;Alternatives
Départs suivants;mynamespace;departuresarrivals;fr;textChat;id1;true;
Départs suivants;mynamespace;departuresarrivals;fr;textChat;id2;true;"""
    }

    @AfterEach
    fun resetMockk() {
        clearAllMocks()
    }

    @Test
    fun importCsv_shouldKeepOldI18nLabels_ifNewLabelsAreNotValidated() {
        every { i18nDAO.getLabelById(id.toId()) } answers {
            I18nLabel(
                id.toId(),
                namespace,
                category,
                LinkedHashSet(
                    listOf(
                        I18nLocalizedLabel(
                            FRENCH,
                            textChat,
                            "ok"
                        ),
                        I18nLocalizedLabel(
                            FRENCH,
                            voiceAssistant,
                            "notok"
                        )
                    )
                )
            )
        }

        val result = I18nCsvCodec.importCsv("app", export)

        val slot = slot<I18nLabel>()
        verify(exactly = 1) {
            i18nDAO.save(capture(slot))
        }

        assertEquals(1, result)
        assertEquals(2, slot.captured.i18n.size)
        assertEquals("ok", slot.captured.i18n.first { it.interfaceType == textChat }.label)
        assertEquals("Départs suivants", slot.captured.i18n.first { it.interfaceType == voiceAssistant }.label)
    }

    @Test
    fun `importCsv_should_update_id_when_namespace_in_csv`() {
        val targetNamespace = "other"
        val result = I18nCsvCodec.importCsv(targetNamespace, exportWithNs)

        val slot = slot<I18nLabel>()
        verify(exactly = 1) {
            i18nDAO.save(capture(slot))
        }

        val importedLabel = slot.captured
        assertEquals(1, result)
        assertEquals("Départs suivants", importedLabel.i18n.first { it.interfaceType == voiceAssistant }.label)
        assertEquals(targetNamespace, importedLabel.namespace)
        assertEquals(id.replaceFirst(namespace + "_", targetNamespace + "_"), importedLabel._id.toString())
    }

    @Test
    fun `importCsv_should_not_update_id_when_no_namespace_in_csv`() {
        val targetNamespace = "other"
        val result = I18nCsvCodec.importCsv(targetNamespace, export)

        val slot = slot<I18nLabel>()
        verify(exactly = 1) {
            i18nDAO.save(capture(slot))
        }

        val importedLabel = slot.captured
        assertEquals(1, result)
        assertEquals("Départs suivants", importedLabel.i18n.first { it.interfaceType == voiceAssistant }.label)
        assertEquals(targetNamespace, importedLabel.namespace)
        assertEquals(id, importedLabel._id.toString())
    }

    @Test
    fun `importCsv_should_not_skip_lines_when_ns`() {
        val result = I18nCsvCodec.importCsv(namespace, exportMultipleWithNs)
        verify(exactly = 2) {
            i18nDAO.save(any<I18nLabel>())
        }
        assertEquals(2, result)
    }

    @Test
    fun `importCsv_should_not_skip_lines_when_no_ns`() {
        val result = I18nCsvCodec.importCsv(namespace, exportMultiple)
        verify(exactly = 2) {
            i18nDAO.save(any<I18nLabel>())
        }
        assertEquals(2, result)
    }

    @Test
    fun `importCsv_should_return_number_of_inserts`() {
        assertEquals(1, I18nCsvCodec.importCsv(namespace, export))
        assertEquals(1, I18nCsvCodec.importCsv(namespace, exportWithNs))
        assertEquals(2, I18nCsvCodec.importCsv(namespace, exportMultiple))
        assertEquals(2, I18nCsvCodec.importCsv(namespace, exportMultipleWithNs))
        assertEquals(1, I18nCsvCodec.importCsv(namespace, exportValidatedSameId))
        assertEquals(2, I18nCsvCodec.importCsv(namespace, exportValidatedDifferentId))
    }

    @Test
    fun `importCsv_should_insert_each_id_once`() {
        val result = I18nCsvCodec.importCsv(namespace, exportValidatedSameId)

        val slot = slot<I18nLabel>()
        verify {
            i18nDAO.save(capture(slot))
        }

        assertEquals(1, result)
        assertEquals(2, slot.captured.i18n.size)
    }
}
