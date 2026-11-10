package ai.tock.nlp.front.shared.config

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ClassifiedEntity_Serializer : StdSerializer<ClassifiedEntity>(ClassifiedEntity::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ClassifiedEntity::class.java, this)

  override fun serialize(
    `value`: ClassifiedEntity,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("type")
    val _type_ = value.type
    gen.writeString(_type_)
    gen.writeName("role")
    val _role_ = value.role
    gen.writeString(_role_)
    gen.writeName("start")
    val _start_ = value.start
    gen.writeNumber(_start_)
    gen.writeName("end")
    val _end_ = value.end
    gen.writeNumber(_end_)
    gen.writeName("subEntities")
    val _subEntities_ = value.subEntities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(ClassifiedEntity::class.javaObjectType)), true).serialize(_subEntities_, gen, serializers)
    gen.writeEndObject()
  }
}
