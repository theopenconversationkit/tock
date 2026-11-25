package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ArchivedEntityValueWrapper_Serializer : StdSerializer<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>(ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.java, this)

  override fun serialize(
    `value`: ArchivedEntityValuesCol.ArchivedEntityValueWrapper,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("entityValue")
    val _entityValue_ = value.entityValue
    if (_entityValue_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _entityValue_)
    }
    gen.writeName("actionId")
    val _actionId_ = value.actionId
    if (_actionId_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _actionId_)
    }
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
