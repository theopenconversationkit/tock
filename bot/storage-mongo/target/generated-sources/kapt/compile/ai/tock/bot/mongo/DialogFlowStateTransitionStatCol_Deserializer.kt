package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.dialog.Dialog
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class DialogFlowStateTransitionStatCol_Deserializer :
        JsonDeserializer<DialogFlowStateTransitionStatCol>(), JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(DialogFlowStateTransitionStatCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            DialogFlowStateTransitionStatCol {
        with(p) {
            var _applicationId_: Id<BotApplicationConfiguration>? = null
            var _applicationId_set : Boolean = false
            var _transitionId_: Id<DialogFlowStateTransitionCol>? = null
            var _transitionId_set : Boolean = false
            var _dialogId_: Id<Dialog>? = null
            var _dialogId_set : Boolean = false
            var _text_: String? = null
            var _text_set : Boolean = false
            var _locale_: Locale? = null
            var _locale_set : Boolean = false
            var _date_: Instant? = null
            var _date_set : Boolean = false
            var _processedLevel_: Int? = null
            var _processedLevel_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "transitionId" -> {
                            _transitionId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_transitionId__reference);
                            _transitionId_set = true
                            }
                    "dialogId" -> {
                            _dialogId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_dialogId__reference);
                            _dialogId_set = true
                            }
                    "text" -> {
                            _text_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "locale" -> {
                            _locale_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _locale_set = true
                            }
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    "processedLevel" -> {
                            _processedLevel_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _processedLevel_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_applicationId_set && _transitionId_set && _dialogId_set && _text_set &&
                    _locale_set && _date_set && _processedLevel_set)
                    DialogFlowStateTransitionStatCol(applicationId = _applicationId_!!, transitionId
                            = _transitionId_!!, dialogId = _dialogId_!!, text = _text_, locale =
                            _locale_, date = _date_!!, processedLevel = _processedLevel_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_transitionId_set)
                    map[parameters.getValue("transitionId")] = _transitionId_
                    if(_dialogId_set)
                    map[parameters.getValue("dialogId")] = _dialogId_
                    if(_text_set)
                    map[parameters.getValue("text")] = _text_
                    if(_locale_set)
                    map[parameters.getValue("locale")] = _locale_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_
                    if(_processedLevel_set)
                    map[parameters.getValue("processedLevel")] = _processedLevel_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogFlowStateTransitionStatCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogFlowStateTransitionStatCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "transitionId" to
                primaryConstructor.findParameterByName("transitionId")!!, "dialogId" to
                primaryConstructor.findParameterByName("dialogId")!!, "text" to
                primaryConstructor.findParameterByName("text")!!, "locale" to
                primaryConstructor.findParameterByName("locale")!!, "date" to
                primaryConstructor.findParameterByName("date")!!, "processedLevel" to
                primaryConstructor.findParameterByName("processedLevel")!!) }

        private val _applicationId__reference: TypeReference<Id<BotApplicationConfiguration>> =
                object : TypeReference<Id<BotApplicationConfiguration>>() {}

        private val _transitionId__reference: TypeReference<Id<DialogFlowStateTransitionCol>> =
                object : TypeReference<Id<DialogFlowStateTransitionCol>>() {}

        private val _dialogId__reference: TypeReference<Id<Dialog>> = object :
                TypeReference<Id<Dialog>>() {}
    }
}
