package ai.tock.bot.admin.bot

import java.util.Locale
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class BotConfiguration_Serializer : StdSerializer<BotConfiguration>(BotConfiguration::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(BotConfiguration::class.java, this)

  override fun serialize(
    `value`: BotConfiguration,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("name")
    val _name_ = value.name
    gen.writeString(_name_)
    gen.writeName("botId")
    val _botId_ = value.botId
    gen.writeString(_botId_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("nlpModel")
    val _nlpModel_ = value.nlpModel
    gen.writeString(_nlpModel_)
    gen.writeName("apiKey")
    val _apiKey_ = value.apiKey
    gen.writeString(_apiKey_)
    gen.writeName("webhookUrl")
    val _webhookUrl_ = value.webhookUrl
    if (_webhookUrl_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_webhookUrl_)
    }
    gen.writeName("supportedLocales")
    val _supportedLocales_ = value.supportedLocales
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(Locale::class.javaObjectType)), true).serialize(_supportedLocales_, gen, serializers)
    gen.writeEndObject()
  }
}
