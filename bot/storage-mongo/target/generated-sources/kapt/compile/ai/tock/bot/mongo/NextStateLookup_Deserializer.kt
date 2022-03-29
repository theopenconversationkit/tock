package ai.tock.bot.mongo

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

internal class NextStateLookup_Deserializer : JsonDeserializer<NextStateLookup>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NextStateLookup::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NextStateLookup {
        with(p) {
            var _nextState_: DialogFlowStateCol? = null
            var _nextState_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "nextState" -> {
                            _nextState_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(DialogFlowStateCol::class.java);
                            _nextState_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_nextState_set)
                    NextStateLookup(nextState = _nextState_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_nextState_set)
                    map[parameters.getValue("nextState")] = _nextState_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<NextStateLookup> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { NextStateLookup::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("nextState" to
                primaryConstructor.findParameterByName("nextState")!!) }
    }
}
