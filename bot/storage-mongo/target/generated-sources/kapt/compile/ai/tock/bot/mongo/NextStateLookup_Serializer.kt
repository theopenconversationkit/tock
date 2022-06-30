package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class NextStateLookup_Serializer :
        StdSerializer<NextStateLookup>(NextStateLookup::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(NextStateLookup::class.java, this)

    override fun serialize(
        value: NextStateLookup,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("nextState")
        val _nextState_ = value.nextState
        serializers.defaultSerializeValue(_nextState_, gen)
        gen.writeEndObject()
    }
}
