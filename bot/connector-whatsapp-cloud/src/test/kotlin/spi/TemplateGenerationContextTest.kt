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

package spi

import ai.tock.bot.connector.whatsapp.cloud.model.common.MetaUploadHandle
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateBody
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateCard
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateCardBody
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateCardHeader
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateCarousel
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateFooter
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateHeader
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateQuickReply
import ai.tock.bot.connector.whatsapp.cloud.model.template.WhatsappTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.template.WhatsappTemplateCategory
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import ai.tock.bot.connector.whatsapp.cloud.spi.TemplateGenerationContext
import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class TemplateGenerationContextTest {
    @Test
    fun `carousel result serializes correctly`() {
        val metaApplicationId = "test-app"
        val apiService = mockk<WhatsAppCloudApiService> {
            every { getOrUpload(metaApplicationId, "a", "image/png") } returns MetaUploadHandle("aaa")
            every { getOrUpload(metaApplicationId, "b", "video/mp4") } returns MetaUploadHandle("aaa")
        }
        val ctx = TemplateGenerationContext("test-connector", "123456789", metaApplicationId, apiService)
        val template = ctx.buildCarousel("test", Locale("en", "gb")) {
            body = TemplateBody("Hello, {{0}}", "World!")
            carousel = TemplateCarousel(
                TemplateCard(
                    TemplateCardHeader.image(ctx.getOrUpload("a", "image/png")),
                    TemplateCardBody("Body A"),
                    TemplateQuickReply("Button 1a"),
                    TemplateQuickReply("Button 2a"),
                ),
                TemplateCard(
                    TemplateCardHeader.video(ctx.getOrUpload("b", "video/mp4")),
                    TemplateCardBody("Body B"),
                    TemplateQuickReply("Button 1b"),
                ),
            )
        }
        val serialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(template)
        println(serialized)
        assertEquals(template, mapper.readValue<WhatsappTemplate>(serialized))
    }

    @Test
    fun `basic template result serializes correctly`() {
        val ctx = TemplateGenerationContext("test-connector", "123456789", "test-app", mockk())
        val template = ctx.buildBasicTemplate("test", Locale.ENGLISH) {
            category = WhatsappTemplateCategory.UTILITY
            header = TemplateHeader.text("Greetings")
            body = TemplateBody("Hello, {{0}}", "World!")
            footer = TemplateFooter("Message sent from a pineapple")
        }
        val serialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(template)
        println(serialized)
        assertEquals(template, mapper.readValue<WhatsappTemplate>(serialized))
    }
}
