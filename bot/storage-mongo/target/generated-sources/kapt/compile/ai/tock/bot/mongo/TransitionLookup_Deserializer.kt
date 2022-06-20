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

internal class TransitionLookup_Deserializer : JsonDeserializer<TransitionLookup>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TransitionLookup::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TransitionLookup {
        with(p) {
            var _transition_: DialogFlowStateTransitionCol? = null
            var _transition_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "transition" -> {
                            _transition_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(DialogFlowStateTransitionCol::class.java);
                            _transition_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_transition_set)
                    TransitionLookup(transition = _transition_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_transition_set)
                    map[parameters.getValue("transition")] = _transition_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<TransitionLookup> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                TransitionLookup::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("transition" to
                primaryConstructor.findParameterByName("transition")!!) }
    }
}
