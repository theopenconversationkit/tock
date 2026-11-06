package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogTextCol_Serializer : StdSerializer<DialogTextCol>(DialogTextCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogTextCol::class.java, this)

  override fun serialize(
    `value`: DialogTextCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("text")
    val _text_ = value.text
    gen.writeString(_text_)
    gen.writeName("dialogId")
    val _dialogId_ = value.dialogId
    serializers.writeValue(gen, _dialogId_)
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
