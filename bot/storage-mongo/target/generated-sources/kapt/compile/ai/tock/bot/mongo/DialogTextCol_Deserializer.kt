package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
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

internal class DialogTextCol_Deserializer : JsonDeserializer<DialogTextCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogTextCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogTextCol {
        with(p) {
            var _text_: String? = null
            var _text_set : Boolean = false
            var _dialogId_: Id<Dialog>? = null
            var _dialogId_set : Boolean = false
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
                    "text" -> {
                            _text_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "dialogId" -> {
                            _dialogId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_dialogId__reference);
                            _dialogId_set = true
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
            return if(_text_set && _dialogId_set && _date_set)
                    DialogTextCol(text = _text_!!, dialogId = _dialogId_!!, date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_text_set)
                    map[parameters.getValue("text")] = _text_
                    if(_dialogId_set)
                    map[parameters.getValue("dialogId")] = _dialogId_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogTextCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { DialogTextCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("text" to primaryConstructor.findParameterByName("text")!!,
                "dialogId" to primaryConstructor.findParameterByName("dialogId")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }

        private val _dialogId__reference: TypeReference<Id<Dialog>> = object :
                TypeReference<Id<Dialog>>() {}
    }
}
