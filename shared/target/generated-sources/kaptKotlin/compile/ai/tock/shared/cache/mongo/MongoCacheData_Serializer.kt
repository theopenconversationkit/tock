package ai.tock.shared.cache.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class MongoCacheData_Serializer : StdSerializer<MongoCacheData>(MongoCacheData::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(MongoCacheData::class.java, this)

  override fun serialize(
    `value`: MongoCacheData,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("id")
    val _id_ = value.id
    serializers.writeValue(gen, _id_)
    gen.writeName("type")
    val _type_ = value.type
    gen.writeString(_type_)
    gen.writeName("s")
    val _s_ = value.s
    if (_s_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_s_)
    }
    gen.writeName("b")
    val _b_ = value.b
    if (_b_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _b_)
    }
    gen.writeName("a")
    val _a_ = value.a
    if (_a_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _a_)
    }
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
