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

import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.BotAdminService.importStories
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.dump.StoryDefinitionConfigurationDumpImport
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import ai.tock.nlp.front.shared.codec.DumpType
import ai.tock.nlp.front.shared.codec.ImportReport
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.user.UserNamespace
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.TockUserRole
import ai.tock.translator.I18nDAO
import chat.rocket.common.util.ifNull
import com.github.salomonbrys.kodein.instance
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

object SynchronizationService {
    private val front = FrontClient
    private val i18n: I18nDAO by injector.instance()
    private val storyDefinitionDAO: StoryDefinitionConfigurationDAO get() = injector.provide()
    private val botConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()
    private const val TEMP_POSTFIX = "-temp"

    fun synchronize(
        srcNamespace: String,
        srcAppName: String,
        srcAppId: Id<ApplicationDefinition>,
        targetNamespace: String,
        targetAppName: String,
        withInboxMessages: Boolean,
        userLogin: String,
    ): ImportReport {
        // Step 1: Prepare temporary application
        val configMap = prepareTempApplication(targetNamespace, targetAppName, userLogin, withInboxMessages)

        // Step 2: Migrate data from source to target
        val report = migrateData(srcNamespace, srcAppName, srcAppId, targetNamespace, targetAppName, withInboxMessages)

        // Step 3: Clean up temporary application and namespace
        cleanTempNamespaceAndApplication(
            "${targetNamespace}$TEMP_POSTFIX",
            "${targetAppName}$TEMP_POSTFIX",
            userLogin,
            configMap,
        )

        return report
    }

    private fun migrateData(
        srcNamespace: String,
        srcAppName: String,
        srcAppId: Id<ApplicationDefinition>,
        targetNamespace: String,
        targetAppName: String,
        withInboxMessages: Boolean,
    ): ImportReport {
        // Export source stories and delete target stories
        val sourceStories = BotAdminService.exportStories(srcNamespace, srcAppName)
        deleteStories(targetNamespace, targetAppName)

        // Import source stories to the target namespace and application name
        sourceStories.groupBy { it.userSentenceLocale }
            .forEach { group ->
                importStories(
                    targetNamespace,
                    targetAppName,
                    group.key ?: Locale.ENGLISH,
                    StoryDefinitionConfigurationDumpImport(group.value),
                    TockUserRole.botUser.name,
                )
            }

        // Export the application dump, optionally excluding Inbox messages
        val originalAppDump = front.export(srcAppId, DumpType.full)
        val appDumpToImport =
            if (withInboxMessages) {
                originalAppDump
            } else {
                originalAppDump.copy(sentences = emptyList())
            }

        // Import the application dump with an update to the name and namespace
        val updatedAppDump =
            appDumpToImport.copy(
                application =
                    appDumpToImport.application.copy(
                        name = targetAppName,
                        namespace = targetNamespace,
                    ),
            )

        // Perform the import with the updated application dump
        val importReport =
            front.import(
                targetNamespace,
                updatedAppDump,
                ApplicationImportConfiguration(targetAppName),
            )

        // Import source labels to the target namespace
        val sourceLabels = i18n.getLabels(srcNamespace)
        BotAdminService.importLabels(sourceLabels, targetNamespace)

        return importReport
    }

