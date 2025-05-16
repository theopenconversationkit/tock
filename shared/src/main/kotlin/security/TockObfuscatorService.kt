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
    private val mapObfuscators: MutableList<MapObfuscator> = CopyOnWriteArrayList()

    internal fun loadObfuscators() {
        try {
            Loader
                .loadServices<ObfuscatorService>()
                .flatMap { it.stringObfuscators() }
                .forEach { registerStringObfuscator(it) }
            Loader
                .loadServices<ObfuscatorService>()
                .flatMap { it.parameterObfuscators() }
                .forEach { registerMapObfuscator(it) }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    /**
     * Registers string stringObfuscators.
     */
    fun registerStringObfuscator(vararg newObfuscators: StringObfuscator) {
        stringObfuscators.addAll(newObfuscators.toList())
    }

    /**
     * Registers parameters stringObfuscators.
     */
    fun registerMapObfuscator(vararg newObfuscators: MapObfuscator) {
        mapObfuscators.addAll(newObfuscators.toList())
    }

    /**
     * Removes all current stringObfuscators.
     */
    fun deregisterObfuscators() {
        stringObfuscators.clear()
        mapObfuscators.clear()
    }

    /**
     * Obfuscates list of texts.
     *
     * @param texts the text list to obfuscate
     * @param obfuscatedRanges a map (texts list indexed) of forced obfuscated ranges
     */
    fun obfuscate(texts: List<String>, obfuscatedRanges: Map<Int, List<IntRange>> = emptyMap()): List<String> {
        return texts.mapIndexed { index, t -> obfuscate(t, obfuscatedRanges[index] ?: emptyList()) ?: "" }
    }

    /**
     * Obfuscates text.
     *
     * @param text the text to obfuscate
     * @param obfuscatedRanges the forced obfuscation ranges
     */
    fun obfuscate(text: String?, obfuscatedRanges: List<IntRange> = emptyList()): String? {
        return try {
            if (text == null) {
                null
            } else {
                var t: String = text
                stringObfuscators.forEach {
                    t = it.obfuscate(t)
                }
                obfuscatedRanges.asSequence().filterNot { it.isEmpty() }.forEach {
                    t = t.replaceRange(it, "*".repeat(1 + it.last - it.first))
                }
                t
            }
        } catch (e: Exception) {
            logger.error(e)
            text
        }
    }

    /**
     * Obfuscates a map - usually key-based.
     *
     * @map the map to be obfuscated
     */
    fun obfuscate(map: Map<String, String>): Map<String, String> {
        return try {
            var p: Map<String, String> = map
            mapObfuscators.forEach {
                p = it.obfuscate(p)
            }
            p
        } catch (e: Exception) {
            logger.error(e)
            map
        }
    }
}
