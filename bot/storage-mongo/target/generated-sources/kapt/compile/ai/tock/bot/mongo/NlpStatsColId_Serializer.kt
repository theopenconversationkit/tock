package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpStatsColId_Serializer : StdSerializer<NlpStatsColId>(NlpStatsColId::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(NlpStatsColId::class.java, this)

    override fun serialize(
        value: NlpStatsColId,
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
