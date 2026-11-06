package ai.tock.nlp.front.shared.test

import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class TestBuild_Serializer : StdSerializer<TestBuild>(TestBuild::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(TestBuild::class.java, this)

  override fun serialize(
    `value`: TestBuild,
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
    gen.writeName("startDate")
    val _startDate_ = value.startDate
    serializers.writeValue(gen, _startDate_)
    gen.writeName("buildModelDuration")
    val _buildModelDuration_ = value.buildModelDuration
    serializers.writeValue(gen, _buildModelDuration_)
    gen.writeName("testSentencesDuration")
    val _testSentencesDuration_ = value.testSentencesDuration
    serializers.writeValue(gen, _testSentencesDuration_)
    gen.writeName("nbSentencesInModel")
    val _nbSentencesInModel_ = value.nbSentencesInModel
    gen.writeNumber(_nbSentencesInModel_)
    gen.writeName("nbSentencesTested")
    val _nbSentencesTested_ = value.nbSentencesTested
    gen.writeNumber(_nbSentencesTested_)
    gen.writeName("nbErrors")
    val _nbErrors_ = value.nbErrors
    gen.writeNumber(_nbErrors_)
    gen.writeName("intentErrors")
    val _intentErrors_ = value.intentErrors
    gen.writeNumber(_intentErrors_)
    gen.writeName("entityErrors")
    val _entityErrors_ = value.entityErrors
    gen.writeNumber(_entityErrors_)
    gen.writeName("nbSentencesTestedByIntent")
    val _nbSentencesTestedByIntent_ = value.nbSentencesTestedByIntent
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(String::class.javaObjectType), serializers.typeFactory.constructType(Int::class.javaObjectType)), true).serialize(_nbSentencesTestedByIntent_, gen, serializers)
    gen.writeName("intentErrorsByIntent")
    val _intentErrorsByIntent_ = value.intentErrorsByIntent
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(String::class.javaObjectType), serializers.typeFactory.constructType(Int::class.javaObjectType)), true).serialize(_intentErrorsByIntent_, gen, serializers)
    gen.writeName("entityErrorsByIntent")
    val _entityErrorsByIntent_ = value.entityErrorsByIntent
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(String::class.javaObjectType), serializers.typeFactory.constructType(Int::class.javaObjectType)), true).serialize(_entityErrorsByIntent_, gen, serializers)
    gen.writeEndObject()
  }
}
