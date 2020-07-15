package ai.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Properties
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpModelConfiguration_Deserializer : JsonDeserializer<NlpModelConfiguration>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpModelConfiguration::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpModelConfiguration {
        with(p) {
            var _properties_: Properties? = null
            var _properties_set : Boolean = false
            var _markdown_: String? = null
            var _markdown_set : Boolean = false
            var _hasProperties_: Boolean? = null
            var _hasProperties_set : Boolean = false
            var _hasMarkdown_: Boolean? = null
            var _hasMarkdown_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "properties" -> {
                            _properties_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Properties::class.java);
                            _properties_set = true
                            }
                    "markdown" -> {
                            _markdown_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _markdown_set = true
                            }
                    "hasProperties" -> {
                            _hasProperties_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _hasProperties_set = true
                            }
                    "hasMarkdown" -> {
                            _hasMarkdown_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _hasMarkdown_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_properties_set && _markdown_set && _hasProperties_set && _hasMarkdown_set)
                    NlpModelConfiguration(properties = _properties_!!, markdown = _markdown_,
                            hasProperties = _hasProperties_!!, hasMarkdown = _hasMarkdown_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_properties_set)
                    map[parameters.getValue("properties")] = _properties_
                    if(_markdown_set)
                    map[parameters.getValue("markdown")] = _markdown_
                    if(_hasProperties_set)
                    map[parameters.getValue("hasProperties")] = _hasProperties_
                    if(_hasMarkdown_set)
                    map[parameters.getValue("hasMarkdown")] = _hasMarkdown_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<NlpModelConfiguration> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                NlpModelConfiguration::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("properties" to
                primaryConstructor.findParameterByName("properties")!!, "markdown" to
                primaryConstructor.findParameterByName("markdown")!!, "hasProperties" to
                primaryConstructor.findParameterByName("hasProperties")!!, "hasMarkdown" to
                primaryConstructor.findParameterByName("hasMarkdown")!!) }
    }
}
