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

package fr.vsct.tock.shared.cache

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private val cache: Cache by injector.instance()

/**
 * Returns the value for specified id and type.
 * If no value exists, [valueProvider] provides the value to cache.
 * If [valueProvider] throws exception or returns null, no value is cached and null is returned.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> getOrCache(id: String, type: String, valueProvider: () -> T?): T? {
    return try {
        cache.get(id, type) as T?
    } catch (e: Exception) {
        logger.error(e)
        null
    } ?:
            try {
                valueProvider.invoke()?.apply {
                    try {
                        cache.put(id, type, this)
                    } catch(e: Exception) {
                        logger.error(e)
                    }
                }
            } catch(e: Exception) {
                logger.error(e)
                null
            }
}

/**
 * Remove the value for specified id and type from cache.
 */
fun removeFromCache(id: String, type: String) {
    cache.remove(id, type)
}



