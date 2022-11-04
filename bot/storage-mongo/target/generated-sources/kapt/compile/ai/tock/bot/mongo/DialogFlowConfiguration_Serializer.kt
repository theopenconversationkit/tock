package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowConfiguration_Serializer :
        StdSerializer<DialogFlowConfiguration>(DialogFlowConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(DialogFlowConfiguration::class.java, this)

    override fun serialize(
        value: DialogFlowConfiguration,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        gen.writeString(__id_)
        gen.writeFieldName("currentProcessedLevel")
        val _currentProcessedLevel_ = value.currentProcessedLevel
        gen.writeNumber(_currentProcessedLevel_)
        gen.writeEndObject()
    }
}
