package fr.vsct.tock.nlp.front.shared.config

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

internal class EntityDefinition_Deserializer : JsonDeserializer<EntityDefinition>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(EntityDefinition::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EntityDefinition {
        with(p) {
            var _entityTypeName_: String? = null
            var _entityTypeName_set : Boolean = false
            var _role_: String? = null
            var _role_set : Boolean = false
            var _atStartOfDay_: Boolean? = null
            var _atStartOfDay_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "entityTypeName" -> {
                            _entityTypeName_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _entityTypeName_set = true
                            }
                    "role" -> {
                            _role_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _role_set = true
                            }
                    "atStartOfDay" -> {
                            _atStartOfDay_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _atStartOfDay_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_entityTypeName_set && _role_set && _atStartOfDay_set)
                    EntityDefinition(entityTypeName = _entityTypeName_!!, role = _role_!!,
                            atStartOfDay = _atStartOfDay_)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_entityTypeName_set)
                    map[parameters.getValue("entityTypeName")] = _entityTypeName_
                    if(_role_set)
                    map[parameters.getValue("role")] = _role_
                    if(_atStartOfDay_set)
                    map[parameters.getValue("atStartOfDay")] = _atStartOfDay_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<EntityDefinition> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                EntityDefinition::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("entityTypeName" to
                primaryConstructor.findParameterByName("entityTypeName")!!, "role" to
                primaryConstructor.findParameterByName("role")!!, "atStartOfDay" to
                primaryConstructor.findParameterByName("atStartOfDay")!!) }
    }
}
