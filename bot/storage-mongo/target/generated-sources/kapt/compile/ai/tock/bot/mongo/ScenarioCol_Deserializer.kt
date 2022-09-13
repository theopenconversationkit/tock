package ai.tock.bot.mongo

import ai.tock.bot.admin.scenario.Scenario
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
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

internal class ScenarioCol_Deserializer : JsonDeserializer<ScenarioCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ScenarioCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ScenarioCol {
        with(p) {
            var __id_: Id<Scenario>? = null
            var __id_set : Boolean = false
            var _versions_: MutableList<ScenarioVersionCol>? = null
            var _versions_set : Boolean = false
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
                    "versions" -> {
                            _versions_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_versions__reference);
                            _versions_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _versions_set)
                    ScenarioCol(_id = __id_!!, versions = _versions_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_versions_set)
                    map[parameters.getValue("versions")] = _versions_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ScenarioCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { ScenarioCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "versions" to primaryConstructor.findParameterByName("versions")!!) }

        private val __id__reference: TypeReference<Id<Scenario>> = object :
                TypeReference<Id<Scenario>>() {}

        private val _versions__reference: TypeReference<List<ScenarioVersionCol>> = object :
                TypeReference<List<ScenarioVersionCol>>() {}
    }
}
