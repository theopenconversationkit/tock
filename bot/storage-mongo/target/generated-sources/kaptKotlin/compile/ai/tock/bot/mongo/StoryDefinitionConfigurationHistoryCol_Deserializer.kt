package ai.tock.bot.mongo

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
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

internal class StoryDefinitionConfigurationHistoryCol_Deserializer : ValueDeserializer<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol {
    with(p) {
      var _conf_: StoryDefinitionConfiguration? = null
      var _conf_set: Boolean = false
      var _deleted_: Boolean? = null
      var _deleted_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "conf" -> {
            _conf_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(StoryDefinitionConfiguration::class.javaObjectType)
            _conf_set = true
          }
          "deleted" -> {
            _deleted_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _deleted_set = true
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
      val result_ = if (_conf_set && _deleted_set && _date_set) {
        StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol(conf = _conf_!!, deleted = _deleted_!!, date = _date_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_conf_set) map_[parameters.getValue("conf")] = _conf_
        if (_deleted_set) map_[parameters.getValue("deleted")] = _deleted_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor:
        KFunction<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "conf" to primaryConstructor.findParameterByName("conf")!!,
            "deleted" to primaryConstructor.findParameterByName("deleted")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
          )
        }
  }
}
