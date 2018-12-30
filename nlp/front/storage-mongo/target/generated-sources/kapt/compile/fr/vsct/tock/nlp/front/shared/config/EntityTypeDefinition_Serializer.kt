package fr.vsct.tock.nlp.front.shared.config

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
        serializers.defaultSerializeValue(_subEntities_, gen)
        gen.writeFieldName("predefinedValues")
        val _predefinedValues_ = value.predefinedValues
        serializers.defaultSerializeValue(_predefinedValues_, gen)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
