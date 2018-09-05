package fr.vsct.tock.nlp.front.shared.build

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

class ModelBuild_Serializer : StdSerializer<ModelBuild>(ModelBuild::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(this)

    override fun serialize(
            value: ModelBuild,
            gen: JsonGenerator,
            serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("language")
        val _language_ = value.language
        serializers.defaultSerializeValue(_language_, gen)
        gen.writeFieldName("type")
        val _type_ = value.type
        serializers.defaultSerializeValue(_type_, gen)
        gen.writeFieldName("intentId")
        val _intentId_ = value.intentId
        if(_intentId_ == null) { gen.writeNull() } else {serializers.defaultSerializeValue(_intentId_, gen)}
        gen.writeFieldName("entityTypeName")
        val _entityTypeName_ = value.entityTypeName
        if(_entityTypeName_ == null) { gen.writeNull() } else {gen.writeString(_entityTypeName_)}
        gen.writeFieldName("nbSentences")
        val _nbSentences_ = value.nbSentences
        gen.writeNumber(_nbSentences_)
        gen.writeFieldName("duration")
        val _duration_ = value.duration
        serializers.defaultSerializeValue(_duration_, gen)
        gen.writeFieldName("error")
        val _error_ = value.error
        gen.writeBoolean(_error_)
        gen.writeFieldName("errorMessage")
        val _errorMessage_ = value.errorMessage
        if(_errorMessage_ == null) { gen.writeNull() } else {gen.writeString(_errorMessage_)}
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
