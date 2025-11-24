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

package ai.tock.shared.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.serializer
import mu.KotlinLogging
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal val json =
    Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        serializersModule =
            SerializersModule {
                contextual(AnySerializer())
            }
    }

class AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Any")

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    override fun serialize(
        encoder: Encoder,
        value: Any,
    ) {
        when (value) {
            is List<*> -> {
                val listSerializer = ListSerializer(AnySerializer())
                encoder.encodeSerializableValue(listSerializer as SerializationStrategy<Any>, value)
            }

            is Set<*> -> {
                val setSerializer = SetSerializer(AnySerializer())
                encoder.encodeSerializableValue(setSerializer as SerializationStrategy<Any>, value)
            }

            else -> {
                encoder.encodeSerializableValue(value::class.serializer() as SerializationStrategy<Any>, value)
            }
        }
    }

    override fun deserialize(decoder: Decoder): Any =
        error(
            "Deserialization is not supported for AnySerializer - this serializer is for encoding only",
        )
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> readJson(input: InputStream): T = json.decodeFromStream<T>(input)

inline fun <reified T : Any> readJson(content: String): T = json.decodeFromString(content)

inline fun <reified T> loadResource(path: String): T? =
    object {}::class.java.getResourceAsStream(path)?.let { stream ->
        readJson(stream)
    } ?: null.apply { KotlinLogging.logger {}.warn { "resource not found: $path" } }

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> writeJson(
    content: T,
    output: OutputStream,
) = json.encodeToStream(content, output)

inline fun <reified T : Any> writeJson(content: T): String = json.encodeToString(content)
