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

package fr.vsct.tock.nlp.front.storage.mongo

import com.mongodb.client.FindIterable
import fr.vsct.tock.nlp.front.shared.config.SearchMark

//wrapper to workaround the 1024 chars limit for String indexes
internal fun textKey(text: String): String = if (text.length > 512) text.substring(0, Math.min(512, text.length)) else text

internal fun <T> FindIterable<T>.filterFromMark(
        start: Long,
        size: Int,
        mark: SearchMark?,
        markExtractor: (T) -> SearchMark): List<T> {

    return if (mark == null) {
        skip(start.toInt()).limit(size).toList()
    } else {
        val cursor = batchSize(size + 1).iterator()
        val result = mutableListOf<T>()
        var markSeen = false
        while (result.size < size && cursor.hasNext()) {
            val next = cursor.next()
            if (markSeen) {
                result.add(next)
            } else {
                markSeen = markExtractor.invoke(next).run {
                    text == mark.text || date < mark.date
                }
            }
        }
        return result
    }
}

internal fun List<String?>.toBsonFilter(): String = filterNotNull().joinToString(",", "{", "}")