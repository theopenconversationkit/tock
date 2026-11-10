package ai.tock.nlp.core

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DictionaryData_Serializer : StdSerializer<DictionaryData>(DictionaryData::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DictionaryData::class.java, this)

  override fun serialize(
    `value`: DictionaryData,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("entityName")
    val _entityName_ = value.entityName
    gen.writeString(_entityName_)
    gen.writeName("values")
    val _values_ = value.values
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(PredefinedValue::class.javaObjectType)), true).serialize(_values_, gen, serializers)
    gen.writeName("onlyValues")
    val _onlyValues_ = value.onlyValues
    gen.writeBoolean(_onlyValues_)
    gen.writeName("minDistance")
    val _minDistance_ = value.minDistance
    gen.writeNumber(_minDistance_)
    gen.writeName("textSearch")
    val _textSearch_ = value.textSearch
    gen.writeBoolean(_textSearch_)
    gen.writeEndObject()
  }
}
