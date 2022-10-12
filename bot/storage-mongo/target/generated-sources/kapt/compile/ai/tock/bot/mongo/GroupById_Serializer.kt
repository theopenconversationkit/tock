package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class GroupById_Serializer : StdSerializer<GroupById>(GroupById::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(GroupById::class.java, this)

    override fun serialize(
        value: GroupById,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("date")
        val _date_ = value.date
        gen.writeString(_date_)
        gen.writeFieldName("dialogId")
        val _dialogId_ = value.dialogId
        gen.writeString(_dialogId_)
        gen.writeFieldName("connectorType")
        val _connectorType_ = value.connectorType
        serializers.defaultSerializeValue(_connectorType_, gen)
        gen.writeFieldName("configuration")
        val _configuration_ = value.configuration
        gen.writeString(_configuration_)
        gen.writeFieldName("intent")
        val _intent_ = value.intent
        gen.writeString(_intent_)
        gen.writeFieldName("storyDefinitionId")
        val _storyDefinitionId_ = value.storyDefinitionId
        gen.writeString(_storyDefinitionId_)
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeEndObject()
    }
}
