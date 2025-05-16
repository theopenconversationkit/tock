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

import ai.tock.bot.connector.whatsapp.cloud.model.common.MetaUploadHandle
import ai.tock.bot.connector.whatsapp.cloud.model.template.WhatsappTemplate
import ai.tock.bot.connector.whatsapp.cloud.services.WhatsAppCloudApiService
import java.util.Locale

open class TemplateManagementContext(
    val connectorId: String,
    val businessAccountId: String,
    val metaApplicationId: String,
)

class TemplateGenerationContext internal constructor(
    connectorId: String,
    businessAccountId: String,
    metaApplicationId: String,
    private val apiService: WhatsAppCloudApiService,
): TemplateManagementContext(connectorId, businessAccountId, metaApplicationId) {
    fun buildBasicTemplate(name: String, locale: Locale, builder: WhatsappBasicTemplateBuilder.() -> Unit): WhatsappTemplate {
        return WhatsappBasicTemplateBuilder(name, locale, connectorId).apply(builder).build()
    }

    fun buildCarousel(name: String, locale: Locale, builder: WhatsappCarouselBuilder.() -> Unit): WhatsappTemplate {
        return WhatsappCarouselBuilder(name, locale, connectorId).apply(builder).build()
    }

    /**
     * Uploads an asset file or gets an existing handle if it was previously uploaded
     *
     * Only the following file types are accepted:
     * - `application/pdf`
     * - `image/jpeg`
     * - `image/jpg`
     * - `image/png`
     * - `video/mp4`
     *
     * The asset uploading will be done from the TOCK server, meaning the [fileUrl] does not have to be public.
     * If [fileContents] is specified, the contents will be used instead of downloading from [fileUrl].
     *
     * [fileUrl] is required regardless of the presence of [fileContents] and must be unique per file, as it is used as an
     * ID to avoid uploading the same file to Meta twice.
     *
     * @param fileUrl a valid URL pointing to a file of a valid type
     * @param fileType the MIME type of the file
     * @param fileContents the contents of the file, if already known
     * @return a handle for use in template headers
     * @throws AssetUploadingException
     */
    fun getOrUpload(fileUrl: String, fileType: String, fileContents: ByteArray? = null): MetaUploadHandle {
        return apiService.getOrUpload(metaApplicationId, fileUrl, fileType, fileContents)
    }
}

open class AssetUploadingException(message: String) : RuntimeException(message)
