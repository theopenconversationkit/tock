package ai.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ClassifiedSentenceCol_Serializer :
        StdSerializer<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java,
            this)

    override fun serialize(
        value: ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("text")
        val _text_ = value.text
        gen.writeString(_text_)
        gen.writeFieldName("normalizedText")
        val _normalizedText_ = value.normalizedText
        gen.writeString(_normalizedText_)
        gen.writeFieldName("fullText")
        val _fullText_ = value.fullText
        gen.writeString(_fullText_)
        gen.writeFieldName("language")
        val _language_ = value.language
        serializers.defaultSerializeValue(_language_, gen)
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("creationDate")
        val _creationDate_ = value.creationDate
        serializers.defaultSerializeValue(_creationDate_, gen)
        gen.writeFieldName("updateDate")
        val _updateDate_ = value.updateDate
        serializers.defaultSerializeValue(_updateDate_, gen)
        gen.writeFieldName("status")
        val _status_ = value.status
        serializers.defaultSerializeValue(_status_, gen)
        gen.writeFieldName("classification")
        val _classification_ = value.classification
        serializers.defaultSerializeValue(_classification_, gen)
        gen.writeFieldName("lastIntentProbability")
        val _lastIntentProbability_ = value.lastIntentProbability
        if(_lastIntentProbability_ == null) { gen.writeNull() } else {
                gen.writeNumber(_lastIntentProbability_)
                }
        gen.writeFieldName("lastEntityProbability")
        val _lastEntityProbability_ = value.lastEntityProbability
        if(_lastEntityProbability_ == null) { gen.writeNull() } else {
                gen.writeNumber(_lastEntityProbability_)
                }
        gen.writeFieldName("lastUsage")
        val _lastUsage_ = value.lastUsage
        if(_lastUsage_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_lastUsage_, gen)
                }
        gen.writeFieldName("usageCount")
        val _usageCount_ = value.usageCount
        if(_usageCount_ == null) { gen.writeNull() } else {
                gen.writeNumber(_usageCount_)
                }
        gen.writeFieldName("unknownCount")
        val _unknownCount_ = value.unknownCount
        if(_unknownCount_ == null) { gen.writeNull() } else {
                gen.writeNumber(_unknownCount_)
                }
        gen.writeFieldName("forReview")
        val _forReview_ = value.forReview
        gen.writeBoolean(_forReview_)
        gen.writeFieldName("reviewComment")
        val _reviewComment_ = value.reviewComment
        if(_reviewComment_ == null) { gen.writeNull() } else {
                gen.writeString(_reviewComment_)
                }
        gen.writeFieldName("classifier")
        val _classifier_ = value.classifier
        if(_classifier_ == null) { gen.writeNull() } else {
                gen.writeString(_classifier_)
                }
        gen.writeFieldName("otherIntentsProbabilities")
        val _otherIntentsProbabilities_ = value.otherIntentsProbabilities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java),
                serializers.config.typeFactory.constructType(kotlin.Double::class.java)
                ),
                true,
                null
                )
                .serialize(_otherIntentsProbabilities_, gen, serializers)
        gen.writeFieldName("configuration")
        val _configuration_ = value.configuration
        if(_configuration_ == null) { gen.writeNull() } else {
                gen.writeString(_configuration_)
                }
        gen.writeEndObject()
    }
}
