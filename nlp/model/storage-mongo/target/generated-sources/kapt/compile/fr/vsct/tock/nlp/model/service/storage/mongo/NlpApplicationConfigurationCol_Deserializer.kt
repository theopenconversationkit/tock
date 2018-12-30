package fr.vsct.tock.nlp.model.service.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.configuration.NlpApplicationConfiguration
import java.time.Instant
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpApplicationConfigurationCol_Deserializer :
        StdDeserializer<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol {
        with(p) {
            var _applicationName_: String? = null
            var _applicationName_set = false
            var _engineType_: NlpEngineType? = null
            var _engineType_set = false
            var _configuration_: NlpApplicationConfiguration? = null
            var _configuration_set = false
            var _date_: Instant? = null
            var _date_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "applicationName" -> {
                            _applicationName_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _applicationName_set = true
                            }
                    "engineType" -> {
                            _engineType_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpEngineType::class.java);
                            _engineType_set = true
                            }
                    "configuration" -> {
                            _configuration_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpApplicationConfiguration::class.java);
                            _configuration_set = true
                            }
                    "date" -> {
                            _date_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
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
