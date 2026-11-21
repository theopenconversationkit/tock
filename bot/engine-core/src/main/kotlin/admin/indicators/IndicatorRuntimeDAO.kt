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

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id

/**
 * The part of [IndicatorDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [IndicatorDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface IndicatorRuntimeDAO {
    fun save(indicator: Indicator)

    fun findByNameAndBotId(
        name: String,
        namespace: String,
        botId: String,
    ): Indicator?

    companion object {
        /**
         * The bound [IndicatorRuntimeDAO], or the bound [IndicatorDAO] adapted to it.
         */
        fun fromInjector(): IndicatorRuntimeDAO = injector.provideOrDefault<IndicatorRuntimeDAO> { injector.provide<IndicatorDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [IndicatorRuntimeDAO].
 */
fun IndicatorDAO.asRuntimeDAO(): IndicatorRuntimeDAO = IndicatorRuntimeDAOAdapter(this)

private class IndicatorRuntimeDAOAdapter(
    private val dao: IndicatorDAO,
) : IndicatorRuntimeDAO {
    override fun save(indicator: Indicator) = dao.save(indicator)

    override fun findByNameAndBotId(
        name: String,
        namespace: String,
        botId: String,
    ): Indicator? = dao.findByNameAndBotId(name, namespace, botId)
}
