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

package fr.vsct.tock.bot.engine.nlp

import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.shared.property
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Built-in listener to start [BotDefinition.keywordStory].
 */
object BuiltInKeywordListener : NlpListener {

    val deleteKeyword = property("tock_bot_delete_keyword", "_delete_user_")
    val testContextKeyword = property("tock_bot_test_context_keyword", "_test_")
    val endTestContextKeyword = property("tock_bot_end_test_context_keyword", "_end_test_")

    /**
     * The keyword to listen.
     */
    val keywords: MutableSet<String> = ConcurrentSkipListSet<String>(
            listOf(
                    deleteKeyword,
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
            null
        }
    }

    override fun error(query: NlpQuery, throwable: Throwable?) {
        //do nothing
    }

    override fun success(query: NlpQuery, result: NlpResult) {
        //do nothing
    }
}