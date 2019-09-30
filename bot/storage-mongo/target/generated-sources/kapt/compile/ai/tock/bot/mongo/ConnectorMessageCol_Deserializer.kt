package ai.tock.bot.mongo

import ai.tock.shared.jackson.AnyValueWrapper
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class ConnectorMessageCol_Deserializer : JsonDeserializer<ConnectorMessageCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ConnectorMessageCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorMessageCol {
        with(p) {
            var __id_: ConnectorMessageColId? = null
            var __id_set : Boolean = false
            var _messages_: MutableList<AnyValueWrapper>? = null
            var _messages_set : Boolean = false
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
                             else p.readValueAs(ConnectorMessageColId::class.java);
                            __id_set = true
                            }
                    "messages" -> {
                            _messages_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_messages__reference);
                            _messages_set = true
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
            return if(__id_set && _messages_set && _date_set)
                    ConnectorMessageCol(_id = __id_!!, messages = _messages_!!, date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_messages_set)
                    map[parameters.getValue("messages")] = _messages_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ConnectorMessageCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ConnectorMessageCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "messages" to primaryConstructor.findParameterByName("messages")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }

        private val _messages__reference: TypeReference<List<AnyValueWrapper>> = object :
                TypeReference<List<AnyValueWrapper>>() {}
    }
}
