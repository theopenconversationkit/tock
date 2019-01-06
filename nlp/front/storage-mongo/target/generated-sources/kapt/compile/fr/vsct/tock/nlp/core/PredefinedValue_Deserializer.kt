package fr.vsct.tock.nlp.core

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Locale
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class PredefinedValue_Deserializer :
        StdDeserializer<PredefinedValue>(PredefinedValue::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(PredefinedValue::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PredefinedValue {
        with(p) {
            var _value_: String? = null
            var _value_set = false
            var _labels_: MutableMap<Locale, List<String>>? = null
            var _labels_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "value" -> {
                            _value_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _value_set = true
                            }
                    "labels" -> {
                            _labels_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_labels__reference);
                            _labels_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_value_set && _labels_set)
                    PredefinedValue(value = _value_!!, labels = _labels_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_value_set)
                    map[parameters.getValue("value")] = _value_
                    if(_labels_set)
                    map[parameters.getValue("labels")] = _labels_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<PredefinedValue> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { PredefinedValue::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("value" to
                primaryConstructor.findParameterByName("value")!!, "labels" to
                primaryConstructor.findParameterByName("labels")!!) }

        private val _labels__reference: TypeReference<Map<Locale, List<String>>> = object :
                TypeReference<Map<Locale, List<String>>>() {}
    }
}
