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

package ai.tock.bot.connector.whatsapp.cloud.database.repository

import ai.tock.bot.connector.whatsapp.cloud.database.model.PayloadWhatsAppCloud

/**
 * In order to workaround the characters limit of the WhatsApp API, the payloads are stored in the database.
 */
interface PayloadWhatsAppCloudDAO {

    fun getPayloadById(id: String): String?
    fun save(payloadWhatsAppCloud: PayloadWhatsAppCloud)
}