    private fun prepareTempApplication(
        targetNamespace: String,
        targetAppName: String,
        userLogin: String,
        withInboxMessages: Boolean,
    ): Map<BotApplicationConfiguration, BotApplicationConfiguration> {
        val tempNamespaceName = "${targetNamespace}$TEMP_POSTFIX"
        val tempAppName = "${targetAppName}$TEMP_POSTFIX"
        // Map to exchange paths between configurations
        val botApplicationConfigurationFitMap = mutableMapOf<BotApplicationConfiguration, BotApplicationConfiguration>()
        // Prepare Temp Namespace , Order is important here
        val tempNamespace = front.getNamespaces(userLogin).firstOrNull { it.namespace == tempNamespaceName }
        if (tempNamespace == null) {
            front.saveNamespace(UserNamespace(userLogin, tempNamespaceName))
        }
        // Prepare BotConfigurations (contains API Key) - bot collection on tock_bot
        val targetBotConfigurationsList = BotAdminService.getBots(targetNamespace, targetAppName)
        for (botConfiguration in targetBotConfigurationsList) {
            BotAdminService.save(
                botConfiguration.copy(
                    name = tempAppName,
                    botId = tempAppName,
                    namespace = tempNamespaceName,
                ),
            )
        }
        // Prepare ApplicationDefinition - application_definition collection on tock_front
        val targetNlpAppDef = front.getApplicationByNamespaceAndName(targetNamespace, targetAppName)
        if (targetNlpAppDef != null) {
            val tempNlpAppDef =
                targetNlpAppDef.copy(
                    _id = newId(),
                    name = tempAppName,
                    label = targetNlpAppDef.label.plus(TEMP_POSTFIX),
                    namespace = tempNamespaceName,
                    // Should be updated by synchronization process
                    intents = setOf(),
                )
            // delete temp application if exists
            front.getApplicationByNamespaceAndName(tempNamespaceName, tempAppName)?.let {
                front.deleteApplicationById(it._id)
            }

            front.save(tempNlpAppDef)
        }
        // Prepare BotApplicationConfigurations (contains path) - bot_configuration collection on tock_bot
        val targetBotApplicationConfigurationList =
            BotAdminService.getBotConfigurationsByNamespaceAndBotId(targetNamespace, targetAppName)
        for (targetBotAppConfig in targetBotApplicationConfigurationList) {
            val tempApplicationId = targetBotAppConfig.applicationId.plus(TEMP_POSTFIX)
            val tempBotAppConfig =
                targetBotAppConfig.copy(
                    applicationId = tempApplicationId,
                    botId = tempAppName,
                    namespace = tempNamespaceName,
                    path = targetBotAppConfig.path!!.removeSuffix("/").plus("$TEMP_POSTFIX/"),
                    _id = newId(),
                    nlpModel = tempAppName,
                )
            BotAdminService.getBotConfigurationByApplicationIdAndBotId(
                tempNamespaceName,
                tempApplicationId,
                tempAppName,
            )?.let {
                BotAdminService.deleteApplicationConfiguration(it)
            }

            BotAdminService.saveApplicationConfiguration(tempBotAppConfig)
            botApplicationConfigurationFitMap[targetBotAppConfig] = tempBotAppConfig
        }
        migrateData(
            targetNamespace,
            targetAppName,
            targetNlpAppDef!!._id,
            tempNamespaceName,
            tempAppName,
            withInboxMessages,
        )
        // Exchange paths for blue/green deployment before the main copy process ends
        val updatedBotConfigsMap = mutableMapOf<BotApplicationConfiguration, BotApplicationConfiguration>()
        botApplicationConfigurationFitMap.keys.forEach {
            val result = exchangePaths(it, botApplicationConfigurationFitMap[it]!!)
            updatedBotConfigsMap[result.first] = result.second
        }
        return updatedBotConfigsMap
    }

    private fun cleanTempNamespaceAndApplication(
        tempNamespace: String,
        tempAppName: String,
        userLogin: String,
        botAppConfMap: Map<BotApplicationConfiguration, BotApplicationConfiguration>,
    ) {
        // restore the original paths
        botAppConfMap.keys.forEach {
            exchangePaths(botAppConfMap[it]!!, it)
        }
        // delete the temp bot configuration
        val tempAppDef = front.getApplicationByNamespaceAndName(tempNamespace, tempAppName)
        if (tempAppDef != null) {
            front.deleteApplicationById(tempAppDef._id)
            BotAdminService.deleteApplication(tempAppDef)
        }
        // delete the temp namespace
        front.deleteNamespace(userLogin, tempNamespace)
    }

    /**
     * Since the path is a unique index in the bot_configuration collection, we must temporarily replace one of the
     * values with a non-conflicting one.
     */
    private fun exchangePaths(
        first: BotApplicationConfiguration,
        second: BotApplicationConfiguration,
    ): Pair<BotApplicationConfiguration, BotApplicationConfiguration> {
        val firstPath = first.path!!
        val nonConflictingFirstPath = first.path!!.removeSuffix(TEMP_POSTFIX).removeSuffix("/").plus("_")
        val secondPath = second.path!!.removeSuffix("/").removeSuffix("_")

        botConfigurationDAO.getConfigurationByPath(nonConflictingFirstPath)
            .ifNull { BotAdminService.saveApplicationConfiguration(first.copy(path = nonConflictingFirstPath)) }
        botConfigurationDAO.getConfigurationByPath(firstPath)
            .ifNull { BotAdminService.saveApplicationConfiguration(second.copy(path = firstPath)) }
        botConfigurationDAO.getConfigurationByPath(secondPath)
            .ifNull { BotAdminService.saveApplicationConfiguration(first.copy(path = secondPath)) }
        val firstUpdated = BotAdminService.getBotConfigurationById(first._id)
        val secondUpdated = BotAdminService.getBotConfigurationById(second._id)
        return firstUpdated!! to secondUpdated!!
    }

    private fun deleteStories(
        namespace: String,
        applicationName: String,
    ) {
        storyDefinitionDAO.deleteByNamespaceAndBotId(namespace, applicationName)
    }
}
