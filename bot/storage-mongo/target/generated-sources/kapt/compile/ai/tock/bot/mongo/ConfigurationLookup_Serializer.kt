package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ConfigurationLookup_Serializer :
        StdSerializer<ConfigurationLookup>(ConfigurationLookup::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ConfigurationLookup::class.java, this)

    override fun serialize(
        value: ConfigurationLookup,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("configuration")
        val _configuration_ = value.configuration
        serializers.defaultSerializeValue(_configuration_, gen)
        gen.writeEndObject()
    }
}
