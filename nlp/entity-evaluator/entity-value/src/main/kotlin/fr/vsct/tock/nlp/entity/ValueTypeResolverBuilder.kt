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

import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder

/**
 *
 */
internal class ValueTypeResolverBuilder : StdTypeResolverBuilder() {

    class ValueTypeIdResolver(val typeIdResolver: TypeIdResolver) : TypeIdResolver by typeIdResolver {
        override fun typeFromId(context: DatabindContext, id: String): JavaType {
            return context.constructType(ValueTypeIdResolverRepository.getType(id).java)
        }
    }

    override fun idResolver(config: MapperConfig<*>?, baseType: JavaType?, subtypes: MutableCollection<NamedType>?, forSer: Boolean, forDeser: Boolean): TypeIdResolver {
        return ValueTypeIdResolver(super.idResolver(config, baseType, subtypes, forSer, forDeser))
    }
}