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

package ai.tock.shared

import mu.KotlinLogging
import java.util.Collections
import java.util.Enumeration

/**
 * Return a map with only not null values.
 */
fun <K, V> mapNotNullValues(vararg pairs: Pair<K, V?>): Map<K, V> = mapOf(*pairs).filterValues { it != null }.mapValues { it.value!! }

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
fun <T> Iterator<T>.toList(): List<T> =
    mutableListOf<T>().apply {
        while (hasNext()) {
            add(next())
        }
    }

/**
 * Extract safely a [List] from an [Iterator] - if an [Iterator.next()) call throws an error, ignore this call.
 */
fun <T> Iterator<T>.toSafeList(): List<T> =
    mutableListOf<T>().apply {
        while (hasNext()) {
            try {
                add(next())
            } catch (throwable: Throwable) {
                KotlinLogging.logger {}.error(throwable)
            }
        }
    }

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
