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
package ai.tock.shared.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator

/**
 * Jackson 2 support of [Id] for the Tock [mapper]: `kmongo-dsl-id-jackson` uses Jackson 3.
 */
internal class Jackson2IdModule : SimpleModule() {
    init {
        addSerializer(
            Id::class.java,
            object : JsonSerializer<Id<*>>() {
                override fun serialize(
                    value: Id<*>,
                    gen: JsonGenerator,
                    serializers: SerializerProvider,
                ) = gen.writeString(value.toString())
            },
        )
        addDeserializer(
            Id::class.java,
            object : JsonDeserializer<Id<*>>() {
                override fun deserialize(
                    p: JsonParser,
                    ctxt: DeserializationContext,
                ): Id<*> = IdGenerator.defaultGenerator.create(p.text)
            },
        )
        addKeySerializer(
            Id::class.java,
            object : JsonSerializer<Id<*>>() {
                override fun serialize(
                    value: Id<*>,
                    gen: JsonGenerator,
                    serializers: SerializerProvider,
                ) = gen.writeFieldName(value.toString())
            },
        )
        addKeyDeserializer(
            Id::class.java,
            object : KeyDeserializer() {
                override fun deserializeKey(
                    key: String,
                    ctxt: DeserializationContext,
                ): Any = IdGenerator.defaultGenerator.create(key)
            },
        )
    }
}
