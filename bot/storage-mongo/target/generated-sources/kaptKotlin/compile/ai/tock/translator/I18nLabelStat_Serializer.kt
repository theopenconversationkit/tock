package ai.tock.translator

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class I18nLabelStat_Serializer : StdSerializer<I18nLabelStat>(I18nLabelStat::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(I18nLabelStat::class.java, this)

  override fun serialize(
    `value`: I18nLabelStat,
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
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeName("lastUpdate")
    val _lastUpdate_ = value.lastUpdate
    serializers.writeValue(gen, _lastUpdate_)
    gen.writeEndObject()
  }
}
