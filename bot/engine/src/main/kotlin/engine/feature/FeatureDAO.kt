/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

/**
 * Feature DAO.
 */
interface FeatureDAO {

    fun isEnabled(botId: String, namespace: String, type: FeatureType, default: Boolean = false): Boolean =
        isEnabled(botId, namespace, type.category, type.name, default)

    fun isEnabled(botId: String, namespace: String, category: String, name: String, default: Boolean = false): Boolean

    fun enable(botId: String, namespace: String, category: String, name: String)

    fun enable(botId: String, namespace: String, type: FeatureType) =
        enable(botId, namespace, type.category, type.name)

    fun disable(botId: String, namespace: String, category: String, name: String)

    fun disable(botId: String, namespace: String, type: FeatureType) =
        disable(botId, namespace, type.category, type.name)

    fun getFeatures(botId: String, namespace: String): List<FeatureState>

    fun addFeature(botId: String, namespace: String, enabled: Boolean, type: FeatureType) =
        addFeature(botId, namespace, enabled, type.category, type.name)

    fun addFeature(botId: String, namespace: String, enabled: Boolean, category: String, name: String)

    fun deleteFeature(botId: String, namespace: String, type: FeatureType) =
        deleteFeature(botId, namespace, type.category, type.name)

    fun deleteFeature(botId: String, namespace: String, category: String, name: String)
}