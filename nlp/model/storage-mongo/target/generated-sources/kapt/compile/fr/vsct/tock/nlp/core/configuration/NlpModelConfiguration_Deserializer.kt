package fr.vsct.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Properties
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpModelConfiguration_Deserializer :
        StdDeserializer<NlpModelConfiguration>(NlpModelConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpModelConfiguration::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpModelConfiguration {
        with(p) {
            var _properties_: Properties? = null
            var _properties_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "properties" -> {
                            _properties_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Properties::class.java);
                            _properties_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_properties_set)
                    NlpModelConfiguration(properties = _properties_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_properties_set)
                    map[parameters.getValue("properties")] = _properties_ 
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
                primaryConstructor.findParameterByName("properties")!!) }
    }
}
