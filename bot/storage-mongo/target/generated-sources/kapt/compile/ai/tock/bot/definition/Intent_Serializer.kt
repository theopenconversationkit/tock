package ai.tock.bot.definition

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class Intent_Serializer : StdSerializer<Intent>(Intent::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(Intent::class.java, this)

    override fun serialize(
        value: Intent,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeEndObject()
    }
}
