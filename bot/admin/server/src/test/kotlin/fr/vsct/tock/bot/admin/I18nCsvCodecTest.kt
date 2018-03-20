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

package fr.vsct.tock.bot.admin

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import fr.vsct.tock.shared.defaultNamespace
import fr.vsct.tock.shared.tockInternalInjector
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.I18nLocalizedLabel
import fr.vsct.tock.translator.UserInterfaceType.textChat
import fr.vsct.tock.translator.UserInterfaceType.voiceAssistant
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
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
            tockInternalInjector.inject(Kodein {
                import(module)
            })
        }

        val id = "departuresarrivals_départs_suivants"
        val export = """Label;Category;Language;Interface;Id;Validated;Connector;Alternatives
Départs suivants;departuresarrivals;fr;textChat;departuresarrivals_départs_suivants;false;
Départs suivants;departuresarrivals;fr;voiceAssistant;departuresarrivals_départs_suivants;true;"""
    }


    @Test
    fun importCsv_shouldKeepOldI18nLabels_ifNewLabelsAreNotValidated() {
        every { i18nDAO.getLabelById(id.toId()) } answers {
            I18nLabel(
                id.toId(),
                defaultNamespace,
                "departuresarrivals",
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

        I18nCsvCodec.importCsv("app", export)

        val slot = slot<I18nLabel>()
        verify {
            i18nDAO.save(capture(slot))
        }

        assertEquals(2, slot.captured.i18n.size)
        assertEquals("ok", slot.captured.i18n.first { it.interfaceType == textChat }.label)
        assertEquals("Départs suivants", slot.captured.i18n.first { it.interfaceType == voiceAssistant }.label)

    }


}