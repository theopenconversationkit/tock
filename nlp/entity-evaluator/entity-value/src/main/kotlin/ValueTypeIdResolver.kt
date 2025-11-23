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

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import kotlin.reflect.KClass

/**
 * The [TypeIdResolver] for any [Value] type.
 */
class ValueTypeIdResolver : TypeIdResolverBase() {
    override fun typeFromId(
        context: DatabindContext,
        id: String,
    ): JavaType {
        return context.config.constructType(ValueResolverRepository.getType(id).java)
    }

    override fun idFromValue(value: Any): String {
        @Suppress("UNCHECKED_CAST")
        return ValueResolverRepository.getId(value::class as KClass<Value>)
    }

    override fun idFromValueAndType(
        value: Any,
        suggestedType: Class<*>,
    ): String {
        return idFromValue(value)
    }

    override fun getMechanism(): JsonTypeInfo.Id {
        return JsonTypeInfo.Id.NAME
    }
}
