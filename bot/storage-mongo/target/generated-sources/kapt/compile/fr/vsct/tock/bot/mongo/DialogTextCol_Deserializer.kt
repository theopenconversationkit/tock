package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.dialog.Dialog
import java.time.Instant
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class DialogTextCol_Deserializer :
        StdDeserializer<DialogTextCol>(DialogTextCol::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogTextCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogTextCol {
        with(p) {
            var _text_: String? = null
            var _text_set = false
            var _dialogId_: Id<Dialog>? = null
            var _dialogId_set = false
            var _date_: Instant? = null
            var _date_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "text" -> {
                            _text_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "dialogId" -> {
                            _dialogId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_dialogId__reference);
                            _dialogId_set = true
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
