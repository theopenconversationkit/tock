package fr.vsct.tock.nlp.front.shared.build

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

class ModelBuildTrigger_Serializer : StdSerializer<ModelBuildTrigger>(ModelBuildTrigger::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(this)

    override fun serialize(
            value: ModelBuildTrigger,
            gen: JsonGenerator,
            serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("all")
        val _all_ = value.all
        gen.writeBoolean(_all_)
        gen.writeFieldName("onlyIfModelNotExists")
        val _onlyIfModelNotExists_ = value.onlyIfModelNotExists
        gen.writeBoolean(_onlyIfModelNotExists_)
        gen.writeEndObject()
    }
}
