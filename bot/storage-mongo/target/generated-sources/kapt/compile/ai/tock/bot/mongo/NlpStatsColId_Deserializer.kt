package ai.tock.bot.mongo

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
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
import org.litote.kmongo.Id

internal class NlpStatsColId_Deserializer : JsonDeserializer<NlpStatsColId>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpStatsColId::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpStatsColId {
        with(p) {
            var _actionId_: Id<Action>? = null
            var _actionId_set : Boolean = false
            var _dialogId_: Id<Dialog>? = null
            var _dialogId_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "actionId" -> {
                            _actionId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_actionId__reference);
                            _actionId_set = true
                            }
                    "dialogId" -> {
                            _dialogId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_dialogId__reference);
                            _dialogId_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_actionId_set && _dialogId_set)
                    NlpStatsColId(actionId = _actionId_!!, dialogId = _dialogId_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_actionId_set)
                    map[parameters.getValue("actionId")] = _actionId_
                    if(_dialogId_set)
                    map[parameters.getValue("dialogId")] = _dialogId_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<NlpStatsColId> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { NlpStatsColId::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("actionId" to
                primaryConstructor.findParameterByName("actionId")!!, "dialogId" to
                primaryConstructor.findParameterByName("dialogId")!!) }

        private val _actionId__reference: TypeReference<Id<Action>> = object :
                TypeReference<Id<Action>>() {}

        private val _dialogId__reference: TypeReference<Id<Dialog>> = object :
                TypeReference<Id<Dialog>>() {}
    }
}
