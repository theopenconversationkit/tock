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
import ai.tock.shared.property
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Built-in listener to start [BotDefinition.keywordStory].
 */
object BuiltInKeywordListener : NlpListener {
    val deleteKeyword = property("tock_bot_delete_keyword", "_delete_user_")
    val enableKeyword = property("tock_bot_enable_keyword", "_enable_user_")
    val disableKeyword = property("tock_bot_disable_keyword", "_disable_user_")
    val testContextKeyword = property("tock_bot_test_context_keyword", "_test_")
    val endTestContextKeyword = property("tock_bot_end_test_context_keyword", "_end_test_")

    /**
     * The keyword to listen.
     */
    val keywords: MutableSet<String> = ConcurrentSkipListSet<String>(
        listOf(
            deleteKeyword,
            enableKeyword,
            disableKeyword,
            testContextKeyword,
            endTestContextKeyword
        )
    )

    /**
     * The keyword regexp is applicable.
     */
    @Volatile
    var keywordRegexp: Regex? = null

    override fun handleKeyword(sentence: String): Intent? {
        return if (keywords.contains(sentence) || keywordRegexp?.matches(sentence) == true) {
            Intent.keyword
        } else {
            keywordServices.asSequence().mapNotNull { it.detectKeywordIntent(sentence) }.firstOrNull()
        }
    }
}
