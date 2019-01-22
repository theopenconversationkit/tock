package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.core.PredefinedValue
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

internal class EntityTypeDefinition_Deserializer : JsonDeserializer<EntityTypeDefinition>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(EntityTypeDefinition::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EntityTypeDefinition {
        with(p) {
            var _name_: String? = null
            var _name_set : Boolean = false
            var _description_: String? = null
            var _description_set : Boolean = false
            var _subEntities_: MutableList<EntityDefinition>? = null
            var _subEntities_set : Boolean = false
            var _predefinedValues_: MutableList<PredefinedValue>? = null
            var _predefinedValues_set : Boolean = false
            var __id_: Id<EntityTypeDefinition>? = null
            var __id_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "name" -> {
                            _name_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _name_set = true
                            }
                    "description" -> {
                            _description_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _description_set = true
                            }
                    "subEntities" -> {
                            _subEntities_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_subEntities__reference);
                            _subEntities_set = true
                            }
                    "predefinedValues" -> {
                            _predefinedValues_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_predefinedValues__reference);
                            _predefinedValues_set = true
                            }
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_name_set && _description_set && _subEntities_set && _predefinedValues_set &&
                    __id_set)
                    EntityTypeDefinition(name = _name_!!, description = _description_!!, subEntities
                            = _subEntities_!!, predefinedValues = _predefinedValues_!!, _id =
                            __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_name_set)
                    map[parameters.getValue("name")] = _name_
                    if(_description_set)
                    map[parameters.getValue("description")] = _description_
                    if(_subEntities_set)
                    map[parameters.getValue("subEntities")] = _subEntities_
                    if(_predefinedValues_set)
                    map[parameters.getValue("predefinedValues")] = _predefinedValues_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<EntityTypeDefinition> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                EntityTypeDefinition::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("name" to primaryConstructor.findParameterByName("name")!!,
                "description" to primaryConstructor.findParameterByName("description")!!,
                "subEntities" to primaryConstructor.findParameterByName("subEntities")!!,
                "predefinedValues" to primaryConstructor.findParameterByName("predefinedValues")!!,
                "_id" to primaryConstructor.findParameterByName("_id")!!) }

        private val _subEntities__reference: TypeReference<List<EntityDefinition>> = object :
                TypeReference<List<EntityDefinition>>() {}

        private val _predefinedValues__reference: TypeReference<List<PredefinedValue>> = object :
                TypeReference<List<PredefinedValue>>() {}

        private val __id__reference: TypeReference<Id<EntityTypeDefinition>> = object :
                TypeReference<Id<EntityTypeDefinition>>() {}
    }
}
