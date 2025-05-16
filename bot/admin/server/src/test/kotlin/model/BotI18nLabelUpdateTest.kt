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

package ai.tock.bot.admin.model

import ai.tock.shared.Dice
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class BotI18nLabelUpdateTest {

    private val json =
        """{"_id":"${Dice.newId()}","namespace":"sncf","category":"build","i18n":[{"locale":"de","interfaceType":0,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"de","interfaceType":1,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"en","interfaceType":0,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"en","interfaceType":1,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"es","interfaceType":0,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"es","interfaceType":1,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"fr","interfaceType":0,"label":"Worlds","validated":true,"alternatives":[],"stats":[]},{"locale":"fr","interfaceType":1,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"it","interfaceType":0,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"it","interfaceType":1,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"ja","interfaceType":0,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"ja","interfaceType":1,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"nl","interfaceType":0,"label":"","validated":false,"alternatives":[],"stats":[]},{"locale":"nl","interfaceType":1,"label":"","validated":false,"alternatives":[],"stats":[]}],"defaultLabel":"Worlds","defaultLocale":"fr","defaultI18n":[],"version":10,"unhandledLocaleStats":[],"lastUpdate":null}"""

    @Test
    fun `GIVEN valid json THEN BotI18nLabelUpdate is dezerialized well`() {
        val i18n: BotI18nLabelUpdate = mapper.readValue(json)
        assertNotNull(i18n)
    }
}
