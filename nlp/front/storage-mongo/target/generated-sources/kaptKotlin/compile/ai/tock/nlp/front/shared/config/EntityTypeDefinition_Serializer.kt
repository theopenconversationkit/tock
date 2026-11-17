package ai.tock.nlp.front.shared.config

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class EntityTypeDefinition_Serializer : StdSerializer<EntityTypeDefinition>(EntityTypeDefinition::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(EntityTypeDefinition::class.java, this)

  override fun serialize(
    `value`: EntityTypeDefinition,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("name")
    val _name_ = value.name
    gen.writeString(_name_)
    gen.writeName("description")
    val _description_ = value.description
    gen.writeString(_description_)
    gen.writeName("subEntities")
    val _subEntities_ = value.subEntities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(EntityDefinition::class.javaObjectType)), true).serialize(_subEntities_, gen, serializers)
    gen.writeName("dictionary")
    val _dictionary_ = value.dictionary
    gen.writeBoolean(_dictionary_)
    gen.writeName("obfuscated")
    val _obfuscated_ = value.obfuscated
    gen.writeBoolean(_obfuscated_)
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeEndObject()
  }
}
