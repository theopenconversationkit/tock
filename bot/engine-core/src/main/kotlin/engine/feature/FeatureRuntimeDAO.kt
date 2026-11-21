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

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import java.time.ZonedDateTime

/**
 * The part of [FeatureDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [FeatureDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface FeatureRuntimeDAO {
    suspend fun isEnabled(
        botId: String,
        namespace: String,
        type: FeatureType,
        default: Boolean = false,
        userId: String? = null,
    ): Boolean

    suspend fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        default: Boolean = false,
        userId: String? = null,
    ): Boolean

    suspend fun isEnabled(
        botId: String,
        namespace: String,
        type: FeatureType,
        applicationId: String? = null,
        default: Boolean = false,
        userId: String? = null,
    ): Boolean

    suspend fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String? = null,
        default: Boolean = false,
        userId: String? = null,
    ): Boolean

    companion object {
        /**
         * The bound [FeatureRuntimeDAO], or the bound [FeatureDAO] adapted to it.
         */
        fun fromInjector(): FeatureRuntimeDAO = injector.provideOrDefault<FeatureRuntimeDAO> { injector.provide<FeatureDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [FeatureRuntimeDAO].
 */
fun FeatureDAO.asRuntimeDAO(): FeatureRuntimeDAO = FeatureRuntimeDAOAdapter(this)

private class FeatureRuntimeDAOAdapter(
    private val dao: FeatureDAO,
) : FeatureRuntimeDAO {
    override suspend fun isEnabled(
        botId: String,
        namespace: String,
        type: FeatureType,
        default: Boolean,
        userId: String?,
    ): Boolean = dao.isEnabled(botId, namespace, type, default, userId)

    override suspend fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        default: Boolean,
        userId: String?,
    ): Boolean = dao.isEnabled(botId, namespace, category, name, default, userId)

    override suspend fun isEnabled(
        botId: String,
        namespace: String,
        type: FeatureType,
        applicationId: String?,
        default: Boolean,
        userId: String?,
    ): Boolean = dao.isEnabled(botId, namespace, type, applicationId, default, userId)

    override suspend fun isEnabled(
        botId: String,
        namespace: String,
        category: String,
        name: String,
        applicationId: String?,
        default: Boolean,
        userId: String?,
    ): Boolean = dao.isEnabled(botId, namespace, category, name, applicationId, default, userId)
}
