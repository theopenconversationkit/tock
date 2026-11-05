package ai.tock.nlp.model.service.storage.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class NlpApplicationConfigurationCol_Serializer : StdSerializer<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java, this)

  override fun serialize(
    `value`: NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("applicationName")
    val _applicationName_ = value.applicationName
    gen.writeString(_applicationName_)
    gen.writeName("engineType")
    val _engineType_ = value.engineType
    serializers.writeValue(gen, _engineType_)
    gen.writeName("configuration")
    val _configuration_ = value.configuration
    serializers.writeValue(gen, _configuration_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
