package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class Feature_Serializer : StdSerializer<Feature>(Feature::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(Feature::class.java, this)

  override fun serialize(
    `value`: Feature,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    gen.writeString(__id_)
    gen.writeName("key")
    val _key_ = value.key
    gen.writeString(_key_)
    gen.writeName("enabled")
    val _enabled_ = value.enabled
    gen.writeBoolean(_enabled_)
    gen.writeName("botId")
    val _botId_ = value.botId
    gen.writeString(_botId_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("startDate")
    val _startDate_ = value.startDate
    if (_startDate_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _startDate_)
    }
    gen.writeName("endDate")
    val _endDate_ = value.endDate
    if (_endDate_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _endDate_)
    }
    gen.writeName("graduation")
    val _graduation_ = value.graduation
    if (_graduation_ == null) {
      gen.writeNull()
    } else {
      gen.writeNumber(_graduation_)
    }
    gen.writeEndObject()
  }
}
