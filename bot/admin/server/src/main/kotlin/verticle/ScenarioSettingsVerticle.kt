/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin.verticle

import ai.tock.bot.admin.service.ScenarioSettingsService
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ScenarioSettingsQuery
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.RoutingContext

class ScenarioSettingsVerticle {

    companion object {
        const val PATH_PARAM_APPLICATION_ID = "applicationId"
        const val PATH = "/scenarios/settings/:$PATH_PARAM_APPLICATION_ID"
    }

    private val front = FrontClient

    fun configure(webVerticle: WebVerticle) {

        val authorizedRoles = setOf(TockUserRole.botUser, TockUserRole.admin, TockUserRole.technicalAdmin)

        with(webVerticle) {

            val handlePost = { context: RoutingContext, query: ScenarioSettingsQuery ->
                val applicationDefinition = front.getApplicationById(context.pathId(PATH_PARAM_APPLICATION_ID))
                if (context.organization == applicationDefinition?.namespace) {
                    ScenarioSettingsService.save(applicationDefinition, query)
                } else {
                    WebVerticle.unauthorized()
                }
            }

            val handleGet = { context: RoutingContext ->
                val id = context.pathId<ApplicationDefinition>(PATH_PARAM_APPLICATION_ID)
                val applicationDefinition = front.getApplicationById(id)
                if (context.organization == applicationDefinition?.namespace) {
                    ScenarioSettingsService.getScenarioSettingsByApplicationId(id.toString())?.toScenarioSettingsQuery()
                } else {
                    WebVerticle.unauthorized()
                }
            }

            blockingJsonPost(PATH, authorizedRoles, handler = handlePost)

            blockingJsonGet(PATH, authorizedRoles, handler = handleGet)
        }
    }

}