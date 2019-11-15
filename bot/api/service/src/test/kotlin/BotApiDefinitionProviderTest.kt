import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.api.service.BotApiDefinitionProvider
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

/*
 * Copyright (C) 2017/2019 VSCT
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

class BotApiDefinitionProviderTest {

    @Test
    fun `BotApiDefinitionProvider with same botId and configuration name but different namespaces are not equals`() {
        val p1 = BotApiDefinitionProvider(BotConfiguration("name", "id", "namespace1", "name"))
        val p2 = BotApiDefinitionProvider(BotConfiguration("name", "id", "namespace2", "name"))
        assertNotEquals(p1, p2)
    }
}