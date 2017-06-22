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

package fr.vsct.tock.bot.jackson

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.event.EventType
import fr.vsct.tock.bot.engine.message.Attachment
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Location
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.mongoJacksonModules

/**
 *
 */
object BotEngineJacksonConfiguration {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "eventType")
    private interface MixinMessage

    @Volatile
    private var module: SimpleModule? = null

    fun init() {
        if (module == null) {
            val module = SimpleModule()
            this.module = module
            with(module) {
                setMixInAnnotation(Message::class.java, MixinMessage::class.java)
                registerSubtypes(NamedType(Attachment::class.java, EventType.attachment.name))
                registerSubtypes(NamedType(Sentence::class.java, EventType.sentence.name))
                registerSubtypes(NamedType(Choice::class.java, EventType.choice.name))
                registerSubtypes(NamedType(Location::class.java, EventType.location.name))
                mapper.registerModule(this)
            }
            mongoJacksonModules.add(module)
        }
    }
}