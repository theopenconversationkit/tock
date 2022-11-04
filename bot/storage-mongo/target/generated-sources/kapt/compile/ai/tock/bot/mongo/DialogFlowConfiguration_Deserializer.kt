package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowConfiguration_Deserializer : JsonDeserializer<DialogFlowConfiguration>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogFlowConfiguration::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowConfiguration {
        with(p) {
            var __id_: String? = null
            var __id_set : Boolean = false
            var _currentProcessedLevel_: Int? = null
            var _currentProcessedLevel_set : Boolean = false
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
                             else p.text;
                            __id_set = true
                            }
                    "currentProcessedLevel" -> {
                            _currentProcessedLevel_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _currentProcessedLevel_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _currentProcessedLevel_set)
                    DialogFlowConfiguration(_id = __id_!!, currentProcessedLevel =
                            _currentProcessedLevel_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_currentProcessedLevel_set)
                    map[parameters.getValue("currentProcessedLevel")] = _currentProcessedLevel_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogFlowConfiguration> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogFlowConfiguration::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "currentProcessedLevel" to
                primaryConstructor.findParameterByName("currentProcessedLevel")!!) }
    }
}
