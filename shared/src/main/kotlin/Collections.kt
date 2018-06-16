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

package fr.vsct.tock.shared

import java.util.Collections
import java.util.Enumeration

/**
 * Return a map with only not null values.
 */
fun <K, V> mapNotNullValues(vararg pairs: Pair<K, V?>): Map<K, V> =
    mapOf(*pairs).filterValues { it != null }.mapValues { it.value!! }

/**
 * Map not null values of the [Pair] results of the specified transformation.
 */
fun <T, K, V> Iterable<T>.mapNotNullValues(transform: (T) -> Pair<K, V?>): List<Pair<K, V>> =
    map { transform.invoke(it) }
        .filter { it.second != null }
        .map {
            @Suppress("UNCHECKED_CAST")
            it as Pair<K, V>
        }

/**
 * Extract a [Set] from an [Enumeration].
 */
fun <T> Enumeration<T>.toSet(): Set<T> = Collections.list(this).toSet()

/**
 * Extract a [List] from an [Iterator].
 */
fun <T> Iterator<T>.toList(): List<T> = mutableListOf<T>().apply {
    while (hasNext()) {
        add(next())
    }
}
