package ai.tock.nlp.front.shared.config

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

internal class FaqSettings_Deserializer : ValueDeserializer<FaqSettings>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(FaqSettings::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FaqSettings {
    with(p) {
      var __id_: Id<FaqSettings>? = null
      var __id_set: Boolean = false
      var _applicationId_: Id<ApplicationDefinition>? = null
      var _applicationId_set: Boolean = false
      var _satisfactionEnabled_: Boolean? = null
      var _satisfactionEnabled_set: Boolean = false
      var _satisfactionStoryId_: String? = null
      var _satisfactionStoryId_set: Boolean = false
      var _creationDate_: Instant? = null
      var _creationDate_set: Boolean = false
      var _updateDate_: Instant? = null
      var _updateDate_set: Boolean = false
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
          "applicationId" -> {
            _applicationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationId__reference)
            _applicationId_set = true
          }
          "satisfactionEnabled" -> {
            _satisfactionEnabled_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _satisfactionEnabled_set = true
          }
          "satisfactionStoryId" -> {
            _satisfactionStoryId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _satisfactionStoryId_set = true
          }
          "creationDate" -> {
            _creationDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _creationDate_set = true
          }
          "updateDate" -> {
            _updateDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _updateDate_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _applicationId_set && _satisfactionEnabled_set && _satisfactionStoryId_set && _creationDate_set && _updateDate_set) {
        FaqSettings(_id = __id_!!, applicationId = _applicationId_!!, satisfactionEnabled = _satisfactionEnabled_!!, satisfactionStoryId = _satisfactionStoryId_, creationDate = _creationDate_!!, updateDate = _updateDate_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_satisfactionEnabled_set) map_[parameters.getValue("satisfactionEnabled")] = _satisfactionEnabled_
        if (_satisfactionStoryId_set) map_[parameters.getValue("satisfactionStoryId")] = _satisfactionStoryId_
        if (_creationDate_set) map_[parameters.getValue("creationDate")] = _creationDate_
        if (_updateDate_set) map_[parameters.getValue("updateDate")] = _updateDate_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<FaqSettings> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { FaqSettings::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "satisfactionEnabled" to primaryConstructor.findParameterByName("satisfactionEnabled")!!,
            "satisfactionStoryId" to primaryConstructor.findParameterByName("satisfactionStoryId")!!,
            "creationDate" to primaryConstructor.findParameterByName("creationDate")!!,
            "updateDate" to primaryConstructor.findParameterByName("updateDate")!!,
          )
        }

    private val __id__reference: TypeReference<Id<FaqSettings>> =
        object : TypeReference<Id<FaqSettings>>() {}

    private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> =
        object : TypeReference<Id<ApplicationDefinition>>() {}
  }
}
