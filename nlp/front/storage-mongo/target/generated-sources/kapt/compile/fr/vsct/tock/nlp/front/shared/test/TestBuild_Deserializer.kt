package fr.vsct.tock.nlp.front.shared.test

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class TestBuild_Deserializer : JsonDeserializer<TestBuild>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TestBuild::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestBuild {
        with(p) {
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set : Boolean = false
            var _language_: Locale? = null
            var _language_set : Boolean = false
            var _startDate_: Instant? = null
            var _startDate_set : Boolean = false
            var _buildModelDuration_: Duration? = null
            var _buildModelDuration_set : Boolean = false
            var _testSentencesDuration_: Duration? = null
            var _testSentencesDuration_set : Boolean = false
            var _nbSentencesInModel_: Int? = null
            var _nbSentencesInModel_set : Boolean = false
            var _nbSentencesTested_: Int? = null
            var _nbSentencesTested_set : Boolean = false
            var _nbErrors_: Int? = null
            var _nbErrors_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "language" -> {
                            _language_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "startDate" -> {
                            _startDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _startDate_set = true
                            }
                    "buildModelDuration" -> {
                            _buildModelDuration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Duration::class.java);
                            _buildModelDuration_set = true
                            }
                    "testSentencesDuration" -> {
                            _testSentencesDuration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Duration::class.java);
                            _testSentencesDuration_set = true
                            }
                    "nbSentencesInModel" -> {
                            _nbSentencesInModel_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _nbSentencesInModel_set = true
                            }
                    "nbSentencesTested" -> {
                            _nbSentencesTested_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _nbSentencesTested_set = true
                            }
                    "nbErrors" -> {
                            _nbErrors_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _nbErrors_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_applicationId_set && _language_set && _startDate_set &&
                    _buildModelDuration_set && _testSentencesDuration_set && _nbSentencesInModel_set
                    && _nbSentencesTested_set && _nbErrors_set)
                    TestBuild(applicationId = _applicationId_!!, language = _language_!!, startDate
                            = _startDate_!!, buildModelDuration = _buildModelDuration_!!,
                            testSentencesDuration = _testSentencesDuration_!!, nbSentencesInModel =
                            _nbSentencesInModel_!!, nbSentencesTested = _nbSentencesTested_!!,
                            nbErrors = _nbErrors_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_language_set)
                    map[parameters.getValue("language")] = _language_
                    if(_startDate_set)
                    map[parameters.getValue("startDate")] = _startDate_
                    if(_buildModelDuration_set)
                    map[parameters.getValue("buildModelDuration")] = _buildModelDuration_
                    if(_testSentencesDuration_set)
                    map[parameters.getValue("testSentencesDuration")] = _testSentencesDuration_
                    if(_nbSentencesInModel_set)
                    map[parameters.getValue("nbSentencesInModel")] = _nbSentencesInModel_
                    if(_nbSentencesTested_set)
                    map[parameters.getValue("nbSentencesTested")] = _nbSentencesTested_
                    if(_nbErrors_set)
                    map[parameters.getValue("nbErrors")] = _nbErrors_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<TestBuild> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { TestBuild::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "language" to
                primaryConstructor.findParameterByName("language")!!, "startDate" to
                primaryConstructor.findParameterByName("startDate")!!, "buildModelDuration" to
                primaryConstructor.findParameterByName("buildModelDuration")!!,
                "testSentencesDuration" to
                primaryConstructor.findParameterByName("testSentencesDuration")!!,
                "nbSentencesInModel" to
                primaryConstructor.findParameterByName("nbSentencesInModel")!!, "nbSentencesTested"
                to primaryConstructor.findParameterByName("nbSentencesTested")!!, "nbErrors" to
                primaryConstructor.findParameterByName("nbErrors")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}
    }
}
