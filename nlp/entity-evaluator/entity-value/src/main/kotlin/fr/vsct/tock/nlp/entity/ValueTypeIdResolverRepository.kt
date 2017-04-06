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
import kotlin.reflect.KClass

/**
 *
 */
object ValueTypeIdResolverRepository {

    private val resolverMap = ConcurrentHashMap<String, KClass<out Value>>()

    /**
     * Init default mappings.
     */
    fun initDefault() {
        registerType(DateEntityValue::class)
        registerType(DateIntervalEntityValue::class)
        registerType(NumberValue::class)
        registerType(OrdinalValue::class)
        registerType(DistanceValue::class)
        registerType(TemperatureValue::class)
        registerType(VolumeValue::class)
        registerType(AmountOfMoneyValue::class)
        registerType(EmailValue::class)
        registerType(UrlValue::class)
        registerType(PhoneNumberValue::class)
    }

    fun <T : Value> registerType(kClass: KClass<T>) {
        resolverMap.put(kClass.simpleName!!, kClass)
    }

    fun getType(id: String): KClass<out Value> {
        return resolverMap.getValue(id)
    }
}