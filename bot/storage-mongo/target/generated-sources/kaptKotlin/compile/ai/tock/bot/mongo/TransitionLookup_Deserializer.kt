package ai.tock.bot.mongo

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

internal class TransitionLookup_Deserializer : ValueDeserializer<TransitionLookup>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(TransitionLookup::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TransitionLookup {
    with(p) {
      var _transition_: DialogFlowStateTransitionCol? = null
      var _transition_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "transition" -> {
            _transition_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(DialogFlowStateTransitionCol::class.javaObjectType)
            _transition_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_transition_set) {
        TransitionLookup(transition = _transition_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_transition_set) map_[parameters.getValue("transition")] = _transition_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<TransitionLookup> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { TransitionLookup::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "transition" to primaryConstructor.findParameterByName("transition")!!,
          )
        }
  }
}
