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

package fr.vsct.tock.bot.definition

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.BotDefinitionTest
import fr.vsct.tock.translator.I18nLabelKey
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class BotDefinitionTest {

    private val botDef = BotDefinitionTest()

    @Test
    fun `i18nTranslator() returns an I18nTranslator that use BotDefinition#i18nKeyFromLabel`() {
        val result = botDef.i18nTranslator(Locale.ENGLISH, ConnectorType.none).i18nKeyFromLabel("test")
        assertEquals(
            I18nLabelKey(
                "bottest_test",
                "namespace",
                "bottest",
                "test"
            )
            , result
        )
    }
}