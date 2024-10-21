package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.ZonedDateTime
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class Feature_Deserializer : JsonDeserializer<Feature>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(Feature::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Feature {
        with(p) {
            var __id_: String? = null
            var __id_set : Boolean = false
            var _key_: String? = null
            var _key_set : Boolean = false
            var _enabled_: Boolean? = null
            var _enabled_set : Boolean = false
            var _botId_: String? = null
            var _botId_set : Boolean = false
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _startDate_: ZonedDateTime? = null
            var _startDate_set : Boolean = false
            var _endDate_: ZonedDateTime? = null
            var _endDate_set : Boolean = false
            var _graduation_: Int? = null
            var _graduation_set : Boolean = false
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
                             else p.text;
                            __id_set = true
                            }
                    "key" -> {
                            _key_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _key_set = true
                            }
                    "enabled" -> {
                            _enabled_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _enabled_set = true
                            }
                    "botId" -> {
                            _botId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _botId_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "startDate" -> {
                            _startDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ZonedDateTime::class.java);
                            _startDate_set = true
                            }
                    "endDate" -> {
                            _endDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ZonedDateTime::class.java);
                            _endDate_set = true
                            }
                    "graduation" -> {
                            _graduation_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _graduation_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _key_set && _enabled_set && _botId_set && _namespace_set &&
                    _startDate_set && _endDate_set && _graduation_set)
                    Feature(_id = __id_!!, key = _key_!!, enabled = _enabled_!!, botId = _botId_!!,
                            namespace = _namespace_!!, startDate = _startDate_, endDate = _endDate_,
                            graduation = _graduation_)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_key_set)
                    map[parameters.getValue("key")] = _key_
                    if(_enabled_set)
                    map[parameters.getValue("enabled")] = _enabled_
                    if(_botId_set)
                    map[parameters.getValue("botId")] = _botId_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_startDate_set)
                    map[parameters.getValue("startDate")] = _startDate_
                    if(_endDate_set)
                    map[parameters.getValue("endDate")] = _endDate_
                    if(_graduation_set)
                    map[parameters.getValue("graduation")] = _graduation_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<Feature> by lazy(LazyThreadSafetyMode.PUBLICATION)
                { Feature::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "key" to primaryConstructor.findParameterByName("key")!!, "enabled" to
                primaryConstructor.findParameterByName("enabled")!!, "botId" to
                primaryConstructor.findParameterByName("botId")!!, "namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "startDate" to
                primaryConstructor.findParameterByName("startDate")!!, "endDate" to
                primaryConstructor.findParameterByName("endDate")!!, "graduation" to
                primaryConstructor.findParameterByName("graduation")!!) }
    }
}
