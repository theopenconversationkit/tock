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

import ai.tock.bot.connector.ConnectorTypeConfiguration
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.RequestSucceeded
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler


class ConnectorVerticle : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            blockingJsonGet("/connectorTypes", setOf(
                TockUserRole.botUser,
                TockUserRole.faqBotUser,
                TockUserRole.faqNlpUser
            )) {
                RequestSucceeded(ConnectorTypeConfiguration.connectorConfigurations)
            }

            blockingGet("/connectorIcon/:connectorType/icon.svg", null, basePath,
                handler = toRequestHandler { context ->
                    val connectorType = context.path("connectorType")
                    context.response().putHeader("Content-Type", "image/svg+xml")
                    context.response().putHeader("Cache-Control", "max-age=84600, public")
                    ConnectorTypeConfiguration.connectorConfigurations.firstOrNull { it.connectorType.id == connectorType }?.svgIcon
                        ?: ""
                })

        }
    }

}