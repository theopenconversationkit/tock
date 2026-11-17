package ai.tock.bot.mongo

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
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

internal class StoryLookup_Deserializer : ValueDeserializer<StoryLookup>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(StoryLookup::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StoryLookup {
    with(p) {
      var _story_: StoryDefinitionConfiguration? = null
      var _story_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "story" -> {
            _story_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(StoryDefinitionConfiguration::class.javaObjectType)
            _story_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_story_set) {
        StoryLookup(story = _story_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_story_set) map_[parameters.getValue("story")] = _story_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<StoryLookup> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { StoryLookup::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "story" to primaryConstructor.findParameterByName("story")!!,
          )
        }
  }
}
