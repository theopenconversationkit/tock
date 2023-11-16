/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin.bot.llm.settings.azureopenai


// https://learn.microsoft.com/en-us/java/api/com.azure.ai.openai.openaiserviceversion?view=azure-java-preview
enum class AzureOpenAIVersion(val version: String) {
    V2022_12_01("2022-12-01"),
    V2023_05_15("2023-05-15"),
    V2023_06_01_PREVIEW("2023-06-01-preview"),
    V2023_07_01_PREVIEW("2023-07-01-preview"),
    V2023_08_01_PREVIEW("2023-08-01-preview"),
    V2023_09_01_PREVIEW("2023-09-01-preview");

    companion object {
        fun findByVersion(version: String): AzureOpenAIVersion? {
            return entries.firstOrNull { it.version == version }
        }
    }
}