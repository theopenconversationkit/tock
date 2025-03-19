/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import ai.tock.bot.connector.whatsapp.cloud.model.template.WhatsappTemplate

/**
 * Provides templates for use in WhatsApp messages.
 *
 * The implementation is loaded at runtime to list all available connectors, using the JDK's [java.util.ServiceLoader]
 * - you need to provide a `META-INF/services/ai.tock.bot.connector.whatsapp.cloud.spi.WhatsappTemplateProvider` file.
 */
interface WhatsappTemplateProvider {
    /**
     * Called at connector initialization time to gather the list of templates to sync with the WhatsApp Business API.
     *
     * This method is called at least once for every WhatsApp connector with a set *Meta Application Id* property
     *
     * Web verticles may not be set up by the time this method is called (HTTP calls from the application to itself may get rejected).
     *
     * @return a list of templates for later use in messages
     */
    fun createTemplates(ctx: TemplateGenerationContext): List<WhatsappTemplate>

    /**
     * @return a set of names for templates that were created by past versions of this provider and should be cleaned up
     */
    fun getRemovedTemplateNames(ctx: TemplateManagementContext): Set<String> = emptySet()
}
