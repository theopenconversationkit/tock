package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.dialog.Dialog
import java.time.Instant
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
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

internal class DialogFlowStateTransitionStatCol_Deserializer : ValueDeserializer<DialogFlowStateTransitionStatCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogFlowStateTransitionStatCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowStateTransitionStatCol {
    with(p) {
      var _applicationId_: Id<BotApplicationConfiguration>? = null
      var _applicationId_set: Boolean = false
      var _transitionId_: Id<DialogFlowStateTransitionCol>? = null
      var _transitionId_set: Boolean = false
      var _dialogId_: Id<Dialog>? = null
      var _dialogId_set: Boolean = false
      var _text_: String? = null
      var _text_set: Boolean = false
      var _locale_: Locale? = null
      var _locale_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var _processedLevel_: Int? = null
      var _processedLevel_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "applicationId" -> {
            _applicationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationId__reference)
            _applicationId_set = true
          }
          "transitionId" -> {
            _transitionId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_transitionId__reference)
            _transitionId_set = true
          }
          "dialogId" -> {
            _dialogId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_dialogId__reference)
            _dialogId_set = true
          }
          "text" -> {
            _text_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _text_set = true
          }
          "locale" -> {
            _locale_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _locale_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _date_set = true
          }
          "processedLevel" -> {
            _processedLevel_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _processedLevel_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_applicationId_set && _transitionId_set && _dialogId_set && _text_set && _locale_set && _date_set && _processedLevel_set) {
        DialogFlowStateTransitionStatCol(applicationId = _applicationId_!!, transitionId = _transitionId_!!, dialogId = _dialogId_!!, text = _text_, locale = _locale_, date = _date_!!, processedLevel = _processedLevel_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_transitionId_set) map_[parameters.getValue("transitionId")] = _transitionId_
        if (_dialogId_set) map_[parameters.getValue("dialogId")] = _dialogId_
        if (_text_set) map_[parameters.getValue("text")] = _text_
        if (_locale_set) map_[parameters.getValue("locale")] = _locale_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        if (_processedLevel_set) map_[parameters.getValue("processedLevel")] = _processedLevel_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogFlowStateTransitionStatCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogFlowStateTransitionStatCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "transitionId" to primaryConstructor.findParameterByName("transitionId")!!,
            "dialogId" to primaryConstructor.findParameterByName("dialogId")!!,
            "text" to primaryConstructor.findParameterByName("text")!!,
            "locale" to primaryConstructor.findParameterByName("locale")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
            "processedLevel" to primaryConstructor.findParameterByName("processedLevel")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<BotApplicationConfiguration>> =
        object : TypeReference<Id<BotApplicationConfiguration>>() {}

    private val _transitionId__reference: TypeReference<Id<DialogFlowStateTransitionCol>> =
        object : TypeReference<Id<DialogFlowStateTransitionCol>>() {}

    private val _dialogId__reference: TypeReference<Id<Dialog>> =
        object : TypeReference<Id<Dialog>>() {}
  }
}
