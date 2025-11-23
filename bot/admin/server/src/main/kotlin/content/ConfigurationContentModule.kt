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

package ai.tock.bot.admin.content

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.model.CreateStoryRequest
import ai.tock.nlp.front.client.FrontClient
import java.util.Locale

class ConfigurationContentModule(val id: String, val stories: List<StoryDefinitionConfigurationContent>) {
    fun setupContent(
        conf: BotApplicationConfiguration,
        locale: Locale,
        userLogin: String,
    ) {
        val namespace = conf.namespace
        val nlpApplication = FrontClient.getApplicationByNamespaceAndName(namespace, conf.nlpModel) ?: error("nlp app not found for $namespace & $conf")

        stories.forEach {
            BotAdminService.createStory(
                namespace,
                CreateStoryRequest(
                    story = it.toBotStoryDefinitionConfiguration(namespace, nlpApplication._id, conf.botId, locale),
                    language = locale,
                    firstSentences = emptyList(),
                ),
                userLogin,
            )
        }
    }
}
