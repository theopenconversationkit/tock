package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ClassifiedEntity
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class EntityTestError_Serializer : StdSerializer<EntityTestError>(EntityTestError::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(EntityTestError::class.java, this)

  override fun serialize(
    `value`: EntityTestError,
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
    gen.writeName("intentId")
    val _intentId_ = value.intentId
    if (_intentId_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _intentId_)
    }
    gen.writeName("lastAnalyse")
    val _lastAnalyse_ = value.lastAnalyse
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(ClassifiedEntity::class.javaObjectType)), true).serialize(_lastAnalyse_, gen, serializers)
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
