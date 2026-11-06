package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class StoryDefinitionConfigurationHistoryCol_Serializer : StdSerializer<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java, this)

  override fun serialize(
    `value`: StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("conf")
    val _conf_ = value.conf
    serializers.writeValue(gen, _conf_)
    gen.writeName("deleted")
    val _deleted_ = value.deleted
    gen.writeBoolean(_deleted_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
