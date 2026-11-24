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
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.config.UploadedFilesStorage
import mu.KotlinLogging
import java.time.Duration

/**
 * Stores the uploaded files in an S3 bucket, at `<prefix><file id>`.
 *
 * @param publicBaseUrl the public url of the bucket prefix (a CloudFront distribution...): the connectors download the files from it
 * @param presignedUrlDuration when not null, the bot redirects the downloads to presigned urls valid for this duration, instead of sending the files
 * @param previousStorage the storage of the files uploaded before: the files not found in S3 are copied from it
 */
internal class S3UploadedFilesStorage(
    private val bucket: S3Bucket,
    publicBaseUrl: String?,
    private val presignedUrlDuration: Duration?,
    private val previousStorage: UploadedFilesStorage?,
) : UploadedFilesStorage {
    private val logger = KotlinLogging.logger {}

    private val publicBaseUrl: String? = publicBaseUrl?.trim()?.takeUnless { it.isEmpty() }?.trimEnd('/')

    override fun save(
        fileId: String,
        contentType: String,
        bytes: ByteArray,
    ) {
        bucket.put(fileId, bytes, contentType)
    }

    override fun load(fileId: String): ByteArray? = bucket.getBytes(fileId) ?: migrate(fileId)

    override fun publicUrl(fileId: String): String? = publicBaseUrl?.let { "$it/$fileId" }

    override fun redirectUrl(fileId: String): String? =
        presignedUrlDuration?.let { duration ->
            // a redirection to a file not copied yet from the previous storage would fail
            if (bucket.head(fileId) != null || migrate(fileId) != null) {
                bucket.presignedGetUrl(fileId, duration)
            } else {
                null
            }
        }

    /**
     * Copies the file from the previous storage, if it is there.
     */
    private fun migrate(fileId: String): ByteArray? =
        previousStorage?.load(fileId)?.also { bytes ->
            logger.info { "copy the uploaded file $fileId to s3://${bucket.bucket}/${bucket.key(fileId)}" }
            bucket.put(fileId, bytes, contentType(fileId))
        }

    private fun contentType(fileId: String): String = UploadedFilesService.guessContentType(fileId)
}
