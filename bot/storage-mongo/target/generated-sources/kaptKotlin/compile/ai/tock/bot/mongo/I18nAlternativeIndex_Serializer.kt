package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class I18nAlternativeIndex_Serializer : StdSerializer<I18nAlternativeIndex>(I18nAlternativeIndex::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(I18nAlternativeIndex::class.java, this)

  override fun serialize(
    `value`: I18nAlternativeIndex,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("labelId")
    val _labelId_ = value.labelId
    serializers.writeValue(gen, _labelId_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("locale")
    val _locale_ = value.locale
    serializers.writeValue(gen, _locale_)
    gen.writeName("interfaceType")
    val _interfaceType_ = value.interfaceType
    serializers.writeValue(gen, _interfaceType_)
    gen.writeName("connectorId")
    val _connectorId_ = value.connectorId
    if (_connectorId_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_connectorId_)
    }
    gen.writeName("contextId")
    val _contextId_ = value.contextId
    gen.writeString(_contextId_)
    gen.writeName("index")
    val _index_ = value.index
    gen.writeNumber(_index_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
