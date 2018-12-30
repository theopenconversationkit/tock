package fr.vsct.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpApplicationConfiguration_Deserializer :
        StdDeserializer<NlpApplicationConfiguration>(NlpApplicationConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpApplicationConfiguration::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            NlpApplicationConfiguration {
        with(p) {
            var _tokenizerConfiguration_: NlpModelConfiguration? = null
            var _tokenizerConfiguration_set = false
            var _intentConfiguration_: NlpModelConfiguration? = null
            var _intentConfiguration_set = false
            var _entityConfiguration_: NlpModelConfiguration? = null
            var _entityConfiguration_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "tokenizerConfiguration" -> {
                            _tokenizerConfiguration_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpModelConfiguration::class.java);
                            _tokenizerConfiguration_set = true
                            }
                    "intentConfiguration" -> {
                            _intentConfiguration_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpModelConfiguration::class.java);
                            _intentConfiguration_set = true
                            }
                    "entityConfiguration" -> {
                            _entityConfiguration_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(NlpModelConfiguration::class.java);
                            _entityConfiguration_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_tokenizerConfiguration_set && _intentConfiguration_set &&
                    _entityConfiguration_set)
                    NlpApplicationConfiguration(tokenizerConfiguration = _tokenizerConfiguration_!!,
                            intentConfiguration = _intentConfiguration_!!, entityConfiguration =
                            _entityConfiguration_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_tokenizerConfiguration_set)
                    map[parameters.getValue("tokenizerConfiguration")] = _tokenizerConfiguration_
                    if(_intentConfiguration_set)
                    map[parameters.getValue("intentConfiguration")] = _intentConfiguration_
                    if(_entityConfiguration_set)
                    map[parameters.getValue("entityConfiguration")] = _entityConfiguration_ 
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
                primaryConstructor.findParameterByName("entityConfiguration")!!) }
    }
}
