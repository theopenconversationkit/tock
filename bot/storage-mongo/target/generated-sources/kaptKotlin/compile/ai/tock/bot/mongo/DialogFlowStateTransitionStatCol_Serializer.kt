package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogFlowStateTransitionStatCol_Serializer : StdSerializer<DialogFlowStateTransitionStatCol>(DialogFlowStateTransitionStatCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogFlowStateTransitionStatCol::class.java, this)

  override fun serialize(
    `value`: DialogFlowStateTransitionStatCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeName("transitionId")
    val _transitionId_ = value.transitionId
    serializers.writeValue(gen, _transitionId_)
    gen.writeName("dialogId")
    val _dialogId_ = value.dialogId
    serializers.writeValue(gen, _dialogId_)
    gen.writeName("text")
    val _text_ = value.text
    if (_text_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_text_)
    }
    gen.writeName("locale")
    val _locale_ = value.locale
    if (_locale_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _locale_)
    }
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeName("processedLevel")
    val _processedLevel_ = value.processedLevel
    gen.writeNumber(_processedLevel_)
    gen.writeEndObject()
  }
}
