package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

class ApplicationDefinition_Serializer : StdSerializer<ApplicationDefinition>(ApplicationDefinition::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(this)

    override fun serialize(
            value: ApplicationDefinition,
            gen: JsonGenerator,
            serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("intents")
        val _intents_ = value.intents
        serializers.defaultSerializeValue(_intents_, gen)
        gen.writeFieldName("supportedLocales")
        val _supportedLocales_ = value.supportedLocales
        serializers.defaultSerializeValue(_supportedLocales_, gen)
        gen.writeFieldName("intentStatesMap")
        val _intentStatesMap_ = value.intentStatesMap
        serializers.defaultSerializeValue(_intentStatesMap_, gen)
        gen.writeFieldName("nlpEngineType")
        val _nlpEngineType_ = value.nlpEngineType
        serializers.defaultSerializeValue(_nlpEngineType_, gen)
        gen.writeFieldName("mergeEngineTypes")
        val _mergeEngineTypes_ = value.mergeEngineTypes
        gen.writeBoolean(_mergeEngineTypes_)
        gen.writeFieldName("useEntityModels")
        val _useEntityModels_ = value.useEntityModels
        gen.writeBoolean(_useEntityModels_)
        gen.writeFieldName("supportSubEntities")
        val _supportSubEntities_ = value.supportSubEntities
        gen.writeBoolean(_supportSubEntities_)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
