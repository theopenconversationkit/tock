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

package fr.vsct.tock.nlp.entity

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import fr.vsct.tock.nlp.entity.date.DateEntityValue
import fr.vsct.tock.nlp.entity.date.DateIntervalEntityValue
import fr.vsct.tock.nlp.entity.temperature.TemperatureValue
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 *
 */
object ValueResolverRepository {

    private val idClassMap = ConcurrentHashMap<String, KClass<out Value>>()
    private val classIdMap = ConcurrentHashMap<KClass<out Value>, String>()

    /**
     * Init default mappings.
     */
    fun initDefault(mapper: ObjectMapper) {
        mapper.subtypeResolver
        //workarounds https://github.com/FasterXML/jackson-databind/issues/1594
        mapper.copy().apply {
            listOf(
                    DateEntityValue::class,
                    DateIntervalEntityValue::class,
                    NumberValue::class,
                    OrdinalValue::class,
                    DistanceValue::class,
                    TemperatureValue::class,
                    VolumeValue::class,
                    AmountOfMoneyValue::class,
                    EmailValue::class,
                    UrlValue::class,
                    PhoneNumberValue::class)
                    .forEach { registerType(mapper, this, it) }
        }
    }

    /**
     * Register a new [Value] type.
     *
     * @mapper the jackson mapper
     * @kClass the class to register
     */
    fun <T : Value> registerType(mapper: ObjectMapper, kClass: KClass<T>) {
        registerType(
                mapper,
                mapper.copy(),
                kClass)
    }

    private fun <T : Value> registerType(
            mapper: ObjectMapper,
            mapperCopy: ObjectMapper,
            kClass: KClass<T>) {
        @Suppress("UNCHECKED_CAST")
        registerType(
                mapper,
                mapperCopy.serializerProviderInstance.findTypedValueSerializer(kClass.java, false, null) as JsonSerializer<T>,
                kClass)
    }

    private fun <T : Value> registerType(
            mapper: ObjectMapper,
            jsonSerializer: JsonSerializer<T>,
            kClass: KClass<T>) {

        //workarounds https://github.com/FasterXML/jackson-databind/issues/1594
        val typeSerializer = mapper.serializerProviderInstance.findTypeSerializer(mapper.constructType(kClass.java))
        val module = SimpleModule().addSerializer(
                kClass.java,
                @Suppress("UNCHECKED_CAST")
                object : StdSerializer<T>(kClass::class.java as Class<T>) {

                    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
                        jsonSerializer.serializeWithType(value, gen, provider, typeSerializer)
                    }

                    override fun serializeWithType(value: T, gen: JsonGenerator, serializers: SerializerProvider, typeSer: TypeSerializer) {
                        jsonSerializer.serializeWithType(value, gen, serializers, typeSer)
                    }
                })
        mapper.registerModule(module)

        kClass.simpleName!!.apply {
            idClassMap.put(this, kClass)
            classIdMap.put(kClass, this)
        }
    }

    fun getType(id: String): KClass<out Value> {
        return idClassMap.getValue(id)
    }

    fun <T : Value> getId(kClass: KClass<T>): String {
        return classIdMap.getValue(kClass)
    }
}