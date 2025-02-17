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