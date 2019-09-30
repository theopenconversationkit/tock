package ai.tock.bot.admin.bot

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class BotApplicationConfiguration_Serializer :
        StdSerializer<BotApplicationConfiguration>(BotApplicationConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(BotApplicationConfiguration::class.java,
            this)

    override fun serialize(
        value: BotApplicationConfiguration,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        gen.writeString(_applicationId_)
        gen.writeFieldName("botId")
        val _botId_ = value.botId
        gen.writeString(_botId_)
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("nlpModel")
        val _nlpModel_ = value.nlpModel
        gen.writeString(_nlpModel_)
        gen.writeFieldName("connectorType")
        val _connectorType_ = value.connectorType
        serializers.defaultSerializeValue(_connectorType_, gen)
        gen.writeFieldName("ownerConnectorType")
        val _ownerConnectorType_ = value.ownerConnectorType
        if(_ownerConnectorType_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_ownerConnectorType_, gen)
                }
        gen.writeFieldName("name")
        val _name_ = value.name
        gen.writeString(_name_)
        gen.writeFieldName("baseUrl")
        val _baseUrl_ = value.baseUrl
        if(_baseUrl_ == null) { gen.writeNull() } else {
                gen.writeString(_baseUrl_)
                }
        gen.writeFieldName("parameters")
        val _parameters_ = value.parameters
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java),
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                ),
                true,
                null
                )
                .serialize(_parameters_, gen, serializers)
        gen.writeFieldName("path")
        val _path_ = value.path
        if(_path_ == null) { gen.writeNull() } else {
                gen.writeString(_path_)
                }
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("targetConfigurationId")
        val _targetConfigurationId_ = value.targetConfigurationId
        if(_targetConfigurationId_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_targetConfigurationId_, gen)
                }
        gen.writeEndObject()
    }
}
