package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class GroupById_Serializer : StdSerializer<GroupById>(GroupById::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(GroupById::class.java, this)

  override fun serialize(
    `value`: GroupById,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("date")
    val _date_ = value.date
    gen.writeString(_date_)
    gen.writeName("dialogId")
    val _dialogId_ = value.dialogId
    gen.writeString(_dialogId_)
    gen.writeName("connectorType")
    val _connectorType_ = value.connectorType
    serializers.writeValue(gen, _connectorType_)
    gen.writeName("configuration")
    val _configuration_ = value.configuration
    gen.writeString(_configuration_)
    gen.writeName("intent")
    val _intent_ = value.intent
    gen.writeString(_intent_)
    gen.writeName("storyDefinitionId")
    val _storyDefinitionId_ = value.storyDefinitionId
    gen.writeString(_storyDefinitionId_)
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeEndObject()
  }
}
