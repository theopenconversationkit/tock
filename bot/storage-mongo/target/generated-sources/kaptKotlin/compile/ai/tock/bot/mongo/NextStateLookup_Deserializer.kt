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

internal class NextStateLookup_Deserializer : ValueDeserializer<NextStateLookup>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(NextStateLookup::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NextStateLookup {
    with(p) {
      var _nextState_: DialogFlowStateCol? = null
      var _nextState_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "nextState" -> {
            _nextState_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(DialogFlowStateCol::class.javaObjectType)
            _nextState_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_nextState_set) {
        NextStateLookup(nextState = _nextState_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_nextState_set) map_[parameters.getValue("nextState")] = _nextState_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<NextStateLookup> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { NextStateLookup::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "nextState" to primaryConstructor.findParameterByName("nextState")!!,
          )
        }
  }
}
