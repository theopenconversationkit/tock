package ai.tock.bot.definition

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class IntentWithoutNamespace_Serializer :
        StdSerializer<IntentWithoutNamespace>(IntentWithoutNamespace::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(IntentWithoutNamespace::class.java, this)

    override fun serialize(
        value: IntentWithoutNamespace,
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
