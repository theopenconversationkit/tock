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

import DataMigrationMetricMongoDAO
import MetricProjectionResult
import mu.KotlinLogging

/**
 * Service responsible for migrating data.
 */
object DataMigrationService {
    private val logger = KotlinLogging.logger {}

    /**
     * Migrates metrics by updating their `namespace` and `applicationId` field.
     *
     * Resolves the `namespace` and `applicationId` from related dialogs (for metrics where it is missing).
     */
    fun migrateMetrics(){
        // Count the number of metrics eligible for migration
        val eligibleMetricsCount = DataMigrationMetricMongoDAO.countEligibleMetrics()
        logger.info { "eligibleMetricsCount=$eligibleMetricsCount" }

        // If no metrics are eligible, cancel the migration early
        if(eligibleMetricsCount == 0L){
            logger.warn { "Migration of metrics is canceled: No metric is eligible for migration." }
            return
        }

        // Retrieve the list of MetricProjectionResult containing metricId, dialogNamespace, and dialogApplicationId
        val metricProjections : List<MetricProjectionResult> = DataMigrationMetricMongoDAO.getMetricProjections()

        // Verify that the count of projections matches the count of eligible metrics
        // This ensures data consistency between counting and fetching projections
        if (eligibleMetricsCount != metricProjections.size.toLong()){
            logger.error { "The migration of metrics has failed! (eligibleMetricsCount=$eligibleMetricsCount != metricProjections=${metricProjections.size})" }
            logger.warn { "distinctMetricProjections=${metricProjections.distinctBy { 
                Triple(it.metricId, it.dialogNamespace, it.dialogApplicationId) }.size}" }
            return
        }

        // Perform bulk update on eligible metrics with their namespace and applicationId
        val modifiedCount: Long = DataMigrationMetricMongoDAO.updateMetricByProjections(metricProjections)
        logger.info { "Migrated $modifiedCount Metrics with their namespace and applicationId" }

        // Log partial success or full success
        if(modifiedCount != eligibleMetricsCount){
            logger.warn { "The migration of metrics was partially successful. ${eligibleMetricsCount - modifiedCount} metrics have not been updated!" }
        }else{
            logger.info { "All eligible metrics have been successfully updated." }
        }
    }

    /**
     * Migrates indicators by updating their `namespace` field.
     */
    fun migrateIndicators(){
        // Count indicators eligible for migration
        val eligibleIndicatorsCount = DataMigrationMetricMongoDAO.countEligibleIndicators()
        logger.info { "eligibleIndicatorsCount=$eligibleIndicatorsCount" }

        if(eligibleIndicatorsCount == 0L){
            // No indicators need migration; exit early
            logger.warn { "Migration of indicators is canceled: No indicator is eligible for migration." }
            return
        }

        // Retrieve projections containing botId and namespace information
        val botProjections = DataMigrationMetricMongoDAO.getBotProjections()
        val duplicatesBotId = botProjections.filter { it.namespaces.size > 1 }

        if (duplicatesBotId.isNotEmpty()){
            // Prevent migration if duplicates exist to avoid inconsistent updates
            logger.error { "The migration of indicators has failed! (duplicatesBotId=$duplicatesBotId)" }
            return
        }

        // Perform bulk update on eligible indicators
        val modifiedCount: Long = DataMigrationMetricMongoDAO.updateIndicatorByProjections(botProjections)
        logger.info { "Migrated $modifiedCount Indicators with their namespace" }

        // Log partial success or full success
        if (modifiedCount != eligibleIndicatorsCount) {
            logger.warn { "The migration of indicators was partially successful. ${eligibleIndicatorsCount - modifiedCount} indicators have not been updated!" }
        } else {
            logger.info { "All eligible indicators have been successfully updated." }
        }
    }
}