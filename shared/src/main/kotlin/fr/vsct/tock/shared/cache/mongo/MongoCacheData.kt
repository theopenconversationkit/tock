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

package fr.vsct.tock.shared.cache.mongo

import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant

/**
 *
 */
internal class MongoCacheData(
        val id: String,
        val type: String,
        val s: String? = null,
        val b: ByteArray? = null,
        val a: AnyValueWrapper? = null,
        val date: Instant = Instant.now()) {

    companion object {
        fun fromValue(id: String, type: String, v: Any): MongoCacheData {
            return when (v) {
                is String -> MongoCacheData(id, type, s = v)
                is ByteArray -> MongoCacheData(id, type, b = v)
                else -> MongoCacheData(id, type, a = AnyValueWrapper(v))
            }
        }
    }

    fun toValue(): Any {
        return s ?: b ?: a!!.value!!
    }

}