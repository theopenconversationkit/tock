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

package ai.tock.bot.test.mock

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.ConnectorDef
import ai.tock.bot.definition.ConnectorHandlerProvider
import ai.tock.bot.definition.ConnectorStoryHandlerBase
import ai.tock.bot.definition.HandlerDef
import ai.tock.bot.definition.StoryDefinitionBase
import ai.tock.bot.definition.StoryHandlerBase
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.BotBus
import ai.tock.shared.injector
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.mockk

/**
 * Runs [StoryDefinitionBase.checkPreconditions].
 */
fun StoryDefinitionBase.checkPreconditions(bus: BotBus) = (storyHandler as StoryHandlerBase<*>).checkPreconditions()(bus)

/**
 * Runs the select step from Bus phase.
 */
fun StoryDefinitionBase.selectStepFromData(
    def: HandlerDef<*>,
    data: Any?,
): StoryStep<*>? = (storyHandler as StoryHandlerBase<*>).selectStepFromStoryHandlerAndData(def, data, this)

/**
 * Provides a mock of [ConnectorDef] and run the test block.
 */
inline fun <reified T : ConnectorDef<*>> mockConnector(
    connector: T = mockk(relaxed = true),
    bus: BotBus = mockk(relaxed = true),
    test: (BotBus) -> Any?,
): T {
    try {
        tockInternalInjector = KodeinInjector()
        injector.inject(
            Kodein {
                bind<ConnectorHandlerProvider>() with provider { MyProvider(connector) }
            },
        )
        test(bus)
    } finally {
        tockInternalInjector = KodeinInjector()
    }
    return connector
}

@PublishedApi
internal class MyProvider<T : ConnectorDef<*>>(private val connector: T) : ConnectorHandlerProvider {
    override fun provide(
        storyDef: StoryHandlerDefinition,
        connectorType: ConnectorType,
    ): ConnectorStoryHandlerBase<*> = connector

    override fun provide(
        storyDef: StoryHandlerDefinition,
        connectorId: String,
    ): ConnectorStoryHandlerBase<*> = connector
}
