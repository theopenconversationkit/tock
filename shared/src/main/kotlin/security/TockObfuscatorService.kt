/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.shared.security

import ai.tock.shared.Loader
import ai.tock.shared.error
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList

/**
 * To manage obfuscations.
 */
object TockObfuscatorService {

    private val logger = KotlinLogging.logger {}

    private val stringObfuscators: MutableList<StringObfuscator> = CopyOnWriteArrayList()
    private val parameterObfuscators: MutableList<ParameterObfuscator> = CopyOnWriteArrayList()

    internal fun loadObfuscators() {
        try {
            Loader
                .loadServices<ObfuscatorService>()
                .flatMap { it.stringObfuscators() }
                .forEach { registerStringObfuscator(it) }
            Loader
                .loadServices<ObfuscatorService>()
                .flatMap { it.parameterObfuscators() }
                .forEach { registerParameterObfuscator(it) }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    /**
     * Register string stringObfuscators.
     */
    fun registerStringObfuscator(vararg newObfuscators: StringObfuscator) {
        stringObfuscators.addAll(newObfuscators.toList())
    }

    /**
     * Register parameters stringObfuscators.
     */
    fun registerParameterObfuscator(vararg newObfuscators: ParameterObfuscator) {
        parameterObfuscators.addAll(newObfuscators.toList())
    }

    /**
     * Remove all current stringObfuscators.
     */
    fun deregisterObfuscators() {
        stringObfuscators.clear()
        parameterObfuscators.clear()
    }

    fun obfuscate(texts: List<String>): List<String> {
        return texts.map { obfuscate(it)!! }
    }

    fun obfuscate(text: String?, mode: StringObfuscatorMode = StringObfuscatorMode.normal): String? {
        return if (text == null) {
            null
        } else {
            var t: String = text
            stringObfuscators.forEach {
                t = it.obfuscate(t, mode)
            }
            t
        }
    }

    fun obfuscate(parameters: Map<String, String>): Map<String, String> {
        var p: Map<String, String> = parameters
        parameterObfuscators.forEach {
            p = it.obfuscate(p)
        }
        return p
    }

}