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

package ai.tock.bot.engine.feature

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

class FeatureValidationTest {
    @Test
    fun `validateFeatureName accepts valid names`() {
        // Should not throw
        validateFeatureName("validName")
        validateFeatureName("valid_name")
        validateFeatureName("valid-name")
        validateFeatureName("valid.name")
        validateFeatureName("validName123")
        validateFeatureName("VALID_NAME")
    }

    @Test
    fun `validateFeatureName rejects name with plus sign`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                validateFeatureName("invalid+name")
            }
        assertTrue(exception.message?.contains("+") ?: false)
        assertTrue(exception.message?.contains("must not contain") ?: false)
    }

    @Test
    fun `validateFeatureName rejects name with comma`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                validateFeatureName("invalid,name")
            }
        assertTrue(exception.message?.contains(",") ?: false)
        assertTrue(exception.message?.contains("must not contain") ?: false)
    }

    @Test
    fun `validateFeatureName rejects name with both forbidden characters`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                validateFeatureName("invalid+name,test")
            }
        assertTrue(exception.message?.contains("+") ?: false)
        assertTrue(exception.message?.contains(",") ?: false)
    }

    @Test
    fun `validateFeatureCategory accepts valid categories`() {
        // Should not throw
        validateFeatureCategory("valid.category")
        validateFeatureCategory("ai.tock.bot.Feature")
        validateFeatureCategory("com.example.MyFeature")
    }

    @Test
    fun `validateFeatureCategory rejects category with plus sign`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                validateFeatureCategory("invalid+category")
            }
        assertTrue(exception.message?.contains("+") ?: false)
        assertTrue(exception.message?.contains("must not contain") ?: false)
    }

    @Test
    fun `validateFeatureCategory rejects category with comma`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                validateFeatureCategory("invalid,category")
            }
        assertTrue(exception.message?.contains(",") ?: false)
        assertTrue(exception.message?.contains("must not contain") ?: false)
    }

    @Test
    fun `validateFeatureCategory rejects category with both forbidden characters`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                validateFeatureCategory("invalid+category,test")
            }
        assertTrue(exception.message?.contains("+") ?: false)
        assertTrue(exception.message?.contains(",") ?: false)
    }
}
