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

package model

import ai.tock.bot.admin.model.BotMediaActionDescriptor
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private val LABEL = I18nLabel(
    _id = "id".toId(),
    namespace = "syntactic_namespace",
    category = "syntactic_category",
    i18n = LinkedHashSet()
)

internal class BotMediaActionDescriptorTest {

    @Test
    internal fun `GIVEN BotMediaActionDescriptor with an empty url WHEN toDescriptor is called THEN transform to a null url`() {
        val descriptor = BotMediaActionDescriptor(title = LABEL, url = "")

        val transformation = descriptor.toDescriptor()

        assertNotNull(transformation)
        assertEquals(transformation.title, I18nLabelValue(descriptor.title))
        assertNull(transformation.url)
    }

    @Test
    internal fun `GIVEN BotMediaActionDescriptor with an blank url WHEN toDescriptor is called THEN transform to a null url`() {
        val descriptor = BotMediaActionDescriptor(title = LABEL, url = "            ")

        val transformation = descriptor.toDescriptor()

        assertNotNull(transformation)
        assertEquals(transformation.title, I18nLabelValue(descriptor.title))
        assertNull(transformation.url)
    }

    @Test
    internal fun `GIVEN BotMediaActionDescriptor with an null url WHEN toDescriptor is called THEN transform to a null url`() {
        val descriptor = BotMediaActionDescriptor(title = LABEL)

        val transformation = descriptor.toDescriptor()

        assertNotNull(transformation)
        assertEquals(transformation.title, I18nLabelValue(descriptor.title))
        assertNull(transformation.url)
    }
}
