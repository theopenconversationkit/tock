package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ClientIdCol_Deserializer : JsonDeserializer<ClientIdCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ClientIdCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClientIdCol {
        with(p) {
            var _userIds_: MutableSet<String>? = null
            var _userIds_set : Boolean = false
            var __id_: Id<ClientIdCol>? = null
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
                    "userIds" -> {
                            _userIds_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_userIds__reference);
                            _userIds_set = true
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
            return if(_userIds_set && __id_set)
                    ClientIdCol(userIds = _userIds_!!, _id = __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_userIds_set)
                    map[parameters.getValue("userIds")] = _userIds_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ClientIdCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { ClientIdCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("userIds" to
                primaryConstructor.findParameterByName("userIds")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!) }

        private val _userIds__reference: TypeReference<Set<String>> = object :
                TypeReference<Set<String>>() {}

        private val __id__reference: TypeReference<Id<ClientIdCol>> = object :
                TypeReference<Id<ClientIdCol>>() {}
    }
}
