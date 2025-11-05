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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.shared.booleanProperty
import ai.tock.shared.cache.getFromCache
import ai.tock.shared.cache.putInCache
import ai.tock.shared.coroutines.fireAndForgetIO
import ai.tock.shared.longProperty
import ai.tock.shared.security.decrypt
import ai.tock.shared.security.encrypt
import ai.tock.shared.security.encryptionEnabled
import ai.tock.shared.security.sha256Uuid
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.litote.kmongo.toId
import java.util.concurrent.TimeUnit

/**
 * Cache user id for privacy. By default, only memory cache is enabled.
 * The expiration ttl is configurable using tock_whatsapp_memory_timeout_in_minutes property (default 60).
 *
 * If you need to use notification, and if you have more than one bot instance, you can activate the persistent cache
 * using tock_whatsapp_persistent_cache boolean property (default false).
 * In that case, you will also need to enable encryption (using tock_encrypt_pass property) because the WhatsApp id must be encrypted
 * (see https://javadoc.io/doc/com.melloware/jasypt/latest/org/jasypt/util/text/BasicTextEncryptor.html)
 */
object UserHashedIdCache {

    private const val PERSISTENT_CACHE_TYPE = "whatsapp_id"

    private val persistentCacheActivated = booleanProperty("tock_whatsapp_persistent_cache", false)
        .also { activated ->
            if (activated && !encryptionEnabled) {
                error("when tock_whatsapp_persistent_cache is activated, you need also to activate encryption using tock_encrypt_pass property - exiting")
            }
        }

    private val idCache: Cache<String, String> =
        CacheBuilder.newBuilder()
            .expireAfterAccess(longProperty("tock_whatsapp_memory_timeout_in_minutes", 60), TimeUnit.MINUTES)
            .build()

    fun createHashedId(id: String): String = sha256Uuid(id).toString().apply {
        putIdFromPersistentCache(this, id)
        idCache.put(this, id)
    }

    fun getRealId(hashedId: String): String =
        idCache.getIfPresent(hashedId)
            ?: getIdFromPersistentCache(hashedId)
            ?: throw CacheExpiredException("Cache expired or real ID not found for hashedId: $hashedId")

    private fun putIdFromPersistentCache(hashedId: String, id: String) {
        if (persistentCacheActivated) {
            fireAndForgetIO {
                putInCache(hashedId.toId(), PERSISTENT_CACHE_TYPE, encrypt(id))
            }
        }
    }

    private fun getIdFromPersistentCache(hashedId: String): String? =
        if (persistentCacheActivated) {
            getFromCache<String>(hashedId.toId(), PERSISTENT_CACHE_TYPE)
                ?.let { realId -> decrypt(realId) }
        } else {
            null
        }
}

class CacheExpiredException(message: String) : RuntimeException(message)
