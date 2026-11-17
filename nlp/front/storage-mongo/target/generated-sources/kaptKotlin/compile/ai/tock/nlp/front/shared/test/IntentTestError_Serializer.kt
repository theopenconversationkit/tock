package ai.tock.nlp.front.shared.test

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class IntentTestError_Serializer : StdSerializer<IntentTestError>(IntentTestError::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(IntentTestError::class.java, this)

  override fun serialize(
    `value`: IntentTestError,
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
    gen.writeName("text")
    val _text_ = value.text
    gen.writeString(_text_)
    gen.writeName("currentIntent")
    val _currentIntent_ = value.currentIntent
    gen.writeString(_currentIntent_)
    gen.writeName("wrongIntent")
    val _wrongIntent_ = value.wrongIntent
    gen.writeString(_wrongIntent_)
    gen.writeName("averageErrorProbability")
    val _averageErrorProbability_ = value.averageErrorProbability
    gen.writeNumber(_averageErrorProbability_)
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeName("total")
    val _total_ = value.total
    gen.writeNumber(_total_)
    gen.writeName("firstDetectionDate")
    val _firstDetectionDate_ = value.firstDetectionDate
    serializers.writeValue(gen, _firstDetectionDate_)
    gen.writeEndObject()
  }
}
