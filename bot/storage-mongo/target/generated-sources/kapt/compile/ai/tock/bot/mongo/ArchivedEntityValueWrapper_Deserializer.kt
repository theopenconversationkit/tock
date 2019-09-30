package ai.tock.bot.mongo

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.EntityValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ArchivedEntityValueWrapper_Deserializer :
        JsonDeserializer<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>(),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ArchivedEntityValuesCol.ArchivedEntityValueWrapper {
        with(p) {
            var _entityValue_: EntityValue? = null
            var _entityValue_set : Boolean = false
            var _actionId_: Id<Action>? = null
            var _actionId_set : Boolean = false
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
                    "entityValue" -> {
                            _entityValue_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(EntityValue::class.java);
                            _entityValue_set = true
                            }
                    "actionId" -> {
                            _actionId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_actionId__reference);
                            _actionId_set = true
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
            return if(_entityValue_set && _actionId_set && _date_set)
                    ArchivedEntityValuesCol.ArchivedEntityValueWrapper(entityValue = _entityValue_,
                            actionId = _actionId_, date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_entityValue_set)
                    map[parameters.getValue("entityValue")] = _entityValue_
                    if(_actionId_set)
                    map[parameters.getValue("actionId")] = _actionId_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor:
                KFunction<ArchivedEntityValuesCol.ArchivedEntityValueWrapper> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("entityValue" to
                primaryConstructor.findParameterByName("entityValue")!!, "actionId" to
                primaryConstructor.findParameterByName("actionId")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }

        private val _actionId__reference: TypeReference<Id<Action>> = object :
                TypeReference<Id<Action>>() {}
    }
}
