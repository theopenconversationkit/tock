package ai.tock.nlp.core

import java.util.Locale
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class PredefinedValue_Serializer : StdSerializer<PredefinedValue>(PredefinedValue::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(PredefinedValue::class.java, this)

  override fun serialize(
    `value`: PredefinedValue,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("value")
    val _value_ = value.`value`
    gen.writeString(_value_)
    gen.writeName("labels")
    val _labels_ = value.labels
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(Locale::class.javaObjectType), serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(String::class.javaObjectType))), true).serialize(_labels_, gen, serializers)
    gen.writeEndObject()
  }
}
