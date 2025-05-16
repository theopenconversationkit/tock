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

package ai.tock.bot.connector.messenger

import ai.tock.shared.cache.getFromCache
import ai.tock.shared.cache.putInCache
import mu.KotlinLogging
import org.litote.kmongo.toId

/**
 * Manage Facebook attachment upload cache API.
 * cf (https://developers.facebook.com/docs/messenger-platform/send-api-reference/attachment-upload)
 */
internal object AttachmentCacheService {

    private val logger = KotlinLogging.logger {}
    private val cacheType = "messenger_cache_url"

    fun getAttachmentId(applicationId: String, url: String): String? {
        logger.trace { "get attachment from cache $url" }
        return getFromCache("${applicationId}_$url".toId(), cacheType)
    }

    fun setAttachmentId(applicationId: String, url: String, attachmentId: String) {
        logger.trace { "set attachment in cache $url $attachmentId" }
        putInCache("${applicationId}_$url".toId(), cacheType, attachmentId)
    }
}
