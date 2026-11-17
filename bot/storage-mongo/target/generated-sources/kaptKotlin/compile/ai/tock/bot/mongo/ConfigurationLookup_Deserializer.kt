package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
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

internal class ConfigurationLookup_Deserializer : ValueDeserializer<ConfigurationLookup>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ConfigurationLookup::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConfigurationLookup {
    with(p) {
      var _configuration_: BotApplicationConfiguration? = null
      var _configuration_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "configuration" -> {
            _configuration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(BotApplicationConfiguration::class.javaObjectType)
            _configuration_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_configuration_set) {
        ConfigurationLookup(configuration = _configuration_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_configuration_set) map_[parameters.getValue("configuration")] = _configuration_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ConfigurationLookup> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ConfigurationLookup::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "configuration" to primaryConstructor.findParameterByName("configuration")!!,
          )
        }
  }
}
