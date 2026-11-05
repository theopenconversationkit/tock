package ai.tock.nlp.front.storage.mongo

import kotlin.Double
import kotlin.String
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ClassifiedSentenceCol_Serializer : StdSerializer<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java, this)

  override fun serialize(
    `value`: ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("text")
    val _text_ = value.text
    gen.writeString(_text_)
    gen.writeName("normalizedText")
    val _normalizedText_ = value.normalizedText
    gen.writeString(_normalizedText_)
    gen.writeName("fullText")
    val _fullText_ = value.fullText
    gen.writeString(_fullText_)
    gen.writeName("language")
    val _language_ = value.language
    serializers.writeValue(gen, _language_)
    gen.writeName("applicationId")
    val _applicationId_ = value.applicationId
    serializers.writeValue(gen, _applicationId_)
    gen.writeName("creationDate")
    val _creationDate_ = value.creationDate
    serializers.writeValue(gen, _creationDate_)
    gen.writeName("updateDate")
    val _updateDate_ = value.updateDate
    serializers.writeValue(gen, _updateDate_)
    gen.writeName("status")
    val _status_ = value.status
    serializers.writeValue(gen, _status_)
    gen.writeName("classification")
    val _classification_ = value.classification
    serializers.writeValue(gen, _classification_)
    gen.writeName("lastIntentProbability")
    val _lastIntentProbability_ = value.lastIntentProbability
    if (_lastIntentProbability_ == null) {
      gen.writeNull()
    } else {
      gen.writeNumber(_lastIntentProbability_)
    }
    gen.writeName("lastEntityProbability")
    val _lastEntityProbability_ = value.lastEntityProbability
    if (_lastEntityProbability_ == null) {
      gen.writeNull()
    } else {
      gen.writeNumber(_lastEntityProbability_)
    }
    gen.writeName("lastUsage")
    val _lastUsage_ = value.lastUsage
    if (_lastUsage_ == null) {
      gen.writeNull()
    } else {
      serializers.writeValue(gen, _lastUsage_)
    }
    gen.writeName("usageCount")
    val _usageCount_ = value.usageCount
    if (_usageCount_ == null) {
      gen.writeNull()
    } else {
      gen.writeNumber(_usageCount_)
    }
    gen.writeName("unknownCount")
    val _unknownCount_ = value.unknownCount
    if (_unknownCount_ == null) {
      gen.writeNull()
    } else {
      gen.writeNumber(_unknownCount_)
    }
    gen.writeName("forReview")
    val _forReview_ = value.forReview
    gen.writeBoolean(_forReview_)
    gen.writeName("reviewComment")
    val _reviewComment_ = value.reviewComment
    if (_reviewComment_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_reviewComment_)
    }
    gen.writeName("classifier")
    val _classifier_ = value.classifier
    if (_classifier_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_classifier_)
    }
    gen.writeName("otherIntentsProbabilities")
    val _otherIntentsProbabilities_ = value.otherIntentsProbabilities
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(String::class.javaObjectType), serializers.typeFactory.constructType(Double::class.javaObjectType)), true).serialize(_otherIntentsProbabilities_, gen, serializers)
    gen.writeName("configuration")
    val _configuration_ = value.configuration
    if (_configuration_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_configuration_)
    }
    gen.writeEndObject()
  }
}
