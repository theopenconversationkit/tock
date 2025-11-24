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

package ai.tock.bot.xray

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.nlp.BuiltInKeywordListener
import ai.tock.shared.TOCK_NAMESPACE
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class XrayKeywordServiceTest {
    @Test
    fun `GIVEN a keyword WHEN this keywords do not belong to keywords list THEN keyword is not recognized and return an error`() {
        val keyword = "_toto_"

        val resultingIntent = BuiltInKeywordListener.handleKeyword(keyword)
        assertTrue(resultingIntent == null)
    }

    @ParameterizedTest
    @ValueSource(strings = ["_test_", "_end_test_", "_xray_", "_xray_update"])
    fun `GIVEN a keyword WHEN this keywords is known by Tock THEN keyword is recognized and the Intent -keyword- is returned`(keyword: String) {
        val resultingIntent = BuiltInKeywordListener.handleKeyword(keyword)
        assertTrue {
            resultingIntent is Intent
            resultingIntent?.name == "$TOCK_NAMESPACE:keyword"
        }
    }
}
