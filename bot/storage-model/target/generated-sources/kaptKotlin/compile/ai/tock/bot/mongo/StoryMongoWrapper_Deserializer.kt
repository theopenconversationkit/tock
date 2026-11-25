package ai.tock.bot.mongo

import ai.tock.bot.definition.Intent
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
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

internal class StoryMongoWrapper_Deserializer : ValueDeserializer<DialogCol.StoryMongoWrapper>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogCol.StoryMongoWrapper::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogCol.StoryMongoWrapper {
    with(p) {
      var _storyDefinitionId_: String? = null
      var _storyDefinitionId_set: Boolean = false
      var _currentIntent_: Intent? = null
      var _currentIntent_set: Boolean = false
      var _currentStep_: String? = null
      var _currentStep_set: Boolean = false
      var _actions_: List<DialogCol.ActionMongoWrapper>? = null
      var _actions_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "storyDefinitionId" -> {
            _storyDefinitionId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _storyDefinitionId_set = true
          }
          "currentIntent" -> {
            _currentIntent_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Intent::class.javaObjectType)
            _currentIntent_set = true
          }
          "currentStep" -> {
            _currentStep_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _currentStep_set = true
          }
          "actions" -> {
            _actions_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_actions__reference)
            _actions_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_storyDefinitionId_set && _currentIntent_set && _currentStep_set && _actions_set) {
        DialogCol.StoryMongoWrapper(storyDefinitionId = _storyDefinitionId_!!, currentIntent = _currentIntent_, currentStep = _currentStep_, actions = _actions_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_storyDefinitionId_set) map_[parameters.getValue("storyDefinitionId")] = _storyDefinitionId_
        if (_currentIntent_set) map_[parameters.getValue("currentIntent")] = _currentIntent_
        if (_currentStep_set) map_[parameters.getValue("currentStep")] = _currentStep_
        if (_actions_set) map_[parameters.getValue("actions")] = _actions_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogCol.StoryMongoWrapper> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogCol.StoryMongoWrapper::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "storyDefinitionId" to primaryConstructor.findParameterByName("storyDefinitionId")!!,
            "currentIntent" to primaryConstructor.findParameterByName("currentIntent")!!,
            "currentStep" to primaryConstructor.findParameterByName("currentStep")!!,
            "actions" to primaryConstructor.findParameterByName("actions")!!,
          )
        }

    private val _actions__reference: TypeReference<List<DialogCol.ActionMongoWrapper>> =
        object : TypeReference<List<DialogCol.ActionMongoWrapper>>() {}
  }
}
