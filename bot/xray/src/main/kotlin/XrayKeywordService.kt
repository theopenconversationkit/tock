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
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.nlp.KeywordService
import ai.tock.bot.xray.XrayKeywords.XRAY_KEYWORD
import ai.tock.bot.xray.XrayKeywords.XRAY_UPDATE_KEYWORD
import ai.tock.shared.property

object XrayKeywords {
    val XRAY_KEYWORD = property("tock_bot_xray_creation_keyword", "_xray_")
    val XRAY_UPDATE_KEYWORD = property("tock_bot_xray_update_keyword", "_xray_update_")
}

class XrayKeywordService : KeywordService {
    override fun detectKeywordIntent(sentence: String): Intent? {
        return if (sentence.startsWith(XRAY_KEYWORD) || sentence.startsWith(XRAY_UPDATE_KEYWORD)) {
            Intent.keyword
        } else {
            null
        }
    }

    override fun keywordHandler(keyword: String): ((bus: BotBus) -> Unit)? {
        when {
            keyword.contains(XRAY_UPDATE_KEYWORD) -> {
                return {
                    XrayKeywordHandler().updateXray(keyword, it)
                }
            }
            keyword.contains(XRAY_KEYWORD) -> {
                return {
                    XrayKeywordHandler().createXray(keyword, it)
                }
            }
            else -> {
                return null
            }
        }
    }
}
