package ai.tock.translator

import java.util.LinkedHashSet
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.Set
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

internal class I18nLabel_Deserializer : ValueDeserializer<I18nLabel>(), JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(I18nLabel::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): I18nLabel {
    with(p) {
      var __id_: Id<I18nLabel>? = null
      var __id_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _category_: String? = null
      var _category_set: Boolean = false
      var _i18n_: LinkedHashSet<I18nLocalizedLabel>? = null
      var _i18n_set: Boolean = false
      var _defaultLabel_: String? = null
      var _defaultLabel_set: Boolean = false
      var _defaultLocale_: Locale? = null
      var _defaultLocale_set: Boolean = false
      var _defaultI18n_: Set<I18nLocalizedLabel>? = null
      var _defaultI18n_set: Boolean = false
      var _version_: Int? = null
      var _version_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "category" -> {
            _category_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _category_set = true
          }
          "i18n" -> {
            _i18n_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_i18n__reference)
            _i18n_set = true
          }
          "defaultLabel" -> {
            _defaultLabel_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _defaultLabel_set = true
          }
          "defaultLocale" -> {
            _defaultLocale_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _defaultLocale_set = true
          }
          "defaultI18n" -> {
            _defaultI18n_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_defaultI18n__reference)
            _defaultI18n_set = true
          }
          "version" -> {
            _version_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _version_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _namespace_set && _category_set && _i18n_set && _defaultLabel_set && _defaultLocale_set && _defaultI18n_set && _version_set) {
        I18nLabel(_id = __id_!!, namespace = _namespace_!!, category = _category_!!, i18n = _i18n_!!, defaultLabel = _defaultLabel_, defaultLocale = _defaultLocale_!!, defaultI18n = _defaultI18n_!!, version = _version_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_category_set) map_[parameters.getValue("category")] = _category_
        if (_i18n_set) map_[parameters.getValue("i18n")] = _i18n_
        if (_defaultLabel_set) map_[parameters.getValue("defaultLabel")] = _defaultLabel_
        if (_defaultLocale_set) map_[parameters.getValue("defaultLocale")] = _defaultLocale_
        if (_defaultI18n_set) map_[parameters.getValue("defaultI18n")] = _defaultI18n_
        if (_version_set) map_[parameters.getValue("version")] = _version_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<I18nLabel> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { I18nLabel::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "category" to primaryConstructor.findParameterByName("category")!!,
            "i18n" to primaryConstructor.findParameterByName("i18n")!!,
            "defaultLabel" to primaryConstructor.findParameterByName("defaultLabel")!!,
            "defaultLocale" to primaryConstructor.findParameterByName("defaultLocale")!!,
            "defaultI18n" to primaryConstructor.findParameterByName("defaultI18n")!!,
            "version" to primaryConstructor.findParameterByName("version")!!,
          )
        }

    private val __id__reference: TypeReference<Id<I18nLabel>> =
        object : TypeReference<Id<I18nLabel>>() {}

    private val _i18n__reference: TypeReference<LinkedHashSet<I18nLocalizedLabel>> =
        object : TypeReference<LinkedHashSet<I18nLocalizedLabel>>() {}

    private val _defaultI18n__reference: TypeReference<Set<I18nLocalizedLabel>> =
        object : TypeReference<Set<I18nLocalizedLabel>>() {}
  }
}
