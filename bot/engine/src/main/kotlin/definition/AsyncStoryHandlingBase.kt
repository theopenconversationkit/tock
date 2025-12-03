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
import ai.tock.bot.engine.AsyncBus
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.github.salomonbrys.kodein.KodeinInjector
import mu.KotlinLogging
import kotlin.LazyThreadSafetyMode.PUBLICATION

@ExperimentalTockCoroutines
abstract class AsyncStoryHandlingBase<out T : AsyncConnectorHandling<AsyncStoryHandlingBase<T>>>(val bus: AsyncBus) : AsyncStoryHandling, AsyncBus by bus {
    companion object {
        private val logger = KotlinLogging.logger {}

        private val connectorProvider: ConnectorHandlerProvider
            get() =
                try {
                    injector.provide()
                } catch (_: KodeinInjector.UninjectedException) {
                    DefaultConnectorHandlerProvider
                }
    }

    /**
     * The method to implement if there is no [StoryStep] in the [StoryDefinition]
     * or when current [StoryStep] is null
     */
    protected abstract suspend fun answer()

    /**
     * Default implementation redirect to answer.
     */
    override suspend fun handle() {
        answer()
    }

    /**
     * Shortcut for [AsyncBus.targetConnectorType].
     */
    val connectorType: ConnectorType get() = bus.targetConnectorType

    /**
     * Method to override in order to provide [ConnectorStoryHandler].
     * Default implementation use annotations annotated with @[ConnectorHandler].
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun findConnector(connectorType: ConnectorType): T? = connectorProvider.provide(this, connectorType) as? T?

    /**
     * Method to override in order to provide [ConnectorStoryHandler].
     * Default implementation use annotations annotated with @[ConnectorIdHandlers].
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun findConnector(connectorId: String): T? = connectorProvider.provide(this, connectorId) as? T?

    /**
     * Provides the current [ConnectorStoryHandler] using [findConnector].
     */
    val connector: T? by lazy(PUBLICATION) {
        (findConnector(connectorId) ?: findConnector(connectorType))
            .also { if (it == null) logger.warn { "unsupported connector type $connectorType/$connectorId for ${this::class}" } }
    }

    /**
     * Provides a not null [connector]. Throws NPE if [connector] is null.
     */
    val c: T
        get() = connector!!
}
