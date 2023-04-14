/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin

import ai.tock.bot.admin.constants.Properties
import ai.tock.bot.admin.model.BotAdminConfiguration
import ai.tock.bot.admin.service.BotAdminService
import ai.tock.bot.admin.service.FaqAdminService
import ai.tock.bot.admin.test.findTestService
import ai.tock.bot.admin.verticle.ActionVerticle
import ai.tock.bot.admin.verticle.AnalyticsVerticle
import ai.tock.bot.admin.verticle.ApplicationVerticle
import ai.tock.bot.admin.verticle.BotVerticle
import ai.tock.bot.admin.verticle.ChildVerticle
import ai.tock.bot.admin.verticle.ConfigurationVerticle
import ai.tock.bot.admin.verticle.ConnectorVerticle
import ai.tock.bot.admin.verticle.DialogVerticle
import ai.tock.bot.admin.verticle.FaqVerticle
import ai.tock.bot.admin.verticle.FeatureVerticle
import ai.tock.bot.admin.verticle.FileVerticle
import ai.tock.bot.admin.verticle.FlowVerticle
import ai.tock.bot.admin.verticle.I18nVerticle
import ai.tock.bot.admin.verticle.ParentVerticle
import ai.tock.bot.admin.verticle.StoryVerticle
import ai.tock.bot.admin.verticle.UserVerticle
import ai.tock.bot.engine.dialog.DialogFlowDAO
import ai.tock.nlp.admin.AdminVerticle
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.booleanProperty
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.injector
import ai.tock.shared.vertx.ServerStatus
import ai.tock.translator.I18nDAO
import ai.tock.translator.Translator.initTranslator
import com.github.salomonbrys.kodein.instance
import mu.KLogger
import mu.KotlinLogging

/**
 *
 */
open class BotAdminVerticle : AdminVerticle(), ParentVerticle<AdminException> {

    private val botAdminConfiguration = BotAdminConfiguration()

    override val logger: KLogger = KotlinLogging.logger {}

    private val i18n: I18nDAO by injector.instance()

    private val dialogFlowDAO: DialogFlowDAO by injector.instance()

    override val supportCreateNamespace: Boolean = !botAdminConfiguration.botApiSupport

    override fun protectedPaths(): Set<String> = setOf(rootPath)

    override fun configureServices() {
        vertx.eventBus().consumer<Boolean>(ServerStatus.SERVER_STARTED) {
            if (it.body() && booleanProperty(Properties.FAQ_MIGRATION_ENABLED, false)) {
                FaqAdminService.makeMigration()
            }
        }
        initTranslator()
        dialogFlowDAO.initFlowStatCrawl()
        super.configureServices()
    }

    override fun children(): List<ChildVerticle<AdminException>> {
        return listOf(
            AnalyticsVerticle(),
            StoryVerticle(),
            FaqVerticle(),
            UserVerticle(),
            DialogVerticle(),
            ConfigurationVerticle(botAdminConfiguration),
            FeatureVerticle(),
            ActionVerticle(),
            FlowVerticle(),
            I18nVerticle(i18n),
            BotVerticle(),
            ApplicationVerticle(),
            ConnectorVerticle(),
            FileVerticle()
        )
    }

    override fun configure() {
        configureServices()

        preConfigure()

        findTestService().registerServices().invoke(this)

        configureStaticHandling()
    }

    override fun deleteApplication(app: ApplicationDefinition) {
        super.deleteApplication(app)
        BotAdminService.deleteApplication(app)
    }

    override fun saveApplication(
        existingApp: ApplicationDefinition?,
        app: ApplicationDefinition
    ): ApplicationDefinition {
        if (existingApp != null && existingApp.name != app.name) {
            BotAdminService.changeApplicationName(existingApp, app)
        }
        if (app.supportedLocales != existingApp?.supportedLocales) {
            BotAdminService.changeSupportedLocales(app)
        }
        return super.saveApplication(existingApp, app)
    }

}
