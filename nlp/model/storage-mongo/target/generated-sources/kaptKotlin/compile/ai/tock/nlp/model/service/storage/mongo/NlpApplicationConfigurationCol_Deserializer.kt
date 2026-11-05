package ai.tock.nlp.model.service.storage.mongo

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
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
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class NlpApplicationConfigurationCol_Deserializer : ValueDeserializer<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol {
    with(p) {
      var _applicationName_: String? = null
      var _applicationName_set: Boolean = false
      var _engineType_: NlpEngineType? = null
      var _engineType_set: Boolean = false
      var _configuration_: NlpApplicationConfiguration? = null
      var _configuration_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "applicationName" -> {
            _applicationName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _applicationName_set = true
          }
          "engineType" -> {
            _engineType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpEngineType::class.javaObjectType)
            _engineType_set = true
          }
          "configuration" -> {
            _configuration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpApplicationConfiguration::class.javaObjectType)
            _configuration_set = true
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
      val result_ = if (_applicationName_set && _engineType_set && _configuration_set && _date_set) {
        NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol(applicationName = _applicationName_!!, engineType = _engineType_!!, configuration = _configuration_!!, date = _date_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationName_set) map_[parameters.getValue("applicationName")] = _applicationName_
        if (_engineType_set) map_[parameters.getValue("engineType")] = _engineType_
        if (_configuration_set) map_[parameters.getValue("configuration")] = _configuration_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor:
        KFunction<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationName" to primaryConstructor.findParameterByName("applicationName")!!,
            "engineType" to primaryConstructor.findParameterByName("engineType")!!,
            "configuration" to primaryConstructor.findParameterByName("configuration")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
          )
        }
  }
}
