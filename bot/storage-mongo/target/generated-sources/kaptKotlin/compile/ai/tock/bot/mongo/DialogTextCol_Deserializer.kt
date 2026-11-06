package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import java.time.Instant
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class DialogTextCol_Deserializer : ValueDeserializer<DialogTextCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogTextCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogTextCol {
    with(p) {
      var _text_: String? = null
      var _text_set: Boolean = false
      var _dialogId_: Id<Dialog>? = null
      var _dialogId_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "text" -> {
            _text_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _text_set = true
          }
          "dialogId" -> {
            _dialogId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_dialogId__reference)
            _dialogId_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _date_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_text_set && _dialogId_set && _date_set) {
        DialogTextCol(text = _text_!!, dialogId = _dialogId_!!, date = _date_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_text_set) map_[parameters.getValue("text")] = _text_
        if (_dialogId_set) map_[parameters.getValue("dialogId")] = _dialogId_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogTextCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogTextCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "text" to primaryConstructor.findParameterByName("text")!!,
            "dialogId" to primaryConstructor.findParameterByName("dialogId")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
          )
        }

    private val _dialogId__reference: TypeReference<Id<Dialog>> =
        object : TypeReference<Id<Dialog>>() {}
  }
}
