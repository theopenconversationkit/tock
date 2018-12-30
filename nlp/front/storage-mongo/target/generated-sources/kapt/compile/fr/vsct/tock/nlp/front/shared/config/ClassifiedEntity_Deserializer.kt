package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class ClassifiedEntity_Deserializer :
        StdDeserializer<ClassifiedEntity>(ClassifiedEntity::class.java), JacksonModuleServiceLoader
        {
    override fun module() = SimpleModule().addDeserializer(ClassifiedEntity::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClassifiedEntity {
        with(p) {
            var _type_: String? = null
            var _type_set = false
            var _role_: String? = null
            var _role_set = false
            var _start_: Int? = null
            var _start_set = false
            var _end_: Int? = null
            var _end_set = false
            var _subEntities_: MutableList<ClassifiedEntity>? = null
            var _subEntities_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "type" -> {
                            _type_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _type_set = true
                            }
                    "role" -> {
                            _role_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _role_set = true
                            }
                    "start" -> {
                            _start_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _start_set = true
                            }
                    "end" -> {
                            _end_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _end_set = true
                            }
                    "subEntities" -> {
                            _subEntities_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_subEntities__reference);
                            _subEntities_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_type_set && _role_set && _start_set && _end_set && _subEntities_set)
                    ClassifiedEntity(type = _type_!!, role = _role_!!, start = _start_!!, end =
                            _end_!!, subEntities = _subEntities_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_type_set)
                    map[parameters.getValue("type")] = _type_
                    if(_role_set)
                    map[parameters.getValue("role")] = _role_
                    if(_start_set)
                    map[parameters.getValue("start")] = _start_
                    if(_end_set)
                    map[parameters.getValue("end")] = _end_
                    if(_subEntities_set)
                    map[parameters.getValue("subEntities")] = _subEntities_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ClassifiedEntity> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ClassifiedEntity::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("type" to primaryConstructor.findParameterByName("type")!!,
                "role" to primaryConstructor.findParameterByName("role")!!, "start" to
                primaryConstructor.findParameterByName("start")!!, "end" to
                primaryConstructor.findParameterByName("end")!!, "subEntities" to
                primaryConstructor.findParameterByName("subEntities")!!) }

        private val _subEntities__reference: TypeReference<List<ClassifiedEntity>> = object :
                TypeReference<List<ClassifiedEntity>>() {}
    }
}
