package ai.tock.nlp.front.shared.config

import java.util.Locale
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ApplicationDefinition_Serializer : StdSerializer<ApplicationDefinition>(ApplicationDefinition::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ApplicationDefinition::class.java, this)

  override fun serialize(
    `value`: ApplicationDefinition,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("name")
    val _name_ = value.name
    gen.writeString(_name_)
    gen.writeName("label")
    val _label_ = value.label
    gen.writeString(_label_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("intents")
    val _intents_ = value.intents
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(Id::class.javaObjectType)), true).serialize(_intents_, gen, serializers)
    gen.writeName("supportedLocales")
    val _supportedLocales_ = value.supportedLocales
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(Locale::class.javaObjectType)), true).serialize(_supportedLocales_, gen, serializers)
    gen.writeName("intentStatesMap")
    val _intentStatesMap_ = value.intentStatesMap
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(Id::class.javaObjectType), serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(String::class.javaObjectType))), true).serialize(_intentStatesMap_, gen, serializers)
    gen.writeName("nlpEngineType")
    val _nlpEngineType_ = value.nlpEngineType
    serializers.writeValue(gen, _nlpEngineType_)
    gen.writeName("mergeEngineTypes")
    val _mergeEngineTypes_ = value.mergeEngineTypes
    gen.writeBoolean(_mergeEngineTypes_)
    gen.writeName("useEntityModels")
    val _useEntityModels_ = value.useEntityModels
    gen.writeBoolean(_useEntityModels_)
    gen.writeName("supportSubEntities")
    val _supportSubEntities_ = value.supportSubEntities
    gen.writeBoolean(_supportSubEntities_)
    gen.writeName("unknownIntentThreshold")
    val _unknownIntentThreshold_ = value.unknownIntentThreshold
    gen.writeNumber(_unknownIntentThreshold_)
    gen.writeName("knownIntentThreshold")
    val _knownIntentThreshold_ = value.knownIntentThreshold
    gen.writeNumber(_knownIntentThreshold_)
    gen.writeName("normalizeText")
    val _normalizeText_ = value.normalizeText
    gen.writeBoolean(_normalizeText_)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeEndObject()
  }
}
