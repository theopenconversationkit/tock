package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ConnectorMessageColId_Serializer :
        StdSerializer<ConnectorMessageColId>(ConnectorMessageColId::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ConnectorMessageColId::class.java, this)

    override fun serialize(
        value: ConnectorMessageColId,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("actionId")
        val _actionId_ = value.actionId
        serializers.defaultSerializeValue(_actionId_, gen)
        gen.writeFieldName("dialogId")
        val _dialogId_ = value.dialogId
        serializers.defaultSerializeValue(_dialogId_, gen)
        gen.writeEndObject()
    }
}
