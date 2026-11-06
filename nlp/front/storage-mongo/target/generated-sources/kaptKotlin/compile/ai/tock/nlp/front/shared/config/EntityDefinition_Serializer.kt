package ai.tock.nlp.front.shared.config

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class EntityDefinition_Serializer : StdSerializer<EntityDefinition>(EntityDefinition::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(EntityDefinition::class.java, this)

  override fun serialize(
    `value`: EntityDefinition,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("entityTypeName")
    val _entityTypeName_ = value.entityTypeName
    gen.writeString(_entityTypeName_)
    gen.writeName("role")
    val _role_ = value.role
    gen.writeString(_role_)
    gen.writeName("atStartOfDay")
    val _atStartOfDay_ = value.atStartOfDay
    if (_atStartOfDay_ == null) {
      gen.writeNull()
    } else {
      gen.writeBoolean(_atStartOfDay_)
    }
    gen.writeEndObject()
  }
}
