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

import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.indicators.IndicatorDAO
import ai.tock.bot.admin.indicators.metric.MetricDAO
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging

/**
 * Service responsible for migrating data.
 */
object DataMigrationService {
    private val logger = KotlinLogging.logger {}
    private val metricDAO: MetricDAO get() = injector.provide()
    private val indicatorDAO: IndicatorDAO by injector.instance()
    private val botApplicationConfigurationDAO: BotApplicationConfigurationDAO by injector.instance()

    /**
     * Executes the migration process for both metrics and indicators.
     * Migrates all existing metrics and indicators by setting their namespace.
     */
    fun migrateMetricsAndIndicators() {
        val botConfigs = botApplicationConfigurationDAO.getBotConfigurations()
        var modifiedCount: Long = 0

        // update for existing bots
        botConfigs.forEach { botConfig ->
            val botId = botConfig.botId
            val namespace = botConfig.namespace

            modifiedCount = metricDAO.updateNamespaceForBot(botId, namespace)
            logger.info { "Migrated $modifiedCount Metrics for botId=$botId with namespace=$namespace" }

            modifiedCount = indicatorDAO.updateNamespaceForBot(botId, namespace)
            logger.info { "Migrated $modifiedCount Indicators for botId=$botId with namespace=$namespace" }
        }

        // fallback = "unknown"
        modifiedCount = metricDAO.updateUnknownNamespace()
        logger.info { "Set namespace='unknown' for $modifiedCount Metrics" }

        modifiedCount = indicatorDAO.updateUnknownNamespace()
        logger.info { "Set namespace='unknown' for $modifiedCount Indicators" }
    }
}