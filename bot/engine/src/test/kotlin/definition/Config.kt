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

import ai.tock.bot.engine.BotBus
import ai.tock.nlp.entity.date.DateEntityRange
import java.time.ZonedDateTime

class SimpleDef(bus: BotBus) : HandlerDef<Connector>(bus)

data class StoryData(
    val entityValue: String? = null,
    val departureDate: ZonedDateTime? = null
)

class Def(bus: BotBus, val data: StoryData) : HandlerDef<Connector>(bus) {
    override fun answer() {
        when {
            data.entityValue == null -> askToFillValue()
            data.departureDate == null -> askForDepartureDate(data.entityValue)
            else -> askMain()
        }
    }

    fun askToFillValue() {
        end("please fill Data")
    }

    fun askForDepartureDate(entity: String) {
        end("please fill departure date for {0}", entity)
    }

    fun askMain() {
        end("ok")
    }
}

class Connector(context: Def) : ConnectorDef<Def>(context)

data class StoryData2(
    val entityValue: String?,
    val departureDate: ZonedDateTime?
)

class Def2(bus: BotBus, val data: StoryData) : HandlerDef<Connector>(bus) {
    override fun answer() {
        when {
            data.entityValue == null -> askToFillValue()
            data.departureDate == null -> askForDepartureDate(data.entityValue)
            else -> askMain()
        }
    }

    fun askToFillValue() {
        end("please fill Data")
    }

    fun askForDepartureDate(entity: String) {
        end("please fill departure date for {0}", entity)
    }

    fun askMain() {
        end("ok")
    }
}

class Connector2(context: Def2) : ConnectorDef<Def2>(context)

enum class Step2 : StoryDataStep<Def2, StoryData, StoryData2> {
    s1 {

        override fun checkPreconditions(): BotBus.(StoryData?) -> StoryData2? = {
            changeContextValue("myValue", entityText("entity"))

            StoryData2(
                entityText("entity"),
                entityValue<DateEntityRange>("date")?.start()
            )
        }

        override fun handler(): Def2.(StoryData2?) -> Any? = { data: StoryData2? ->
            changeContextValue("myValue", data?.entityValue)

            end("at {0}?", data?.departureDate)
        }
    },

    s2
}

object Step3 : StoryDataStepBase<Def2, StoryData, EmptyData>(reply = { "ok" })
