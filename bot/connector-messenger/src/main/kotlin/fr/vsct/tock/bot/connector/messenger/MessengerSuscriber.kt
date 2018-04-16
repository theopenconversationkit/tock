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

package fr.vsct.tock.bot.connector.messenger

import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorConfigurationRepository
import java.util.Properties


/**
 * Adds a messenger using a property file in the classpath.
 */
fun BotDefinition.addMessengerConnector(propertyFileName: String = "/messenger.properties") {
    Properties().apply {
        load(MessengerConnector::class.java.getResourceAsStream(propertyFileName))
        addMessengerConnector(
            getProperty("pageId"),
            getProperty("pageToken"),
            getProperty("applicationSecret"),
            getProperty("verifyToken"),
            getProperty("path") ?: "/messenger",
            getProperty("baseUrl")
        )
    }
}

/**
 * Adds a messenger connector with the specified params.
 */
fun BotDefinition.addMessengerConnector(
    /**
     * The facebook page id.
     */
    pageId: String,
    /**
     * The messenger page token.
     */
    pageToken: String,
    /**
     * The messenger application secret key.
     */
    applicationSecret: String,
    /**
     * The webhook verify token.
     */
    webhookVerifyToken: String? = null,
    /**
     * The relative connector path.
     */
    path: String = "/messenger",
    /**
     * The base url for the connector path.
     */
    baseUrl: String? = null
) {
    addMessengerConnector(
        pageId,
        pageToken,
        applicationSecret,
        webhookVerifyToken,
        pageId,
        path,
        nlpModelName,
        baseUrl
    )
}

/**
 * Adds a messenger connector.
 */
@Deprecated("use addMessengerConnector with botDefinition receiver")
fun addMessengerConnector(
    /**
     * The facebook page id.
     */
    pageId: String,
    /**
     * The messenger page token.
     */
    pageToken: String,
    /**
     * The messenger application secret key.
     */
    applicationSecret: String,
    /**
     * The webhook verify token.
     */
    webhookVerifyToken: String? = null,
    /**
     * Application id has to be different for each facebook page served by the bot.
     */
    applicationId: String = pageId,
    /**
     * The relative connector path.
     */
    path: String = "/messenger",
    /**
     * The name of the application.
     */
    name: String = applicationId,
    /**
     * The base url for the connector path.
     */
    baseUrl: String? = null
) {

    ConnectorConfigurationRepository.addConfiguration(
        MessengerConnectorProvider.newConfiguration(
            pageId,
            pageToken,
            applicationSecret,
            webhookVerifyToken,
            applicationId,
            path,
            name,
            baseUrl
        )
    )
    BotRepository.registerConnectorProvider(MessengerConnectorProvider)
}