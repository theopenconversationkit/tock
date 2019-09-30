package ai.tock.nlp.model.service.storage.mongo

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpApplicationConfigurationCol_Deserializer :
        JsonDeserializer<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>(),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol {
        with(p) {
            var _applicationName_: String? = null
            var _applicationName_set : Boolean = false
            var _engineType_: NlpEngineType? = null
            var _engineType_set : Boolean = false
            var _configuration_: NlpApplicationConfiguration? = null
            var _configuration_set : Boolean = false
            var _date_: Instant? = null
            var _date_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "applicationName" -> {
                            _applicationName_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _applicationName_set = true
                            }
                    "engineType" -> {
                            _engineType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpEngineType::class.java);
                            _engineType_set = true
                            }
                    "configuration" -> {
                            _configuration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpApplicationConfiguration::class.java);
                            _configuration_set = true
                            }
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_applicationName_set && _engineType_set && _configuration_set && _date_set)
                    NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol(applicationName
                            = _applicationName_!!, engineType = _engineType_!!, configuration =
                            _configuration_!!, date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_applicationName_set)
                    map[parameters.getValue("applicationName")] = _applicationName_
                    if(_engineType_set)
                    map[parameters.getValue("engineType")] = _engineType_
                    if(_configuration_set)
                    map[parameters.getValue("configuration")] = _configuration_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor:
                KFunction<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("applicationName" to
                primaryConstructor.findParameterByName("applicationName")!!, "engineType" to
                primaryConstructor.findParameterByName("engineType")!!, "configuration" to
                primaryConstructor.findParameterByName("configuration")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }
    }
}
