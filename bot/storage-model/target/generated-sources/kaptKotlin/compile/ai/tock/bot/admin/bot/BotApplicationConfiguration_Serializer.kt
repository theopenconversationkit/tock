package ai.tock.bot.admin.bot

import kotlin.String
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class BotApplicationConfiguration_Serializer : StdSerializer<BotApplicationConfiguration>(BotApplicationConfiguration::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(BotApplicationConfiguration::class.java, this)

  override fun serialize(
    `value`: BotApplicationConfiguration,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    gen.writeString(_applicationId_)
    gen.writeName("botId")
    val _botId_ = value.botId
    gen.writeString(_botId_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("nlpModel")
    val _nlpModel_ = value.nlpModel
    gen.writeString(_nlpModel_)
    gen.writeName("connectorType")
    val _connectorType_ = value.connectorType
    serializers.writeValue(gen, _connectorType_)
    gen.writeName("ownerConnectorType")
    val _ownerConnectorType_ = value.ownerConnectorType
    if (_ownerConnectorType_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _ownerConnectorType_)
    }
    gen.writeName("name")
    val _name_ = value.name
    gen.writeString(_name_)
    gen.writeName("baseUrl")
    val _baseUrl_ = value.baseUrl
    if (_baseUrl_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_baseUrl_)
    }
    gen.writeName("parameters")
    val _parameters_ = value.parameters
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(String::class.javaObjectType), serializers.typeFactory.constructType(String::class.javaObjectType)), true).serialize(_parameters_, gen, serializers)
    gen.writeName("path")
    val _path_ = value.path
    if (_path_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_path_)
    }
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("targetConfigurationId")
    val _targetConfigurationId_ = value.targetConfigurationId
    if (_targetConfigurationId_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _targetConfigurationId_)
    }
    gen.writeEndObject()
  }
}
