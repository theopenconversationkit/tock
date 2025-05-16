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
package ai.tock.stt.google

import ai.tock.shared.property
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale

/**
 *
 */
class GoogleSpeechClientIntegrationTest {

    @Test
    fun testBasicFile() {
        val filePath = property("audio_test_file", "Please provide a local file path")
        val data = Files.readAllBytes(Paths.get(filePath))

        println(GoogleSpeechClient.parse(data, Locale.ENGLISH))
    }
}
