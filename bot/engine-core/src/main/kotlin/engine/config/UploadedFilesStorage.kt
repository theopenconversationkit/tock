/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.engine.config

import ai.tock.shared.cache.getFromCache
import ai.tock.shared.cache.putInCache
import org.litote.kmongo.toId

/**
 * The storage of the files uploaded to the bots (the media of the stories...).
 */
interface UploadedFilesStorage {
    fun save(
        fileId: String,
        contentType: String,
        bytes: ByteArray,
    )

    /**
     * Returns the content of the file, null if it does not exist.
     */
    fun load(fileId: String): ByteArray?

    /**
     * The url the connectors use to download the file, null to download it from the bot.
     */
    fun publicUrl(fileId: String): String? = null

    /**
     * The url the bot redirects the downloads of the file to, null to send the file content.
     */
    fun redirectUrl(fileId: String): String? = null
}

/**
 * The default storage: the Tock cache (the `cache` collection with the Mongo storage).
 */
object TockCacheUploadedFilesStorage : UploadedFilesStorage {
    private const val UPLOADED_TYPE = "_uploaded"

    override fun save(
        fileId: String,
        contentType: String,
        bytes: ByteArray,
    ) {
        putInCache(fileId.toId(), UPLOADED_TYPE, bytes)
    }

    override fun load(fileId: String): ByteArray? = getFromCache(fileId.toId(), UPLOADED_TYPE) as? ByteArray
}
