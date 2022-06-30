package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class TransitionLookup_Serializer :
        StdSerializer<TransitionLookup>(TransitionLookup::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(TransitionLookup::class.java, this)

    override fun serialize(
        value: TransitionLookup,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("transition")
        val _transition_ = value.transition
        serializers.defaultSerializeValue(_transition_, gen)
        gen.writeEndObject()
    }
}
