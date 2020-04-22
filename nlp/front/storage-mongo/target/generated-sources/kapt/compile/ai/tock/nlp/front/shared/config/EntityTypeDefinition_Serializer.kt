package ai.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class EntityTypeDefinition_Serializer :
        StdSerializer<EntityTypeDefinition>(EntityTypeDefinition::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(EntityTypeDefinition::class.java, this)

    override fun serialize(
        value: EntityTypeDefinition,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("description")
        val _description_ = value.description
        gen.writeString(_description_)
        gen.writeFieldName("subEntities")
        val _subEntities_ = value.subEntities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.nlp.front.shared.config.EntityDefinition::class.java)
                ),
                true,
                null
                )
                .serialize(_subEntities_, gen, serializers)
        gen.writeFieldName("dictionary")
        val _dictionary_ = value.dictionary
        gen.writeBoolean(_dictionary_)
        gen.writeFieldName("obfuscated")
        val _obfuscated_ = value.obfuscated
        gen.writeBoolean(_obfuscated_)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
