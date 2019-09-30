package ai.tock.bot.engine.user

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

internal class PlayerId_Deserializer : JsonDeserializer<PlayerId>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(PlayerId::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PlayerId {
        with(p) {
            var _id_: String? = null
            var _id_set : Boolean = false
            var _type_: PlayerType? = null
            var _type_set : Boolean = false
            var _clientId_: String? = null
            var _clientId_set : Boolean = false
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
                    "type" -> {
                            _type_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(PlayerType::class.java);
                            _type_set = true
                            }
                    "clientId" -> {
                            _clientId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _clientId_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_id_set && _type_set && _clientId_set)
                    PlayerId(id = _id_!!, type = _type_!!, clientId = _clientId_)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_id_set)
                    map[parameters.getValue("id")] = _id_
                    if(_type_set)
                    map[parameters.getValue("type")] = _type_
                    if(_clientId_set)
                    map[parameters.getValue("clientId")] = _clientId_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<PlayerId> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { PlayerId::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("id" to primaryConstructor.findParameterByName("id")!!,
                "type" to primaryConstructor.findParameterByName("type")!!, "clientId" to
                primaryConstructor.findParameterByName("clientId")!!) }
    }
}
