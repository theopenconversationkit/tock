package ai.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class EntityDefinition_Serializer :
        StdSerializer<EntityDefinition>(EntityDefinition::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(EntityDefinition::class.java, this)

    override fun serialize(
        value: EntityDefinition,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("entityTypeName")
        val _entityTypeName_ = value.entityTypeName
        gen.writeString(_entityTypeName_)
        gen.writeFieldName("role")
        val _role_ = value.role
        gen.writeString(_role_)
        gen.writeFieldName("atStartOfDay")
        val _atStartOfDay_ = value.atStartOfDay
        if(_atStartOfDay_ == null) { gen.writeNull() } else {
                gen.writeBoolean(_atStartOfDay_)
                }
        gen.writeEndObject()
    }
}
