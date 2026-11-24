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

package ai.tock.storage.dynamodb

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoTockCacheTest {
    data class Attachment(
        val url: String,
        val size: Int,
    )

    @BeforeAll
    fun start() {
        DynamoLocal.start()
    }

    @Test
    fun `GIVEN a string, bytes and an object WHEN they are cached THEN they are read back, by id and by type`() {
        DynamoTockCache.put("url1".toId<String>(), "messenger_cache_url", "attachment1")
        DynamoTockCache.put("file.png".toId<ByteArray>(), "_uploaded", byteArrayOf(1, 2, 3))
        DynamoTockCache.put("a1".toId<Attachment>(), "attachments", Attachment("https://x", 42))

        assertEquals("attachment1", DynamoTockCache.get("url1".toId<String>(), "messenger_cache_url"))
        assertContentEquals(byteArrayOf(1, 2, 3), DynamoTockCache.get("file.png".toId<ByteArray>(), "_uploaded"))
        assertEquals(Attachment("https://x", 42), DynamoTockCache.get("a1".toId<Attachment>(), "attachments"))
        assertEquals(mapOf<Id<String>, Any>("url1".toId<String>() to "attachment1"), DynamoTockCache.getAll<String>("messenger_cache_url"))
        assertNull(DynamoTockCache.get("url1".toId<String>(), "_uploaded"), "the types are separated")

        DynamoTockCache.remove("url1".toId<String>(), "messenger_cache_url")
        assertNull(DynamoTockCache.get("url1".toId<String>(), "messenger_cache_url"))
    }
}
