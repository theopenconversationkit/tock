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

package ai.tock.bot.engine.nlp

import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.BotBus
import ai.tock.shared.Loader

@Volatile
private var internalKeywordServices: List<KeywordService>? = null

internal val keywordServices: List<KeywordService>
    get() =
        if (internalKeywordServices == null) {
            internalKeywordServices = Loader.loadServices()
            internalKeywordServices!!
        } else {
            internalKeywordServices!!
        }

interface KeywordService {
    /**
     * If returns not null, a keyword is detected, and the returned intent is used to find the story.
     * Use [Intent.keyword] to redirect to the default keyword handler.
     */
    fun detectKeywordIntent(sentence: String): Intent? = null

    /**
     * If this method returns not null for the specified [keyword], then the returned handler is called.
     */
    fun keywordHandler(keyword: String): ((bus: BotBus) -> Unit)? = null
}
