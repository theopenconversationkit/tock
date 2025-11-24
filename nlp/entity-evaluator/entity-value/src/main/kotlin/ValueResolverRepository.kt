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

package ai.tock.nlp.entity

import ai.tock.nlp.entity.date.DateEntityValue
import ai.tock.nlp.entity.date.DateIntervalEntityValue
import ai.tock.nlp.entity.temperature.TemperatureValue
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass

/**
 * Repository used to register custom [Value] serializer/deserializer.
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
            UnknownValue::class,
            StringValue::class,
            DurationValue::class,
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
            .replaceFirstChar { it.lowercase(Locale.getDefault()) }
            .apply {
                idClassMap.put(this, kClass)
                classIdMap.put(kClass, this)
            }
    }

    internal fun getType(id: String): KClass<out Value> {
        return idClassMap[id] ?: try {
            @Suppress("UNCHECKED_CAST")
            (Class.forName(id) as Class<out Value>).kotlin
        } catch (e: ClassNotFoundException) {
            val className = ValueResolverRepository::class.qualifiedName
            Logger.getLogger(className).logrb(
                Level.FINE,
                className,
                className,
                null as ResourceBundle?,
                "${e.message}: Please call ValueResolverRepository#registerType during app initialization for value of type $id",
                e,
            )

            UnknownValue::class
        }
    }

    internal fun <T : Value> getId(kClass: KClass<T>): String {
        return classIdMap[kClass] ?: kClass.qualifiedName!!
    }
}
