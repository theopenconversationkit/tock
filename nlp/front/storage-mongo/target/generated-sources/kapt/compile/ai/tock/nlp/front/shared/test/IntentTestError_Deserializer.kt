package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class IntentTestError_Deserializer : JsonDeserializer<IntentTestError>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(IntentTestError::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntentTestError {
        with(p) {
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set : Boolean = false
            var _language_: Locale? = null
            var _language_set : Boolean = false
            var _text_: String? = null
            var _text_set : Boolean = false
            var _currentIntent_: String? = null
            var _currentIntent_set : Boolean = false
            var _wrongIntent_: String? = null
            var _wrongIntent_set : Boolean = false
            var _averageErrorProbability_: Double? = null
            var _averageErrorProbability_set : Boolean = false
            var _count_: Int? = null
            var _count_set : Boolean = false
            var _total_: Int? = null
            var _total_set : Boolean = false
            var _firstDetectionDate_: Instant? = null
            var _firstDetectionDate_set : Boolean = false
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
                    "text" -> {
                            _text_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "currentIntent" -> {
                            _currentIntent_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _currentIntent_set = true
                            }
                    "wrongIntent" -> {
                            _wrongIntent_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _wrongIntent_set = true
                            }
                    "averageErrorProbability" -> {
                            _averageErrorProbability_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _averageErrorProbability_set = true
                            }
                    "count" -> {
                            _count_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _count_set = true
                            }
                    "total" -> {
                            _total_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _total_set = true
                            }
                    "firstDetectionDate" -> {
                            _firstDetectionDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _firstDetectionDate_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_applicationId_set && _language_set && _text_set && _currentIntent_set &&
                    _wrongIntent_set && _averageErrorProbability_set && _count_set && _total_set &&
                    _firstDetectionDate_set)
                    IntentTestError(applicationId = _applicationId_!!, language = _language_!!, text
                            = _text_!!, currentIntent = _currentIntent_!!, wrongIntent =
                            _wrongIntent_!!, averageErrorProbability = _averageErrorProbability_!!,
                            count = _count_!!, total = _total_!!, firstDetectionDate =
                            _firstDetectionDate_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_language_set)
                    map[parameters.getValue("language")] = _language_
                    if(_text_set)
                    map[parameters.getValue("text")] = _text_
                    if(_currentIntent_set)
                    map[parameters.getValue("currentIntent")] = _currentIntent_
                    if(_wrongIntent_set)
                    map[parameters.getValue("wrongIntent")] = _wrongIntent_
                    if(_averageErrorProbability_set)
                    map[parameters.getValue("averageErrorProbability")] = _averageErrorProbability_
                    if(_count_set)
                    map[parameters.getValue("count")] = _count_
                    if(_total_set)
                    map[parameters.getValue("total")] = _total_
                    if(_firstDetectionDate_set)
                    map[parameters.getValue("firstDetectionDate")] = _firstDetectionDate_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<IntentTestError> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { IntentTestError::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "language" to
                primaryConstructor.findParameterByName("language")!!, "text" to
                primaryConstructor.findParameterByName("text")!!, "currentIntent" to
                primaryConstructor.findParameterByName("currentIntent")!!, "wrongIntent" to
                primaryConstructor.findParameterByName("wrongIntent")!!, "averageErrorProbability"
                to primaryConstructor.findParameterByName("averageErrorProbability")!!, "count" to
                primaryConstructor.findParameterByName("count")!!, "total" to
                primaryConstructor.findParameterByName("total")!!, "firstDetectionDate" to
                primaryConstructor.findParameterByName("firstDetectionDate")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}
    }
}
