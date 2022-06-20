package ai.tock.nlp.front.shared.config

import ai.tock.translator.I18nLabel
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class FaqDefinition_Deserializer : JsonDeserializer<FaqDefinition>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(FaqDefinition::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FaqDefinition {
        with(p) {
            var __id_: Id<FaqDefinition>? = null
            var __id_set : Boolean = false
            var _intentId_: Id<IntentDefinition>? = null
            var _intentId_set : Boolean = false
            var _i18nId_: Id<I18nLabel>? = null
            var _i18nId_set : Boolean = false
            var _tags_: MutableList<String>? = null
            var _tags_set : Boolean = false
            var _enabled_: Boolean? = null
            var _enabled_set : Boolean = false
            var _creationDate_: Instant? = null
            var _creationDate_set : Boolean = false
            var _updateDate_: Instant? = null
            var _updateDate_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    "intentId" -> {
                            _intentId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_intentId__reference);
                            _intentId_set = true
                            }
                    "i18nId" -> {
                            _i18nId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_i18nId__reference);
                            _i18nId_set = true
                            }
                    "tags" -> {
                            _tags_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_tags__reference);
                            _tags_set = true
                            }
                    "enabled" -> {
                            _enabled_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _enabled_set = true
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
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _intentId_set && _i18nId_set && _tags_set && _enabled_set &&
                    _creationDate_set && _updateDate_set)
                    FaqDefinition(_id = __id_!!, intentId = _intentId_!!, i18nId = _i18nId_!!, tags
                            = _tags_!!, enabled = _enabled_!!, creationDate = _creationDate_!!,
                            updateDate = _updateDate_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_intentId_set)
                    map[parameters.getValue("intentId")] = _intentId_
                    if(_i18nId_set)
                    map[parameters.getValue("i18nId")] = _i18nId_
                    if(_tags_set)
                    map[parameters.getValue("tags")] = _tags_
                    if(_enabled_set)
                    map[parameters.getValue("enabled")] = _enabled_
                    if(_creationDate_set)
                    map[parameters.getValue("creationDate")] = _creationDate_
                    if(_updateDate_set)
                    map[parameters.getValue("updateDate")] = _updateDate_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<FaqDefinition> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { FaqDefinition::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "intentId" to primaryConstructor.findParameterByName("intentId")!!, "i18nId" to
                primaryConstructor.findParameterByName("i18nId")!!, "tags" to
                primaryConstructor.findParameterByName("tags")!!, "enabled" to
                primaryConstructor.findParameterByName("enabled")!!, "creationDate" to
                primaryConstructor.findParameterByName("creationDate")!!, "updateDate" to
                primaryConstructor.findParameterByName("updateDate")!!) }

        private val __id__reference: TypeReference<Id<FaqDefinition>> = object :
                TypeReference<Id<FaqDefinition>>() {}

        private val _intentId__reference: TypeReference<Id<IntentDefinition>> = object :
                TypeReference<Id<IntentDefinition>>() {}

        private val _i18nId__reference: TypeReference<Id<I18nLabel>> = object :
                TypeReference<Id<I18nLabel>>() {}

        private val _tags__reference: TypeReference<List<String>> = object :
                TypeReference<List<String>>() {}
    }
}
