package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ClassifiedEntity_Serializer :
        StdSerializer<ClassifiedEntity>(ClassifiedEntity::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ClassifiedEntity::class.java, this)

    override fun serialize(
        value: ClassifiedEntity,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("type")
        val _type_ = value.type
        gen.writeString(_type_)
        gen.writeFieldName("role")
        val _role_ = value.role
        gen.writeString(_role_)
        gen.writeFieldName("start")
        val _start_ = value.start
        gen.writeNumber(_start_)
        gen.writeFieldName("end")
        val _end_ = value.end
        gen.writeNumber(_end_)
        gen.writeFieldName("subEntities")
        val _subEntities_ = value.subEntities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                        serializers.config.typeFactory.constructType(fr.vsct.tock.nlp.front.shared.config.ClassifiedEntity::class.java)
                )
                ,
                true,
                null
                )
                .serialize(_subEntities_, gen, serializers)
        gen.writeEndObject()
    }
}
