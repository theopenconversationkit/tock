package ai.tock.nlp.front.shared.build

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ModelBuild_Serializer : StdSerializer<ModelBuild>(ModelBuild::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ModelBuild::class.java, this)

  override fun serialize(
    `value`: ModelBuild,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeName("language")
    val _language_ = value.language
    serializers.writeValue(gen, _language_)
    gen.writeName("type")
    val _type_ = value.type
    serializers.writeValue(gen, _type_)
    gen.writeName("intentId")
    val _intentId_ = value.intentId
    if (_intentId_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _intentId_)
    }
    gen.writeName("entityTypeName")
    val _entityTypeName_ = value.entityTypeName
    if (_entityTypeName_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_entityTypeName_)
    }
    gen.writeName("nbSentences")
    val _nbSentences_ = value.nbSentences
    gen.writeNumber(_nbSentences_)
    gen.writeName("duration")
    val _duration_ = value.duration
    serializers.writeValue(gen, _duration_)
    gen.writeName("error")
    val _error_ = value.error
    gen.writeBoolean(_error_)
    gen.writeName("errorMessage")
    val _errorMessage_ = value.errorMessage
    if (_errorMessage_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_errorMessage_)
    }
    gen.writeName("date")
    val _date_ = value.date
    serializers.writeValue(gen, _date_)
    gen.writeEndObject()
  }
}
