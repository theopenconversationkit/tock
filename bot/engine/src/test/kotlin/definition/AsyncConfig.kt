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
import ai.tock.bot.engine.AsyncBus
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import kotlin.reflect.KClass

@ExperimentalTockCoroutines
class AsyncDef(private val bus: AsyncBus) : AsyncStoryHandling {
    override suspend fun handle() {
        bus.end("Hello, World!")
    }
}

@ExperimentalTockCoroutines
class AsyncConn(ctx: AsyncDefWithData) : AsyncConnectorHandlingBase<AsyncDefWithData>(ctx) {
    suspend fun askToFillValue() {
        end {
            // Bus-specific methods can be called here
            translate("hello")
        }
    }
}

@ExperimentalTockCoroutines
@TestHandler(AsyncConn::class)
class AsyncDefWithData(bus: AsyncBus, val data: StoryData) : AsyncStoryHandlingBase<AsyncConn>(bus) {
    override suspend fun answer() {
        when {
            data.entityValue == null -> c.askToFillValue()
            data.departureDate == null -> askForDepartureDate(data.entityValue)
            else -> askMain()
        }
    }

    suspend fun askForDepartureDate(entity: String) {
        end("please fill departure date for {0}", entity)
    }

    suspend fun askMain() {
        send("message 1")
        send("message 2")
        end("ok")
    }
}

@ConnectorHandler(connectorTypeId = "NONE")
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class TestHandler(val value: KClass<out ConnectorSpecificHandling>)
