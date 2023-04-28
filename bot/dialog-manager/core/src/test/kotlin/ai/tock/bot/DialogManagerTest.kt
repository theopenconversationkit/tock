/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot

import ai.tock.bot.bean.TickStoryQuery
import ai.tock.bot.statemachine.State
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

abstract class DialogManagerTest {

    fun getTickStoryFromFile(group: String, fileName: String): TickStoryQuery {
        return getFileFromJson("src/test/resources/tickstory/$group/$fileName.json")
    }

    fun getStateMachineFromFile(fileName: String): State {
        return getFileFromJson("src/test/resources/statemachine/$fileName.json")
    }

    private inline fun  <reified T> getFileFromJson(filePath: String): T {
        return Json.decodeFromStream(File(filePath).inputStream())
    }
}