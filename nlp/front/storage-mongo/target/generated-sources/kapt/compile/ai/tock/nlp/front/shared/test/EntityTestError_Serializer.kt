package ai.tock.nlp.front.shared.test

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class EntityTestError_Serializer :
        StdSerializer<EntityTestError>(EntityTestError::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(EntityTestError::class.java, this)

    override fun serialize(
        value: EntityTestError,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("applicationId")
        val _applicationId_ = value.applicationId
        serializers.defaultSerializeValue(_applicationId_, gen)
        gen.writeFieldName("language")
        val _language_ = value.language
        serializers.defaultSerializeValue(_language_, gen)
        gen.writeFieldName("text")
        val _text_ = value.text
        gen.writeString(_text_)
        gen.writeFieldName("intentId")
        val _intentId_ = value.intentId
        if(_intentId_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_intentId_, gen)
                }
        gen.writeFieldName("lastAnalyse")
        val _lastAnalyse_ = value.lastAnalyse
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.nlp.front.shared.config.ClassifiedEntity::class.java)
                ),
                true,
                null
                )
                .serialize(_lastAnalyse_, gen, serializers)
        gen.writeFieldName("averageErrorProbability")
        val _averageErrorProbability_ = value.averageErrorProbability
        gen.writeNumber(_averageErrorProbability_)
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeFieldName("total")
        val _total_ = value.total
        gen.writeNumber(_total_)
        gen.writeFieldName("firstDetectionDate")
        val _firstDetectionDate_ = value.firstDetectionDate
        serializers.defaultSerializeValue(_firstDetectionDate_, gen)
        gen.writeEndObject()
    }
}
