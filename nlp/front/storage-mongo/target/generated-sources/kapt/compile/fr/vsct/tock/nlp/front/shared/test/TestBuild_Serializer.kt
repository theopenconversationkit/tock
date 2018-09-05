package fr.vsct.tock.nlp.front.shared.test

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

class TestBuild_Serializer : StdSerializer<TestBuild>(TestBuild::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(this)

    override fun serialize(
            value: TestBuild,
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
        gen.writeFieldName("startDate")
        val _startDate_ = value.startDate
        serializers.defaultSerializeValue(_startDate_, gen)
        gen.writeFieldName("buildModelDuration")
        val _buildModelDuration_ = value.buildModelDuration
        serializers.defaultSerializeValue(_buildModelDuration_, gen)
        gen.writeFieldName("testSentencesDuration")
        val _testSentencesDuration_ = value.testSentencesDuration
        serializers.defaultSerializeValue(_testSentencesDuration_, gen)
        gen.writeFieldName("nbSentencesInModel")
        val _nbSentencesInModel_ = value.nbSentencesInModel
        gen.writeNumber(_nbSentencesInModel_)
        gen.writeFieldName("nbSentencesTested")
        val _nbSentencesTested_ = value.nbSentencesTested
        gen.writeNumber(_nbSentencesTested_)
        gen.writeFieldName("nbErrors")
        val _nbErrors_ = value.nbErrors
        gen.writeNumber(_nbErrors_)
        gen.writeEndObject()
    }
}
