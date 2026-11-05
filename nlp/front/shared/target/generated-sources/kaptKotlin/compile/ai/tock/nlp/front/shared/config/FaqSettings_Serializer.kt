package ai.tock.nlp.front.shared.config

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class FaqSettings_Serializer : StdSerializer<FaqSettings>(FaqSettings::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(FaqSettings::class.java, this)

  override fun serialize(
    `value`: FaqSettings,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeName("satisfactionEnabled")
    val _satisfactionEnabled_ = value.satisfactionEnabled
    gen.writeBoolean(_satisfactionEnabled_)
    gen.writeName("satisfactionStoryId")
    val _satisfactionStoryId_ = value.satisfactionStoryId
    if (_satisfactionStoryId_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_satisfactionStoryId_)
    }
    gen.writeName("creationDate")
    val _creationDate_ = value.creationDate
    serializers.writeValue(gen, _creationDate_)
    gen.writeName("updateDate")
    val _updateDate_ = value.updateDate
    serializers.writeValue(gen, _updateDate_)
    gen.writeEndObject()
  }
}
