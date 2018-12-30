package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.dialog.Dialog
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ConnectorMessageColId_Deserializer :
        StdDeserializer<ConnectorMessageColId>(ConnectorMessageColId::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ConnectorMessageColId::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorMessageColId {
        with(p) {
            var _actionId_: Id<Action>? = null
            var _actionId_set = false
            var _dialogId_: Id<Dialog>? = null
            var _dialogId_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "actionId" -> {
                            _actionId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_actionId__reference);
                            _actionId_set = true
                            }
                    "dialogId" -> {
                            _dialogId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_dialogId__reference);
                            _dialogId_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_actionId_set && _dialogId_set)
                    ConnectorMessageColId(actionId = _actionId_!!, dialogId = _dialogId_!!)
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
        private val primaryConstructor: KFunction<ConnectorMessageColId> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ConnectorMessageColId::class.primaryConstructor!! }

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
