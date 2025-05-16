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

package ai.tock.bot.mongo

import ai.tock.bot.mongo.StoryDefinitionConfigurationMongoDAO.customRegexToFindWord
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test


class StoryDefinitionConfigurationMongoDAOTest: AbstractTest() {

    @Test
    fun `TEST custom Text Search`() {
        assertEquals(customRegexToFindWord("Action"),"^(.*?([aàáâãä][cç]t[iìíîï][oòóôõöø][nñ])[^\$]*)\$")
        assertEquals(customRegexToFindWord("story action "),"^(.*?(st[oòóôõöø]ry)[^\$]*)(.*?([aàáâãä][cç]t[iìíîï][oòóôõöø][nñ])[^\$]*)\$")
        assertEquals(customRegexToFindWord(""),"")
    }
}
