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

package ai.tock.shared.cache

import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.provide
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

private val inMemoryCache: Cache<Any, Any> =
    CacheBuilder
        .newBuilder()
        .maximumSize(longProperty("tock_cache_in_memory_maximum_size", 10000))
        .expireAfterAccess(
            longProperty("tock_cache_in_memory_expiration_in_ms", 1000 * 60 * 60L),
            TimeUnit.MILLISECONDS
        )
        .build()

private val NOT_PRESENT = Any()

private val cache: TockCache get() = injector.provide()

private fun <T> Any?.replaceNotPresent(): T? {
    @Suppress("UNCHECKED_CAST")
    return if (this == NOT_PRESENT) {
        null
    } else {
        this as T
    }
}

private fun <T : Any> inMemoryKey(id: Id<T>, type: String): Any = id to type

/**
 * Returns the value for specified id and type.
 * If no value exists, [valueProvider] provides the value to cache.
 * If [valueProvider] throws exception or returns null, no value is cached and null is returned.
 */
fun <T : Any> getOrCache(id: Id<T>, type: String, valueProvider: () -> T?): T? {
    return inMemoryCache.get(inMemoryKey(id, type)) {
        cache.get(id, type)
            ?: try {
                valueProvider.invoke()?.apply {
                    putInCache(id, type, this)
                }
            } catch (e: Exception) {
                logger.error(e)
                null
            }
            ?: NOT_PRESENT
    }.replaceNotPresent()
}

/**
 * Returns the value for specified id and type.
 * If no value exists, null is returned.
 */
fun <T : Any> getFromCache(id: Id<T>, type: String): T? {
    return try {
        inMemoryCache.get(inMemoryKey(id, type)) {
            cache.get(id, type) ?: NOT_PRESENT
        }.replaceNotPresent()
    } catch (e: Exception) {
        logger.error(e)
        null
    }
}

/**
 * Adds in cache the specified value.
 */
fun <T : Any> putInCache(id: Id<T>, type: String, value: T) {
    try {
        if (value !is ByteArray) {
            inMemoryCache.put(inMemoryKey(id, type), value)
        }
        cache.put(id, type, value)
    } catch (e: Exception) {
        logger.error(e)
    }
}

/**
 * Remove the value for specified id and type from cache.
 */
fun <T : Any> removeFromCache(id: Id<T>, type: String) {
    inMemoryCache.invalidate(inMemoryKey(id, type))
    cache.remove(id, type)
}

/**
 * Returns all cached value for specified type.
 */
fun <T> getCachedValuesForType(type: String): Map<Id<T>, Any> = cache.getAll(type)
