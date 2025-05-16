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

package ai.tock.bot.connector.whatsapp.cloud.spi

import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateBody
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateCardButtons
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateCarousel
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateComponent
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateFooter
import ai.tock.bot.connector.whatsapp.cloud.model.template.TemplateHeader
import ai.tock.bot.connector.whatsapp.cloud.model.template.WhatsappTemplate
import ai.tock.bot.connector.whatsapp.cloud.model.template.WhatsappTemplateCategory
import ai.tock.translator.I18nContext
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.Translator
import ai.tock.translator.UserInterfaceType
import java.util.Locale

abstract class WhatsappTemplateBuilder(val name: String, val locale: Locale, connectorId: String) {
    private val i18nContext = I18nContext(locale, UserInterfaceType.textChat, connectorId)

    /**
     * The category to which the template should belong.
     *
     * Not every category is permissible depending on the template content
     *
     * [Template Categorization Docs](https://developers.facebook.com/docs/whatsapp/updates-to-pricing/new-template-guidelines)
     */
    var category: WhatsappTemplateCategory = WhatsappTemplateCategory.MARKETING

    /**
     * The language code used to create the template.
     *
     * Must be one of the [supported languages for message templates](https://developers.facebook.com/docs/whatsapp/business-management-api/message-templates/supported-languages/)
     */
    var templateLanguage: String = locale.language

    var body: TemplateBody? = null

    fun translate(text: I18nLabelValue): String {
        return Translator.translate(text, i18nContext).toString()
    }

    protected open fun components(): List<TemplateComponent> = listOf(checkNotNull(body) {
        "Missing 'body' field"
    })

    internal fun build() = WhatsappTemplate(name, templateLanguage, components(), category)
}

class WhatsappBasicTemplateBuilder(name: String, locale: Locale, connectorId: String) : WhatsappTemplateBuilder(name, locale, connectorId) {
    var header: TemplateHeader? = null
    var footer: TemplateFooter? = null
    var buttons: TemplateCardButtons? = null

    override fun components(): List<TemplateComponent> {
        return super.components() + listOfNotNull(header, footer)
    }
}

class WhatsappCarouselBuilder(name: String, locale: Locale, connectorId: String) : WhatsappTemplateBuilder(name, locale, connectorId) {
    var carousel: TemplateCarousel? = null

    override fun components(): List<TemplateComponent> {
        return super.components() + checkNotNull(carousel) {
            "Missing 'carousel' field"
        }
    }
}
