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

package fr.vsct.tock.shared.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import fr.vsct.tock.shared.jackson.AnyValueWrapper.AnyValueDeserializer
import kotlin.reflect.KClass

/**
 * A jackson wrapper to store class name with dynamic type value.
 * Use with care, as it stores the class name in json.
 */
@JsonDeserialize(using = AnyValueDeserializer::class)
data class AnyValueWrapper(val klass: Class<*>, val value: Any?) {

    internal class AnyValueDeserializer : JsonDeserializer<AnyValueWrapper>() {

        override fun deserialize(jp: JsonParser, context: DeserializationContext): AnyValueWrapper? {
            var fieldName = jp.fieldNameWithValueReady()
            if (fieldName != null) {
                val classValue = jp.readValueAs(Class::class.java)!!
                fieldName = jp.fieldNameWithValueReady()
                if (fieldName != null) {
                    val value = jp.readValueAs(classValue)!!
                    jp.checkEndToken()
                    return AnyValueWrapper(classValue, value)
                } else {
                    return AnyValueWrapper(classValue, null)
                }
            }
            return null
        }

    }

    constructor(klass: KClass<*>, value: Any?) : this(klass.java, value)

    constructor(value: Any) : this(value::class.java, value)

}


