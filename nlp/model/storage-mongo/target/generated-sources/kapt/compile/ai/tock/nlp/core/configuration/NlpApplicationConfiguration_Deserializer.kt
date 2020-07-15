package ai.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpApplicationConfiguration_Deserializer :
        JsonDeserializer<NlpApplicationConfiguration>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpApplicationConfiguration::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            NlpApplicationConfiguration {
        with(p) {
            var _tokenizerConfiguration_: NlpModelConfiguration? = null
            var _tokenizerConfiguration_set : Boolean = false
            var _intentConfiguration_: NlpModelConfiguration? = null
            var _intentConfiguration_set : Boolean = false
            var _entityConfiguration_: NlpModelConfiguration? = null
            var _entityConfiguration_set : Boolean = false
            var _applicationConfiguration_: NlpModelConfiguration? = null
            var _applicationConfiguration_set : Boolean = false
            var _hasTokenizerConfiguration_: Boolean? = null
            var _hasTokenizerConfiguration_set : Boolean = false
            var _hasIntentConfiguration_: Boolean? = null
            var _hasIntentConfiguration_set : Boolean = false
            var _hasEntityConfiguration_: Boolean? = null
            var _hasEntityConfiguration_set : Boolean = false
            var _hasApplicationConfiguration_: Boolean? = null
            var _hasApplicationConfiguration_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "tokenizerConfiguration" -> {
                            _tokenizerConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpModelConfiguration::class.java);
                            _tokenizerConfiguration_set = true
                            }
                    "intentConfiguration" -> {
                            _intentConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpModelConfiguration::class.java);
                            _intentConfiguration_set = true
                            }
                    "entityConfiguration" -> {
                            _entityConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpModelConfiguration::class.java);
                            _entityConfiguration_set = true
                            }
                    "applicationConfiguration" -> {
                            _applicationConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpModelConfiguration::class.java);
                            _applicationConfiguration_set = true
                            }
                    "hasTokenizerConfiguration" -> {
                            _hasTokenizerConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _hasTokenizerConfiguration_set = true
                            }
                    "hasIntentConfiguration" -> {
                            _hasIntentConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _hasIntentConfiguration_set = true
                            }
                    "hasEntityConfiguration" -> {
                            _hasEntityConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _hasEntityConfiguration_set = true
                            }
                    "hasApplicationConfiguration" -> {
                            _hasApplicationConfiguration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _hasApplicationConfiguration_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_tokenizerConfiguration_set && _intentConfiguration_set &&
                    _entityConfiguration_set && _applicationConfiguration_set &&
                    _hasTokenizerConfiguration_set && _hasIntentConfiguration_set &&
                    _hasEntityConfiguration_set && _hasApplicationConfiguration_set)
                    NlpApplicationConfiguration(tokenizerConfiguration = _tokenizerConfiguration_!!,
                            intentConfiguration = _intentConfiguration_!!, entityConfiguration =
                            _entityConfiguration_!!, applicationConfiguration =
                            _applicationConfiguration_!!, hasTokenizerConfiguration =
                            _hasTokenizerConfiguration_!!, hasIntentConfiguration =
                            _hasIntentConfiguration_!!, hasEntityConfiguration =
                            _hasEntityConfiguration_!!, hasApplicationConfiguration =
                            _hasApplicationConfiguration_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_tokenizerConfiguration_set)
                    map[parameters.getValue("tokenizerConfiguration")] = _tokenizerConfiguration_
                    if(_intentConfiguration_set)
                    map[parameters.getValue("intentConfiguration")] = _intentConfiguration_
                    if(_entityConfiguration_set)
                    map[parameters.getValue("entityConfiguration")] = _entityConfiguration_
                    if(_applicationConfiguration_set)
                    map[parameters.getValue("applicationConfiguration")] =
                            _applicationConfiguration_
                    if(_hasTokenizerConfiguration_set)
                    map[parameters.getValue("hasTokenizerConfiguration")] =
                            _hasTokenizerConfiguration_
                    if(_hasIntentConfiguration_set)
                    map[parameters.getValue("hasIntentConfiguration")] = _hasIntentConfiguration_
                    if(_hasEntityConfiguration_set)
                    map[parameters.getValue("hasEntityConfiguration")] = _hasEntityConfiguration_
                    if(_hasApplicationConfiguration_set)
                    map[parameters.getValue("hasApplicationConfiguration")] =
                            _hasApplicationConfiguration_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<NlpApplicationConfiguration> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                NlpApplicationConfiguration::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("tokenizerConfiguration" to
                primaryConstructor.findParameterByName("tokenizerConfiguration")!!,
                "intentConfiguration" to
                primaryConstructor.findParameterByName("intentConfiguration")!!,
                "entityConfiguration" to
                primaryConstructor.findParameterByName("entityConfiguration")!!,
                "applicationConfiguration" to
                primaryConstructor.findParameterByName("applicationConfiguration")!!,
                "hasTokenizerConfiguration" to
                primaryConstructor.findParameterByName("hasTokenizerConfiguration")!!,
                "hasIntentConfiguration" to
                primaryConstructor.findParameterByName("hasIntentConfiguration")!!,
                "hasEntityConfiguration" to
                primaryConstructor.findParameterByName("hasEntityConfiguration")!!,
                "hasApplicationConfiguration" to
                primaryConstructor.findParameterByName("hasApplicationConfiguration")!!) }
    }
}
