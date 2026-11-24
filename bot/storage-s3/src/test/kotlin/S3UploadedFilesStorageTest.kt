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
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class S3UploadedFilesStorageTest {
    private val bucket = S3Bucket(S3TestServer.BUCKET, "test/files/", S3TestServer.client) { S3TestServer.presigner }
    private val previousStorage: UploadedFilesStorage = mockk()
    private val storage = S3UploadedFilesStorage(bucket, null, Duration.ofMinutes(5), previousStorage)
    private val bytes = "image content".toByteArray()

    private fun fileId(): String = "ns${UUID.randomUUID()}.png"

    private fun download(url: String): HttpResponse<ByteArray> = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofByteArray())

    @AfterEach
    fun cleanup() {
        tockInternalInjector = KodeinInjector()
    }

    @Test
    fun `a saved file is loaded with its content type`() {
        val fileId = fileId()

        storage.save(fileId, "image/png", bytes)

        assertContentEquals(bytes, storage.load(fileId))
        assertEquals("image/png", bucket.head(fileId)?.contentType())
        assertEquals("test/files/$fileId", bucket.key(fileId))
    }

    @Test
    fun `a file of the previous storage is copied to S3 when it is read`() {
        val fileId = fileId()
        every { previousStorage.load(fileId) } returns bytes

        assertContentEquals(bytes, storage.load(fileId))
        assertContentEquals(bytes, storage.load(fileId))

        verify(exactly = 1) { previousStorage.load(fileId) }
        assertContentEquals(bytes, bucket.getBytes(fileId))
        assertEquals("image/png", bucket.head(fileId)?.contentType())
    }

    @Test
    fun `an unknown file is not found`() {
        val fileId = fileId()
        every { previousStorage.load(fileId) } returns null

        assertNull(storage.load(fileId))
        assertNull(storage.redirectUrl(fileId))
        assertNull(S3UploadedFilesStorage(bucket, null, null, null).load(fileId))
    }

    @Test
    fun `the downloads are redirected to a presigned url`() {
        val fileId = fileId()
        storage.save(fileId, "image/png", bytes)

        val url = assertNotNull(storage.redirectUrl(fileId))

        assertContentEquals(bytes, download(url).body())
        assertNull(S3UploadedFilesStorage(bucket, null, null, null).redirectUrl(fileId))
    }

    @Test
    fun `a file of the previous storage is copied to S3 before the redirection`() {
        val fileId = fileId()
        every { previousStorage.load(fileId) } returns bytes

        val url = assertNotNull(storage.redirectUrl(fileId))

        assertContentEquals(bytes, download(url).body())
    }

    @Test
    fun `the public url is the public base url followed by the file id`() {
        val withPublicUrl = S3UploadedFilesStorage(bucket, "https://cdn.example.com/files/ ", null, null)

        assertEquals("https://cdn.example.com/files/a.png", withPublicUrl.publicUrl("a.png"))
        assertNull(storage.publicUrl("a.png"))
    }

    @Test
    fun `UploadedFilesService stores the files in the injected storage`() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(
            Kodein {
                import(sharedTestModule)
                bind<UploadedFilesStorage>() with provider { storage }
            },
        )

        val descriptor = assertNotNull(UploadedFilesService.uploadFile("ns", "Image.PNG", bytes))

        val fileId = "${descriptor.id}.${descriptor.suffix}"
        assertContentEquals(bytes, bucket.getBytes(fileId))
        assertEquals("image/png", bucket.head(fileId)?.contentType())
        assertContentEquals(bytes, UploadedFilesService.getFileContentFromUrl("https://bot/f/$fileId"))
    }
}
