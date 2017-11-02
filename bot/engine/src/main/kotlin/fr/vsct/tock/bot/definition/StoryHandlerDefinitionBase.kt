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

package fr.vsct.tock.bot.definition

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.BotBus
import mu.KotlinLogging

/**
 * Base implementation of [StoryHandlerDefinition].
 */
abstract class StoryHandlerDefinitionBase<out T : ConnectorStoryHandler<*>>(
        val bus: BotBus)
    : BotBus by bus, StoryHandlerDefinition {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Shortcut for [BotBus.targetConnectorType].
     */
    val connectorType: ConnectorType = bus.targetConnectorType

    /**
     * Method to override in order to provide [ConnectorStoryHandler].
     */
    open fun provideConnector(connectorType: ConnectorType): T? = null

    /**
     * Provides the current [ConnectorStoryHandler] using [provideConnector].
     */
    override val connector: T?
        get() = provideConnector(connectorType)
                .also { if (it == null) logger.warn { "unsupported $connectorType for ${this::class}" } }

}