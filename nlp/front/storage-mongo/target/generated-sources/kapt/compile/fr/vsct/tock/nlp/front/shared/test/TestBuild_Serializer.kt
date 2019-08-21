package fr.vsct.tock.nlp.front.shared.test

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class TestBuild_Serializer : StdSerializer<TestBuild>(TestBuild::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(TestBuild::class.java, this)

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
        gen.writeFieldName("intentErrors")
        val _intentErrors_ = value.intentErrors
        gen.writeNumber(_intentErrors_)
        gen.writeFieldName("entityErrors")
        val _entityErrors_ = value.entityErrors
        gen.writeNumber(_entityErrors_)
        gen.writeFieldName("nbSentencesTestedByIntent")
        val _nbSentencesTestedByIntent_ = value.nbSentencesTestedByIntent
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java),
                serializers.config.typeFactory.constructType(kotlin.Int::class.java)
                ),
                true,
                null
                )
                .serialize(_nbSentencesTestedByIntent_, gen, serializers)
        gen.writeFieldName("intentErrorsByIntent")
        val _intentErrorsByIntent_ = value.intentErrorsByIntent
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java),
                serializers.config.typeFactory.constructType(kotlin.Int::class.java)
                ),
                true,
                null
                )
                .serialize(_intentErrorsByIntent_, gen, serializers)
        gen.writeFieldName("entityErrorsByIntent")
        val _entityErrorsByIntent_ = value.entityErrorsByIntent
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java),
                serializers.config.typeFactory.constructType(kotlin.Int::class.java)
                ),
                true,
                null
                )
                .serialize(_entityErrorsByIntent_, gen, serializers)
        gen.writeEndObject()
    }
}
