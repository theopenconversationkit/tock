package fr.vsct.tock.nlp.front.shared.test

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
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

internal class IntentTestError_Deserializer :
        StdDeserializer<IntentTestError>(IntentTestError::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(IntentTestError::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntentTestError {
        with(p) {
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set = false
            var _language_: Locale? = null
            var _language_set = false
            var _text_: String? = null
            var _text_set = false
            var _currentIntent_: String? = null
            var _currentIntent_set = false
            var _wrongIntent_: String? = null
            var _wrongIntent_set = false
            var _averageErrorProbability_: Double? = null
            var _averageErrorProbability_set = false
            var _count_: Int? = null
            var _count_set = false
            var _total_: Int? = null
            var _total_set = false
            var _firstDetectionDate_: Instant? = null
            var _firstDetectionDate_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "applicationId" -> {
                            _applicationId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "language" -> {
                            _language_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "text" -> {
                            _text_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "currentIntent" -> {
                            _currentIntent_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _currentIntent_set = true
                            }
                    "wrongIntent" -> {
                            _wrongIntent_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _wrongIntent_set = true
                            }
                    "averageErrorProbability" -> {
                            _averageErrorProbability_ = if(currentToken == JsonToken.VALUE_NULL)
                                    null
                             else p.readValueAs(Double::class.java);
                            _averageErrorProbability_set = true
                            }
                    "count" -> {
                            _count_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _count_set = true
                            }
                    "total" -> {
                            _total_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _total_set = true
                            }
                    "firstDetectionDate" -> {
                            _firstDetectionDate_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _firstDetectionDate_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
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
