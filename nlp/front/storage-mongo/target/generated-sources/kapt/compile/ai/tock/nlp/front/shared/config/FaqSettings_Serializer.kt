/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class FaqSettings_Serializer : StdSerializer<FaqSettings>(FaqSettings::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(FaqSettings::class.java, this)

    override fun serialize(
        value: FaqSettings,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("satisfactionEnabled")
        val _satisfactionEnabled_ = value.satisfactionEnabled
        gen.writeBoolean(_satisfactionEnabled_)
        gen.writeFieldName("satisfactionStoryId")
        val _satisfactionStoryId_ = value.satisfactionStoryId
        if(_satisfactionStoryId_ == null) { gen.writeNull() } else {
                gen.writeString(_satisfactionStoryId_)
                }
        gen.writeFieldName("creationDate")
        val _creationDate_ = value.creationDate
        serializers.defaultSerializeValue(_creationDate_, gen)
        gen.writeFieldName("updateDate")
        val _updateDate_ = value.updateDate
        serializers.defaultSerializeValue(_updateDate_, gen)
        gen.writeEndObject()
    }
}
