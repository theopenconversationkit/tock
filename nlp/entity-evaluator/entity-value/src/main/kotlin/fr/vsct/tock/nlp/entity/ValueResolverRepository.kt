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

import fr.vsct.tock.nlp.entity.date.DateEntityValue
import fr.vsct.tock.nlp.entity.date.DateIntervalEntityValue
import fr.vsct.tock.nlp.entity.temperature.TemperatureValue
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger
import kotlin.reflect.KClass

/**
 *
 */
object ValueResolverRepository {

    private val idClassMap = ConcurrentHashMap<String, KClass<out Value>>()
    private val classIdMap = ConcurrentHashMap<KClass<out Value>, String>()

    init {
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
                PhoneNumberValue::class,
                CustomValueWrapper::class,
                UnknownValue::class
        )
                .forEach { registerType(it) }
    }

    /**
     * Register a new [Value] type.
     *
     * @mapper the jackson mapper
     * @kClass the class to register
     */
    fun <T : Value> registerType(kClass: KClass<T>) {
        kClass.simpleName!!
                .replace("Value", "")
                .decapitalize()
                .apply {
                    idClassMap.put(this, kClass)
                    classIdMap.put(kClass, this)
                }
    }

    fun getType(id: String): KClass<out Value> {
        return idClassMap[id] ?:
                try {
                    @Suppress("UNCHECKED_CAST")
                    Class.forName(id) as KClass<out Value>
                } catch(e: ClassNotFoundException) {
                    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
                            .severe("Please call ValueResolverRepository#registerType during app initialization for value of type $id")
                    UnknownValue::class
                }
    }

    fun <T : Value> getId(kClass: KClass<T>): String {
        return classIdMap[kClass]
                ?: kClass.qualifiedName!!
    }
}