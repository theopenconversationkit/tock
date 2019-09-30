package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class UserLock_Deserializer : JsonDeserializer<MongoUserLock.UserLock>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(MongoUserLock.UserLock::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MongoUserLock.UserLock {
        with(p) {
            var __id_: Id<MongoUserLock.UserLock>? = null
            var __id_set : Boolean = false
            var _locked_: Boolean? = null
            var _locked_set : Boolean = false
            var _date_: Instant? = null
            var _date_set : Boolean = false
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
                    "locked" -> {
                            _locked_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _locked_set = true
                            }
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _locked_set && _date_set)
                    MongoUserLock.UserLock(_id = __id_!!, locked = _locked_!!, date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_locked_set)
                    map[parameters.getValue("locked")] = _locked_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<MongoUserLock.UserLock> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                MongoUserLock.UserLock::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "locked" to primaryConstructor.findParameterByName("locked")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }

        private val __id__reference: TypeReference<Id<MongoUserLock.UserLock>> = object :
                TypeReference<Id<MongoUserLock.UserLock>>() {}
    }
}
