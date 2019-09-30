package ai.tock.bot.engine.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class PlayerId_Serializer : StdSerializer<PlayerId>(PlayerId::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(PlayerId::class.java, this)

    override fun serialize(
        value: PlayerId,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("id")
        val _id_ = value.id
        gen.writeString(_id_)
        gen.writeFieldName("type")
        val _type_ = value.type
        serializers.defaultSerializeValue(_type_, gen)
        gen.writeFieldName("clientId")
        val _clientId_ = value.clientId
        if(_clientId_ == null) { gen.writeNull() } else {
                gen.writeString(_clientId_)
                }
        gen.writeEndObject()
    }
}
