package ai.tock.bot.connector

import ai.tock.translator.UserInterfaceType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class ConnectorType_Deserializer : JsonDeserializer<ConnectorType>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ConnectorType::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorType {
        with(p) {
            var _id_: String? = null
            var _id_set : Boolean = false
            var _userInterfaceType_: UserInterfaceType? = null
            var _userInterfaceType_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "id" -> {
                            _id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _id_set = true
                            }
                    "userInterfaceType" -> {
                            _userInterfaceType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(UserInterfaceType::class.java);
                            _userInterfaceType_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_id_set && _userInterfaceType_set)
                    ConnectorType(id = _id_!!, userInterfaceType = _userInterfaceType_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_id_set)
                    map[parameters.getValue("id")] = _id_
                    if(_userInterfaceType_set)
                    map[parameters.getValue("userInterfaceType")] = _userInterfaceType_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ConnectorType> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { ConnectorType::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("id" to primaryConstructor.findParameterByName("id")!!,
                "userInterfaceType" to
                primaryConstructor.findParameterByName("userInterfaceType")!!) }
    }
}
