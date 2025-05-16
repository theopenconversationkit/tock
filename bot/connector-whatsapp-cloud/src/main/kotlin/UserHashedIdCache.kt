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

import ai.tock.shared.security.sha256Uuid
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

object UserHashedIdCache {

    private val idCache: Cache<String, String> =
        CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build()

    fun createHashedId(id: String): String = sha256Uuid(id).toString().apply { idCache.put(this, id) }

    fun getRealId(hashedId: String): String = idCache.getIfPresent(hashedId)
        ?: throw CacheExpiredException("Cache expired or real ID not found for hashedId: $hashedId")
}

class CacheExpiredException(message: String) : RuntimeException(message)
