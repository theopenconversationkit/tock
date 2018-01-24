/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.alexa

import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorConfigurationRepository
import kotlin.reflect.KClass

/**
 *
 */
/**
 * Add a alexa connector.
 */
fun addAlexaConnector(
        /**
         * Application id have to be different for each alexa app served by the bot.
         */
        applicationId: String = "app",
        /**
         * The relative connector path.
         */
        path: String = "/alexa/$applicationId",
        /**
         * The mapper class used to transform alexa intent/slot in tock intent/entity
         */
        alexaTockMapper: KClass<out AlexaTockMapper> = AlexaTockMapper::class,
        /**
         * The supported alexa application ids..
         */
        supportedAlexaApplicationIds: Set<String> = setOf(applicationId)
) {

    ConnectorConfigurationRepository.addConfiguration(
            AlexaConnectorProvider.newConfiguration(
                    applicationId,
                    path,
                    alexaTockMapper,
                    supportedAlexaApplicationIds)
    )
    BotRepository.registerConnectorProvider(AlexaConnectorProvider)
}