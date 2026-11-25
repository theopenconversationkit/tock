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

package ai.tock.bot.admin.indicators.metric

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault

/**
 * The part of [MetricDAO] that the bots use at runtime.
 *
 * A storage that only serves the bots (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [MetricDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface MetricRuntimeDAO {
    fun save(metric: Metric)

    fun saveAll(metrics: List<Metric>)

    companion object {
        /**
         * The bound [MetricRuntimeDAO], or the bound [MetricDAO] adapted to it.
         */
        fun fromInjector(): MetricRuntimeDAO = injector.provideOrDefault<MetricRuntimeDAO> { injector.provide<MetricDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [MetricRuntimeDAO].
 */
fun MetricDAO.asRuntimeDAO(): MetricRuntimeDAO = MetricRuntimeDAOAdapter(this)

private class MetricRuntimeDAOAdapter(
    private val dao: MetricDAO,
) : MetricRuntimeDAO {
    override fun save(metric: Metric) = dao.save(metric)

    override fun saveAll(metrics: List<Metric>) = dao.saveAll(metrics)
}
