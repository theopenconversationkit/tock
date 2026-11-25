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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorHandler
import ai.tock.bot.connector.ConnectorIdHandlers
import ai.tock.bot.connector.ConnectorType
import ai.tock.shared.coroutines.ExperimentalTockCoroutines

/**
 * Provides [ConnectorHandler].
 */
interface ConnectorHandlerProvider {
    /**
     * Method to override in order to provide [ConnectorStoryHandler] from [ConnectorType].
     * Default implementation use annotations annotated with @[ConnectorHandler].
     */
    fun provide(
        storyDef: StoryHandlerDefinition,
        connectorType: ConnectorType,
    ): ConnectorStoryHandlerBase<*>? = null

    /**
     * Method to override in order to provide [ConnectorStoryHandler] from connectorId.
     * Default implementation use annotations annotated with @[ConnectorIdHandlers].
     */
    fun provide(
        storyDef: StoryHandlerDefinition,
        connectorId: String,
    ): ConnectorStoryHandlerBase<*>? = null

    /**
     * Method to override in order to provide [ConnectorStoryHandler] from [ConnectorType].
     * Default implementation use annotations annotated with @[ConnectorHandler].
     */
    @ExperimentalTockCoroutines
    fun provide(
        storyDef: AsyncStoryHandling,
        connectorType: ConnectorType,
    ): AsyncConnectorHandlingBase<*>? = null

    /**
     * Method to override in order to provide [ConnectorStoryHandler] from connectorId.
     * Default implementation use annotations annotated with @[ConnectorIdHandlers].
     */
    @ExperimentalTockCoroutines
    fun provide(
        storyDef: AsyncStoryHandling,
        connectorId: String,
    ): AsyncConnectorHandlingBase<*>? = null
}
