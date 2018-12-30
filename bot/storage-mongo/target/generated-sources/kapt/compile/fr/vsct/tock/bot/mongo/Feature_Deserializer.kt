package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class Feature_Deserializer : StdDeserializer<Feature>(Feature::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(Feature::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Feature {
        with(p) {
            var __id_: String? = null
            var __id_set = false
            var _key_: String? = null
            var _key_set = false
            var _enabled_: Boolean? = null
            var _enabled_set = false
            var _botId_: String? = null
            var _botId_set = false
            var _namespace_: String? = null
            var _namespace_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "_id" -> {
                            __id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            __id_set = true
                            }
                    "key" -> {
                            _key_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _key_set = true
                            }
                    "enabled" -> {
                            _enabled_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Boolean::class.java);
                            _enabled_set = true
                            }
                    "botId" -> {
                            _botId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _botId_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(__id_set && _key_set && _enabled_set && _botId_set && _namespace_set)
                    Feature(_id = __id_!!, key = _key_!!, enabled = _enabled_!!, botId = _botId_!!,
                            namespace = _namespace_!!)
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
                primaryConstructor.findParameterByName("namespace")!!) }
    }
}
