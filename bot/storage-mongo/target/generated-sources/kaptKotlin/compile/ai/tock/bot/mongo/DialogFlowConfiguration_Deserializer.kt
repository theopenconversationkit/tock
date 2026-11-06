package ai.tock.bot.mongo

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
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class DialogFlowConfiguration_Deserializer : ValueDeserializer<DialogFlowConfiguration>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogFlowConfiguration::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowConfiguration {
    with(p) {
      var __id_: String? = null
      var __id_set: Boolean = false
      var _currentProcessedLevel_: Int? = null
      var _currentProcessedLevel_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            __id_set = true
          }
          "currentProcessedLevel" -> {
            _currentProcessedLevel_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _currentProcessedLevel_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _currentProcessedLevel_set) {
        DialogFlowConfiguration(_id = __id_!!, currentProcessedLevel = _currentProcessedLevel_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_currentProcessedLevel_set) map_[parameters.getValue("currentProcessedLevel")] = _currentProcessedLevel_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogFlowConfiguration> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogFlowConfiguration::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "currentProcessedLevel" to primaryConstructor.findParameterByName("currentProcessedLevel")!!,
          )
        }
  }
}
