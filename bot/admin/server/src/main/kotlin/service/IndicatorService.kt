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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.admin.indicators.IndicatorError
import ai.tock.bot.admin.indicators.IndicatorValue
import ai.tock.bot.admin.indicators.PredefinedIndicators
import ai.tock.bot.admin.model.Valid
import ai.tock.bot.admin.model.indicator.IndicatorResponse
import ai.tock.bot.admin.model.indicator.SaveIndicatorRequest
import ai.tock.bot.admin.model.indicator.UpdateIndicatorRequest
import ai.tock.bot.admin.model.indicator.toIndicator
import ai.tock.bot.admin.model.indicator.toResponse
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance

object IndicatorService {

    private val dao: IndicatorDAO by injector.instance()

    /**
     * Save an indicator
     * @param namespace the namespace
     * @param botId the name of the application which the indicator is linked to
     * @param request the save request
     * @throws [IndicatorError.IndicatorAlreadyExists]
     */
    fun save(namespace: String, botId: String, request: Valid<SaveIndicatorRequest>) = request.data.let {
        if (!PredefinedIndicators.has(it.name) && dao.existByNameAndBotId(it.name, namespace, botId)) {
            throw IndicatorError.IndicatorAlreadyExists(it.name, it.label, namespace, botId)
        }
        dao.save(toIndicator(namespace, botId, it))
    }

    /**
     * Update an indicator
     * @param namespace the namespace
     * @param botId the name of the application which the indicator is linked to
     * @param indicatorName the name of indicator to update
     * @param request the update request
     * @throws [IndicatorError.IndicatorNotFound]
     */
    fun update(namespace: String, botId: String, indicatorName: String, request: Valid<UpdateIndicatorRequest>) = request.data.let {
        findIndicatorAndMap(indicatorName, namespace, botId) { it }.let { indicator ->
            dao.save(
                indicator.copy(
                    label = it.label,
                    description = it.description,
                    dimensions = it.dimensions,
                    values = it.values.map { value -> IndicatorValue(value.name, value.label) }.toSet()
                )
            )
        }
    }

    /**
     * Retrieve an indicator with a given name and application name
     * @param indicatorName the expected indicator name
     * @param namespace the namespace
     * @param botId the name of the application which the indicator is linked to
     * @throws [IndicatorError.IndicatorNotFound]
     * @return [IndicatorResponse]
     */
    fun findByNameAndBotId(indicatorName: String, namespace: String, botId: String): IndicatorResponse =
        findIndicatorAndMap(indicatorName, namespace, botId) {
            toResponse(it)
        }

    /**
     * Retrieve all indicators with a given application name
     * @param namespace the namespace
     * @param botId the name of the application which the indicator is linked to
     * @return List<[IndicatorResponse]>
     */
    fun findAllByBotId(namespace: String, botId: String): List<IndicatorResponse> = dao.findAllByBotId(namespace, botId).map {
        toResponse(it)
    }

    /**
     * Retrieve all indicators
     * @return List<[IndicatorResponse]>
     */
    fun findAll(): List<IndicatorResponse> = dao.findAll().map {
        toResponse(it)
    }

    /**
     * Delete an indicator
     * @param name the indicator name to delete
     * @param namespace the namespace
     * @param botId the application name associated to the indicator
     * @throws [IndicatorError.IndicatorDeletionFailed]
     * @return [Boolean]
     */
    fun deleteByNameAndApplicationName(name: String, namespace: String, botId: String): Boolean =
        dao.deleteByNameAndApplicationName(name, namespace, botId)
            .also { if (!it) throw IndicatorError.IndicatorDeletionFailed(name, namespace, botId) }

    /**
     * Find an indicator with a given name and application name and map it into a desired type
     * @param name the indicator name
     * @param namespace the namespace
     * @param botId the application name
     * @param mapper the indicator's mapping function
     * @throws [IndicatorError.IndicatorNotFound] in case indicator is not found
     */
    private fun <T> findIndicatorAndMap(
        name: String,
        namespace: String,
        botId: String,
        mapper: (Indicator) -> T
    ): T =
        dao.findByNameAndBotId(name, namespace, botId)?.let { mapper(it) } ?: throw IndicatorError.IndicatorNotFound(name, namespace, botId)

}
