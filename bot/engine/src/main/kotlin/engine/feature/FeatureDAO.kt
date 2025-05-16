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

import java.time.ZonedDateTime

/**
 * Feature DAO.
 */
interface FeatureDAO {

    fun isEnabled(
        botId: String, namespace: String, type: FeatureType, default: Boolean = false, userId: String? = null,
    ): Boolean =
        isEnabled(botId, namespace, type.category, type.name, null, default, userId)

    fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        default: Boolean = false,
        userId: String? = null,
    ): Boolean =
        isEnabled(botId, namespace, category, name, null, default, userId)

    fun isEnabled(
        botId: String,
        namespace: String,
        type: FeatureType,
        applicationId: String? = null,
        default: Boolean = false,
        userId: String? = null,
    ): Boolean =
        isEnabled(botId, namespace, type.category, type.name, applicationId, default, userId)

    fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String? = null,
        default: Boolean = false,
        userId: String? = null,
    ): Boolean

    fun enable(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        startDate: ZonedDateTime?,
        endDate: ZonedDateTime?,
        applicationId: String? = null,
        graduation: Int? = null,
    )

    fun enable(
        botId: String,
        namespace: String,
        type: FeatureType,
        startDate: ZonedDateTime? = null,
        endDate: ZonedDateTime? = null,
        applicationId: String? = null,
        graduation: Int? = null,
    ) = enable(botId, namespace, type.category, type.name, startDate, endDate, applicationId, graduation)

    fun disable(botId: String, namespace: String, category: String, name: String, applicationId: String? = null)

    fun disable(botId: String, namespace: String, type: FeatureType, applicationId: String? = null) =
        disable(botId, namespace, type.category, type.name, applicationId)

    fun getFeatures(botId: String, namespace: String): List<FeatureState>

    fun addFeature(
        botId: String,
        namespace: String,
        enabled: Boolean,
        type: FeatureType,
        startDate: ZonedDateTime?,
        endDate: ZonedDateTime?,
        applicationId: String? = null,
        graduation: Int? = null,
    ) = addFeature(botId, namespace, enabled, type.category, type.name, startDate, endDate, applicationId, graduation)

    fun addFeature(
        botId: String,
        namespace: String,
        enabled: Boolean,
        category: String,
        name: String,
        startDate: ZonedDateTime?,
        endDate: ZonedDateTime?,
        applicationId: String? = null,
        graduation: Int? = null,
    )

    fun deleteFeature(botId: String, namespace: String, type: FeatureType, applicationId: String? = null) =
        deleteFeature(botId, namespace, type.category, type.name, applicationId)

    fun deleteFeature(botId: String, namespace: String, category: String, name: String, applicationId: String? = null)
}
