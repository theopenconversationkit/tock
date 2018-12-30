package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class Classification_Serializer :
        StdSerializer<Classification>(Classification::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(Classification::class.java, this)

    override fun serialize(
        value: Classification,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("intentId")
        val _intentId_ = value.intentId
        serializers.defaultSerializeValue(_intentId_, gen)
        gen.writeFieldName("entities")
        val _entities_ = value.entities
        serializers.defaultSerializeValue(_entities_, gen)
        gen.writeEndObject()
    }
}
