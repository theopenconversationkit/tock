package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ClassifiedSentenceCol_Deserializer :
        StdDeserializer<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ClassifiedSentenceMongoDAO.ClassifiedSentenceCol {
        with(p) {
            var _text_: String? = null
            var _text_set = false
            var _fullText_: String? = null
            var _fullText_set = false
            var _language_: Locale? = null
            var _language_set = false
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set = false
            var _creationDate_: Instant? = null
            var _creationDate_set = false
            var _updateDate_: Instant? = null
            var _updateDate_set = false
            var _status_: ClassifiedSentenceStatus? = null
            var _status_set = false
            var _classification_: Classification? = null
            var _classification_set = false
            var _lastIntentProbability_: Double? = null
            var _lastIntentProbability_set = false
            var _lastEntityProbability_: Double? = null
            var _lastEntityProbability_set = false
            var _lastUsage_: Instant? = null
            var _lastUsage_set = false
            var _usageCount_: Long? = null
            var _usageCount_set = false
            var _unknownCount_: Long? = null
            var _unknownCount_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "text" -> {
                            _text_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "fullText" -> {
                            _fullText_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _fullText_set = true
                            }
                    "language" -> {
                            _language_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "applicationId" -> {
                            _applicationId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "creationDate" -> {
                            _creationDate_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _creationDate_set = true
                            }
                    "updateDate" -> {
                            _updateDate_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _updateDate_set = true
                            }
                    "status" -> {
                            _status_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ClassifiedSentenceStatus::class.java);
                            _status_set = true
                            }
                    "classification" -> {
                            _classification_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Classification::class.java);
                            _classification_set = true
                            }
                    "lastIntentProbability" -> {
                            _lastIntentProbability_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _lastIntentProbability_set = true
                            }
                    "lastEntityProbability" -> {
                            _lastEntityProbability_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _lastEntityProbability_set = true
                            }
                    "lastUsage" -> {
                            _lastUsage_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUsage_set = true
                            }
                    "usageCount" -> {
                            _usageCount_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.longValue;
                            _usageCount_set = true
                            }
                    "unknownCount" -> {
                            _unknownCount_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.longValue;
                            _unknownCount_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_text_set && _fullText_set && _language_set && _applicationId_set &&
                    _creationDate_set && _updateDate_set && _status_set && _classification_set &&
                    _lastIntentProbability_set && _lastEntityProbability_set && _lastUsage_set &&
                    _usageCount_set && _unknownCount_set)
                    ClassifiedSentenceMongoDAO.ClassifiedSentenceCol(text = _text_!!, fullText =
                            _fullText_!!, language = _language_!!, applicationId =
                            _applicationId_!!, creationDate = _creationDate_!!, updateDate =
                            _updateDate_!!, status = _status_!!, classification =
                            _classification_!!, lastIntentProbability = _lastIntentProbability_,
                            lastEntityProbability = _lastEntityProbability_, lastUsage =
                            _lastUsage_, usageCount = _usageCount_, unknownCount = _unknownCount_)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_text_set)
                    map[parameters.getValue("text")] = _text_
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
                primaryConstructor.findParameterByName("unknownCount")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}
    }
}
