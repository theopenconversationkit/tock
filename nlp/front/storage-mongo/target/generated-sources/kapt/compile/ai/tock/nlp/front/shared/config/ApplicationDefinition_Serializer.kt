package ai.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ApplicationDefinition_Serializer :
        StdSerializer<ApplicationDefinition>(ApplicationDefinition::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ApplicationDefinition::class.java, this)

    override fun serialize(
        value: ApplicationDefinition,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("label")
        val _label_ = value.label
        gen.writeString(_label_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("intents")
        val _intents_ = value.intents
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(org.litote.kmongo.Id::class.java)
                ),
                true,
                null
                )
                .serialize(_intents_, gen, serializers)
        gen.writeFieldName("supportedLocales")
        val _supportedLocales_ = value.supportedLocales
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(java.util.Locale::class.java)
                ),
                true,
                null
                )
                .serialize(_supportedLocales_, gen, serializers)
        gen.writeFieldName("intentStatesMap")
        val _intentStatesMap_ = value.intentStatesMap
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(org.litote.kmongo.Id::class.java),
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                )
                ),
                true,
                null
                )
                .serialize(_intentStatesMap_, gen, serializers)
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
        gen.writeFieldName("unknownIntentThreshold")
        val _unknownIntentThreshold_ = value.unknownIntentThreshold
        gen.writeNumber(_unknownIntentThreshold_)
        gen.writeFieldName("knownIntentThreshold")
        val _knownIntentThreshold_ = value.knownIntentThreshold
        gen.writeNumber(_knownIntentThreshold_)
        gen.writeFieldName("normalizeText")
        val _normalizeText_ = value.normalizeText
        gen.writeBoolean(_normalizeText_)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
