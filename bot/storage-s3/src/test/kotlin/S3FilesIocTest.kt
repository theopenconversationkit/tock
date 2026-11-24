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

import ai.tock.bot.engine.config.UploadedFilesStorage
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class S3FilesIocTest {
    @AfterEach
    fun cleanup() {
        System.clearProperty("tock_bot_files_storage")
        System.clearProperty("tock_bot_files_s3_bucket")
    }

    @Test
    fun `the S3 storage is not used by default`() {
        assertEquals(emptySet(), S3FilesModulesService().customModules())
    }

    @Test
    fun `the S3 storage is bound when tock_bot_files_storage is s3`() {
        System.setProperty("tock_bot_files_storage", "s3")
        System.setProperty("tock_bot_files_s3_bucket", "files")

        // no default binding: the uploaded files service falls back to the Tock cache
        val kodein = Kodein { S3FilesModulesService().customModules().forEach { import(it, allowOverride = true) } }

        assertIs<S3UploadedFilesStorage>(kodein.instance<UploadedFilesStorage>())
    }
}
