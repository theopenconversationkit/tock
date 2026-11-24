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
package ai.tock.nlp.model.service.storage.s3

import ai.tock.nlp.model.service.storage.NlpEngineModelDAO
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class S3ModelIocTest {
    @AfterEach
    fun cleanup() {
        System.clearProperty("tock_nlp_model_storage")
        System.clearProperty("tock_nlp_model_s3_bucket")
    }

    @Test
    fun `the S3 storage is not used by default`() {
        assertEquals(emptySet(), S3ModelModulesService().customModules())
    }

    @Test
    fun `the S3 storage overrides the Mongo storage when tock_nlp_model_storage is s3`() {
        System.setProperty("tock_nlp_model_storage", "S3")
        System.setProperty("tock_nlp_model_s3_bucket", "models")
        val mongoDAO: NlpEngineModelDAO = mockk()

        val kodein =
            Kodein {
                import(Kodein.Module { bind<NlpEngineModelDAO>() with provider { mongoDAO } }, allowOverride = true)
                S3ModelModulesService().customModules().forEach { import(it, allowOverride = true) }
            }

        assertIs<S3NlpEngineModelDAO>(kodein.instance<NlpEngineModelDAO>())
    }
}
