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

package ai.tock.bot.storage.s3

import ai.tock.aws.s3.S3Bucket
import ai.tock.bot.engine.config.TockCacheUploadedFilesStorage
import ai.tock.bot.engine.config.UploadedFilesStorage
import ai.tock.shared.booleanProperty
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.service.BotAdditionalModulesService
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import mu.KotlinLogging
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Set to `s3` to store the uploaded files in S3 instead of the Tock cache.
 */
private const val FILES_STORAGE_PROPERTY = "tock_bot_files_storage"

/**
 * Binds an [UploadedFilesStorage] that stores the files in S3, configured with:
 * - `tock_bot_files_s3_bucket`: the bucket (required)
 * - `tock_bot_files_s3_prefix`: the prefix of the keys, `tock/files/` by default
 * - `tock_bot_files_s3_public_base_url`: the public url of the prefix (a CloudFront distribution...), not set by default.
 *   When set, the connectors download the files from it instead of from the bot.
 * - `tock_bot_files_s3_presigned_url_minutes`: the bot redirects the downloads to presigned urls valid for this number of minutes,
 *   60 by default; 0 to send the files from the bot
 * - `tock_bot_files_s3_copy_from_cache`: copies to S3 the files uploaded before in the Tock cache when they are read, true by default
 *
 * The S3 client is configured by the `tock_s3_*` properties, see [ai.tock.aws.s3.S3Clients].
 */
val filesS3Module =
    Kodein.Module(allowSilentOverride = true) {
        bind<UploadedFilesStorage>() with
            singleton {
                val bucket =
                    S3Bucket(
                        checkNotNull(propertyOrNull("tock_bot_files_s3_bucket")) { "tock_bot_files_s3_bucket is required to store the files in S3" },
                        property("tock_bot_files_s3_prefix", "tock/files/"),
                    )
                logger.info { "uploaded files stored in s3://${bucket.bucket}/${bucket.prefix}" }
                S3UploadedFilesStorage(
                    bucket,
                    propertyOrNull("tock_bot_files_s3_public_base_url"),
                    longProperty("tock_bot_files_s3_presigned_url_minutes", 60).takeIf { it > 0 }?.let { Duration.ofMinutes(it) },
                    TockCacheUploadedFilesStorage.takeIf { booleanProperty("tock_bot_files_s3_copy_from_cache", true) },
                )
            }
    }

/**
 * Overrides the storage of the uploaded files when `tock_bot_files_storage` is `s3`.
 * Declared in META-INF/services/ai.tock.shared.service.BotAdditionalModulesService.
 */
class S3FilesModulesService : BotAdditionalModulesService {
    override fun customModules(): Set<Kodein.Module> = if (propertyOrNull(FILES_STORAGE_PROPERTY).equals("s3", ignoreCase = true)) setOf(filesS3Module) else emptySet()
}
