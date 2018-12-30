package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.shared.jackson.AnyValueWrapper
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

internal class ConnectorMessageCol_Deserializer :
        StdDeserializer<ConnectorMessageCol>(ConnectorMessageCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ConnectorMessageCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorMessageCol {
        with(p) {
            var __id_: ConnectorMessageColId? = null
            var __id_set = false
            var _messages_: MutableList<AnyValueWrapper>? = null
            var _messages_set = false
            var _date_: Instant? = null
            var _date_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "_id" -> {
                            __id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ConnectorMessageColId::class.java);
                            __id_set = true
                            }
                    "messages" -> {
                            _messages_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_messages__reference);
                            _messages_set = true
                            }
                    "date" -> {
                            _date_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
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
