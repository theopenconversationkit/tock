package ai.tock.translator

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

internal class I18nLabelStat_Deserializer : ValueDeserializer<I18nLabelStat>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(I18nLabelStat::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): I18nLabelStat {
    with(p) {
      var _labelId_: Id<I18nLabel>? = null
      var _labelId_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _locale_: Locale? = null
      var _locale_set: Boolean = false
      var _interfaceType_: UserInterfaceType? = null
      var _interfaceType_set: Boolean = false
      var _connectorId_: String? = null
      var _connectorId_set: Boolean = false
      var _count_: Int? = null
      var _count_set: Boolean = false
      var _lastUpdate_: Instant? = null
      var _lastUpdate_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "labelId" -> {
            _labelId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_labelId__reference)
            _labelId_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "locale" -> {
            _locale_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _locale_set = true
          }
          "interfaceType" -> {
            _interfaceType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(UserInterfaceType::class.javaObjectType)
            _interfaceType_set = true
          }
          "connectorId" -> {
            _connectorId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _connectorId_set = true
          }
          "count" -> {
            _count_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _count_set = true
          }
          "lastUpdate" -> {
            _lastUpdate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _lastUpdate_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_labelId_set && _namespace_set && _locale_set && _interfaceType_set && _connectorId_set && _count_set && _lastUpdate_set) {
        I18nLabelStat(labelId = _labelId_!!, namespace = _namespace_!!, locale = _locale_!!, interfaceType = _interfaceType_!!, connectorId = _connectorId_, count = _count_!!, lastUpdate = _lastUpdate_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_labelId_set) map_[parameters.getValue("labelId")] = _labelId_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_locale_set) map_[parameters.getValue("locale")] = _locale_
        if (_interfaceType_set) map_[parameters.getValue("interfaceType")] = _interfaceType_
        if (_connectorId_set) map_[parameters.getValue("connectorId")] = _connectorId_
        if (_count_set) map_[parameters.getValue("count")] = _count_
        if (_lastUpdate_set) map_[parameters.getValue("lastUpdate")] = _lastUpdate_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<I18nLabelStat> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { I18nLabelStat::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "labelId" to primaryConstructor.findParameterByName("labelId")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "locale" to primaryConstructor.findParameterByName("locale")!!,
            "interfaceType" to primaryConstructor.findParameterByName("interfaceType")!!,
            "connectorId" to primaryConstructor.findParameterByName("connectorId")!!,
            "count" to primaryConstructor.findParameterByName("count")!!,
            "lastUpdate" to primaryConstructor.findParameterByName("lastUpdate")!!,
          )
        }

    private val _labelId__reference: TypeReference<Id<I18nLabel>> =
        object : TypeReference<Id<I18nLabel>>() {}
  }
}
