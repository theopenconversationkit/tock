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

package fr.vsct.tock.shared.security

import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.listProperty
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList

/**
 * To manage obfuscations.
 */
object StringObfuscatorService {

    private val logger = KotlinLogging.logger {}

    private val obfuscators: MutableList<StringObfuscator> = CopyOnWriteArrayList()

    internal fun loadObfuscators() {
        try {
            val obf = listProperty("tock_obfuscators", emptyList(), "$$")
            obf.forEach {
                val s = it.split("==")
                registerObfuscator(SimpleObfuscator(s[0].toRegex(), s[1]))
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    /**
     * Register obfuscators.
     */
    fun registerObfuscator(vararg newObfuscators: StringObfuscator) {
        obfuscators.addAll(newObfuscators.toList())
    }

    /**
     * Remove all current obfuscators.
     */
    fun deregisterObfuscators() {
        obfuscators.clear()
    }

    fun obfuscate(texts: List<String>): List<String> {
        return texts.map { obfuscate(it)!! }
    }

    fun obfuscate(text: String?, mode: StringObfuscatorMode = StringObfuscatorMode.normal): String? {
        return if (text == null) {
            null
        } else {
            var t = text
            obfuscators.forEach {
                t = it.obfuscate(t!!, mode)
            }
            t
        }
    }

}