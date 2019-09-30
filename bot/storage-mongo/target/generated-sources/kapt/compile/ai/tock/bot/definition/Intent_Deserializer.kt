package ai.tock.bot.definition

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

internal class Intent_Deserializer : JsonDeserializer<Intent>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(Intent::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Intent {
        with(p) {
            var _name_: String? = null
            var _name_set : Boolean = false
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
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_name_set)
                    Intent(name = _name_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_name_set)
                    map[parameters.getValue("name")] = _name_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<Intent> by lazy(LazyThreadSafetyMode.PUBLICATION)
                { Intent::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("name" to primaryConstructor.findParameterByName("name")!!)
                }
    }
}
