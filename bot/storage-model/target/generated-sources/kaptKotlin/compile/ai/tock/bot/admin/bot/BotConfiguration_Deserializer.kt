package ai.tock.bot.admin.bot

import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class BotConfiguration_Deserializer : ValueDeserializer<BotConfiguration>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(BotConfiguration::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BotConfiguration {
    with(p) {
      var _name_: String? = null
      var _name_set: Boolean = false
      var _botId_: String? = null
      var _botId_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _nlpModel_: String? = null
      var _nlpModel_set: Boolean = false
      var _apiKey_: String? = null
      var _apiKey_set: Boolean = false
      var _webhookUrl_: String? = null
      var _webhookUrl_set: Boolean = false
      var _supportedLocales_: Set<Locale>? = null
      var _supportedLocales_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "name" -> {
            _name_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _name_set = true
          }
          "botId" -> {
            _botId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _botId_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "nlpModel" -> {
            _nlpModel_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _nlpModel_set = true
          }
          "apiKey" -> {
            _apiKey_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _apiKey_set = true
          }
          "webhookUrl" -> {
            _webhookUrl_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _webhookUrl_set = true
          }
          "supportedLocales" -> {
            _supportedLocales_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_supportedLocales__reference)
            _supportedLocales_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_name_set && _botId_set && _namespace_set && _nlpModel_set && _apiKey_set && _webhookUrl_set && _supportedLocales_set) {
        BotConfiguration(name = _name_!!, botId = _botId_!!, namespace = _namespace_!!, nlpModel = _nlpModel_!!, apiKey = _apiKey_!!, webhookUrl = _webhookUrl_, supportedLocales = _supportedLocales_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_name_set) map_[parameters.getValue("name")] = _name_
        if (_botId_set) map_[parameters.getValue("botId")] = _botId_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_nlpModel_set) map_[parameters.getValue("nlpModel")] = _nlpModel_
        if (_apiKey_set) map_[parameters.getValue("apiKey")] = _apiKey_
        if (_webhookUrl_set) map_[parameters.getValue("webhookUrl")] = _webhookUrl_
        if (_supportedLocales_set) map_[parameters.getValue("supportedLocales")] = _supportedLocales_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<BotConfiguration> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { BotConfiguration::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "name" to primaryConstructor.findParameterByName("name")!!,
            "botId" to primaryConstructor.findParameterByName("botId")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "nlpModel" to primaryConstructor.findParameterByName("nlpModel")!!,
            "apiKey" to primaryConstructor.findParameterByName("apiKey")!!,
            "webhookUrl" to primaryConstructor.findParameterByName("webhookUrl")!!,
            "supportedLocales" to primaryConstructor.findParameterByName("supportedLocales")!!,
          )
        }

    private val _supportedLocales__reference: TypeReference<Set<Locale>> =
        object : TypeReference<Set<Locale>>() {}
  }
}
