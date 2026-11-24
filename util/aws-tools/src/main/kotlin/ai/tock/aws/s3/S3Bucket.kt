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

package ai.tock.aws.s3

import ai.tock.shared.booleanProperty
import ai.tock.shared.propertyOrNull
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI
import java.nio.file.Path
import java.time.Duration

/**
 * The S3 clients of Tock, configured with:
 * - `tock_s3_endpoint`: an S3 compatible endpoint (MinIO, Google Cloud Storage with HMAC keys...), AWS S3 if not set
 * - `tock_s3_region`: the region, the default AWS region provider chain if not set
 * - `tock_s3_path_style`: path style access (`http://endpoint/bucket/key`), true by default when an endpoint is set
 *
 * The credentials come from the default AWS credentials provider chain.
 */
object S3Clients {
    private val endpoint: URI? = propertyOrNull("tock_s3_endpoint")?.takeUnless { it.isBlank() }?.let { URI.create(it) }
    private val pathStyle: Boolean = booleanProperty("tock_s3_path_style", endpoint != null)

    private val region: Region by lazy {
        propertyOrNull("tock_s3_region")?.takeUnless { it.isBlank() }?.let { Region.of(it) }
            ?: runCatching { DefaultAwsRegionProviderChain().region }.getOrNull()
            // the region does not matter for most S3 compatible storages
            ?: Region.US_EAST_1
    }

    val client: S3Client by lazy {
        S3Client
            .builder()
            .region(region)
            .forcePathStyle(pathStyle)
            .apply { endpoint?.let { endpointOverride(it) } }
            // the default checksums of the SDK are not supported by all the S3 compatible storages
            .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
            .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
            .build()
    }

    val presigner: S3Presigner by lazy {
        S3Presigner
            .builder()
            .region(region)
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build())
            .apply { endpoint?.let { endpointOverride(it) } }
            .build()
    }
}

/**
 * The objects of an S3 [bucket] whose keys start with a [prefix]: the paths of this class are relative to the prefix.
 */
class S3Bucket(
    val bucket: String,
    prefix: String = "",
    private val client: S3Client = S3Clients.client,
    private val presigner: () -> S3Presigner = { S3Clients.presigner },
) {
    /**
     * The prefix of the keys, empty or ending with a /.
     */
    val prefix: String = prefix.trim('/').let { if (it.isEmpty()) "" else "$it/" }

    fun key(path: String): String = prefix + path

    fun put(
        path: String,
        bytes: ByteArray,
        contentType: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        put(path, RequestBody.fromBytes(bytes), contentType, metadata)
    }

    fun put(
        path: String,
        file: Path,
        contentType: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        put(path, RequestBody.fromFile(file), contentType, metadata)
    }

    private fun put(
        path: String,
        body: RequestBody,
        contentType: String?,
        metadata: Map<String, String>,
    ) {
        client.putObject(
            { request ->
                request
                    .bucket(bucket)
                    .key(key(path))
                    .metadata(metadata)
                    .apply { contentType?.let { contentType(it) } }
            },
            body,
        )
    }

    /**
     * Returns the content of the object, null if it does not exist. The stream must be closed.
     */
    fun get(path: String): ResponseInputStream<GetObjectResponse>? = notFoundAsNull { client.getObject { it.bucket(bucket).key(key(path)) } }

    fun getBytes(path: String): ByteArray? = get(path)?.use { it.readBytes() }

    fun head(path: String): HeadObjectResponse? = notFoundAsNull { client.headObject { it.bucket(bucket).key(key(path)) } }

    /**
     * Lists the objects whose path starts with [pathPrefix]. The keys of the returned objects are not relative to the [prefix].
     */
    fun list(pathPrefix: String = ""): Sequence<S3Object> =
        client
            .listObjectsV2Paginator { it.bucket(bucket).prefix(key(pathPrefix)) }
            .contents()
            .asSequence()

    /**
     * The path of an object returned by [list].
     */
    fun path(o: S3Object): String = o.key().removePrefix(prefix)

    fun delete(path: String) {
        client.deleteObject { it.bucket(bucket).key(key(path)) }
    }

    fun delete(paths: Collection<String>) {
        // at most 1000 keys by request
        paths.chunked(1000).forEach { chunk ->
            client.deleteObjects { request ->
                request.bucket(bucket).delete(
                    Delete
                        .builder()
                        .objects(chunk.map { ObjectIdentifier.builder().key(key(it)).build() })
                        .quiet(true)
                        .build(),
                )
            }
        }
    }

    /**
     * Returns a presigned url to download the object, valid for the [duration].
     */
    fun presignedGetUrl(
        path: String,
        duration: Duration,
    ): String =
        presigner()
            .presignGetObject { request ->
                request.signatureDuration(duration).getObjectRequest { it.bucket(bucket).key(key(path)) }
            }.url()
            .toExternalForm()

    private fun <T> notFoundAsNull(call: () -> T): T? =
        try {
            call()
        } catch (e: S3Exception) {
            if (e.statusCode() == 404) null else throw e
        }
}
