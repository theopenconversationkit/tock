package ai.tock.nlp.front.shared.config

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class Classification_Serializer : StdSerializer<Classification>(Classification::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(Classification::class.java, this)

  override fun serialize(
    `value`: Classification,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("intentId")
    val _intentId_ = value.intentId
    serializers.writeValue(gen, _intentId_)
    gen.writeName("entities")
    val _entities_ = value.entities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(ClassifiedEntity::class.javaObjectType)), true).serialize(_entities_, gen, serializers)
    gen.writeEndObject()
  }
}
