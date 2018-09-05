package fr.vsct.tock.nlp.front.shared.test

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

class IntentTestError_Serializer : StdSerializer<IntentTestError>(IntentTestError::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(this)

    override fun serialize(
            value: IntentTestError,
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
        gen.writeFieldName("currentIntent")
        val _currentIntent_ = value.currentIntent
        gen.writeString(_currentIntent_)
        gen.writeFieldName("wrongIntent")
        val _wrongIntent_ = value.wrongIntent
        gen.writeString(_wrongIntent_)
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
