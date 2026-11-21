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

package ai.tock.bot.admin.indicators

import org.litote.kmongo.Id

interface IndicatorDAO {
    /**
     * Save an indicator.
     * @param indicator the indicator to save
     */
    fun save(indicator: Indicator)

    /**
     * Check if an indicator exists.
     * @param name the indicator name
     * @param namespace the namespace
     * @param botId the bot id
     */
    fun existByNameAndBotId(
        name: String,
        namespace: String,
        botId: String,
    ): Boolean

    /**
     * Find an indicator by its name, namespace and bot id.
     * @param name the indicator name
     * @param namespace the namespace
     * @param botId the bot id
     */
    fun findByNameAndBotId(
        name: String,
        namespace: String,
        botId: String,
    ): Indicator?

    /**
     * Find all indicators by namespace and bot id
     * @param namespace the namespace
     * @param botId the bot id
     */
    fun findAllByBotId(
        namespace: String,
        botId: String,
    ): List<Indicator>

    /**
     * Find all indicators
     */
    fun findAll(): List<Indicator>

    /**
     * Delete an indicator by its id
     * @param id the indicator id
     */
    fun delete(id: Id<Indicator>): Boolean

    /**
     * Delete an indicator by its name, namespace and its application name
     * @param name indicator name
     * @param namespace the namespace
     * @param botId the application name
     */
    fun deleteByNameAndApplicationName(
        name: String,
        namespace: String,
        botId: String,
    ): Boolean

    fun deleteByApplicationName(
        namespace: String,
        botId: String,
    ): Boolean
}
