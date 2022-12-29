package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import java.util.Locale
import kotlin.Boolean
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ClassifiedSentenceCol_Deserializer :
        JsonDeserializer<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>(),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ClassifiedSentenceMongoDAO.ClassifiedSentenceCol {
        with(p) {
            var _text_: String? = null
            var _text_set : Boolean = false
            var _normalizedText_: String? = null
            var _normalizedText_set : Boolean = false
            var _fullText_: String? = null
            var _fullText_set : Boolean = false
            var _language_: Locale? = null
            var _language_set : Boolean = false
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set : Boolean = false
            var _creationDate_: Instant? = null
            var _creationDate_set : Boolean = false
            var _updateDate_: Instant? = null
            var _updateDate_set : Boolean = false
            var _status_: ClassifiedSentenceStatus? = null
            var _status_set : Boolean = false
            var _classification_: Classification? = null
            var _classification_set : Boolean = false
            var _lastIntentProbability_: Double? = null
            var _lastIntentProbability_set : Boolean = false
            var _lastEntityProbability_: Double? = null
            var _lastEntityProbability_set : Boolean = false
            var _lastUsage_: Instant? = null
            var _lastUsage_set : Boolean = false
            var _usageCount_: Long? = null
            var _usageCount_set : Boolean = false
            var _unknownCount_: Long? = null
            var _unknownCount_set : Boolean = false
            var _forReview_: Boolean? = null
            var _forReview_set : Boolean = false
            var _reviewComment_: String? = null
            var _reviewComment_set : Boolean = false
            var _classifier_: String? = null
            var _classifier_set : Boolean = false
            var _otherIntentsProbabilities_: MutableMap<String, Double>? = null
            var _otherIntentsProbabilities_set : Boolean = false
            var _configuration_: String? = null
            var _configuration_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "text" -> {
                            _text_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "normalizedText" -> {
                            _normalizedText_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _normalizedText_set = true
                            }
                    "fullText" -> {
                            _fullText_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _fullText_set = true
                            }
                    "language" -> {
                            _language_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "creationDate" -> {
                            _creationDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _creationDate_set = true
                            }
                    "updateDate" -> {
                            _updateDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _updateDate_set = true
                            }
                    "status" -> {
                            _status_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ClassifiedSentenceStatus::class.java);
                            _status_set = true
                            }
                    "classification" -> {
                            _classification_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Classification::class.java);
                            _classification_set = true
                            }
                    "lastIntentProbability" -> {
                            _lastIntentProbability_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _lastIntentProbability_set = true
                            }
                    "lastEntityProbability" -> {
                            _lastEntityProbability_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _lastEntityProbability_set = true
                            }
                    "lastUsage" -> {
                            _lastUsage_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUsage_set = true
                            }
                    "usageCount" -> {
                            _usageCount_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.longValue;
                            _usageCount_set = true
                            }
                    "unknownCount" -> {
                            _unknownCount_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.longValue;
                            _unknownCount_set = true
                            }
                    "forReview" -> {
                            _forReview_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _forReview_set = true
                            }
                    "reviewComment" -> {
                            _reviewComment_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _reviewComment_set = true
                            }
                    "classifier" -> {
                            _classifier_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _classifier_set = true
                            }
                    "otherIntentsProbabilities" -> {
                            _otherIntentsProbabilities_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_otherIntentsProbabilities__reference);
                            _otherIntentsProbabilities_set = true
                            }
                    "configuration" -> {
                            _configuration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _configuration_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_text_set && _normalizedText_set && _fullText_set && _language_set &&
                    _applicationId_set && _creationDate_set && _updateDate_set && _status_set &&
                    _classification_set && _lastIntentProbability_set && _lastEntityProbability_set
                    && _lastUsage_set && _usageCount_set && _unknownCount_set && _forReview_set &&
                    _reviewComment_set && _classifier_set && _otherIntentsProbabilities_set &&
                    _configuration_set)
                    ClassifiedSentenceMongoDAO.ClassifiedSentenceCol(text = _text_!!, normalizedText
                            = _normalizedText_!!, fullText = _fullText_!!, language = _language_!!,
                            applicationId = _applicationId_!!, creationDate = _creationDate_!!,
                            updateDate = _updateDate_!!, status = _status_!!, classification =
                            _classification_!!, lastIntentProbability = _lastIntentProbability_,
                            lastEntityProbability = _lastEntityProbability_, lastUsage =
                            _lastUsage_, usageCount = _usageCount_, unknownCount = _unknownCount_,
                            forReview = _forReview_!!, reviewComment = _reviewComment_, classifier =
                            _classifier_, otherIntentsProbabilities = _otherIntentsProbabilities_!!,
                            configuration = _configuration_)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_text_set)
                    map[parameters.getValue("text")] = _text_
                    if(_normalizedText_set)
                    map[parameters.getValue("normalizedText")] = _normalizedText_
                    if(_fullText_set)
                    map[parameters.getValue("fullText")] = _fullText_
                    if(_language_set)
                    map[parameters.getValue("language")] = _language_
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_creationDate_set)
                    map[parameters.getValue("creationDate")] = _creationDate_
                    if(_updateDate_set)
                    map[parameters.getValue("updateDate")] = _updateDate_
                    if(_status_set)
                    map[parameters.getValue("status")] = _status_
                    if(_classification_set)
                    map[parameters.getValue("classification")] = _classification_
                    if(_lastIntentProbability_set)
                    map[parameters.getValue("lastIntentProbability")] = _lastIntentProbability_
                    if(_lastEntityProbability_set)
                    map[parameters.getValue("lastEntityProbability")] = _lastEntityProbability_
                    if(_lastUsage_set)
                    map[parameters.getValue("lastUsage")] = _lastUsage_
                    if(_usageCount_set)
                    map[parameters.getValue("usageCount")] = _usageCount_
                    if(_unknownCount_set)
                    map[parameters.getValue("unknownCount")] = _unknownCount_
                    if(_forReview_set)
                    map[parameters.getValue("forReview")] = _forReview_
                    if(_reviewComment_set)
                    map[parameters.getValue("reviewComment")] = _reviewComment_
                    if(_classifier_set)
                    map[parameters.getValue("classifier")] = _classifier_
                    if(_otherIntentsProbabilities_set)
                    map[parameters.getValue("otherIntentsProbabilities")] =
                            _otherIntentsProbabilities_
                    if(_configuration_set)
                    map[parameters.getValue("configuration")] = _configuration_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>
                by lazy(LazyThreadSafetyMode.PUBLICATION) {
                ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("text" to primaryConstructor.findParameterByName("text")!!,
                "normalizedText" to primaryConstructor.findParameterByName("normalizedText")!!,
                "fullText" to primaryConstructor.findParameterByName("fullText")!!, "language" to
                primaryConstructor.findParameterByName("language")!!, "applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "creationDate" to
                primaryConstructor.findParameterByName("creationDate")!!, "updateDate" to
                primaryConstructor.findParameterByName("updateDate")!!, "status" to
                primaryConstructor.findParameterByName("status")!!, "classification" to
                primaryConstructor.findParameterByName("classification")!!, "lastIntentProbability"
                to primaryConstructor.findParameterByName("lastIntentProbability")!!,
                "lastEntityProbability" to
                primaryConstructor.findParameterByName("lastEntityProbability")!!, "lastUsage" to
                primaryConstructor.findParameterByName("lastUsage")!!, "usageCount" to
                primaryConstructor.findParameterByName("usageCount")!!, "unknownCount" to
                primaryConstructor.findParameterByName("unknownCount")!!, "forReview" to
                primaryConstructor.findParameterByName("forReview")!!, "reviewComment" to
                primaryConstructor.findParameterByName("reviewComment")!!, "classifier" to
                primaryConstructor.findParameterByName("classifier")!!, "otherIntentsProbabilities"
                to primaryConstructor.findParameterByName("otherIntentsProbabilities")!!,
                "configuration" to primaryConstructor.findParameterByName("configuration")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}

        private val _otherIntentsProbabilities__reference: TypeReference<Map<String, Double>> =
                object : TypeReference<Map<String, Double>>() {}
    }
}
