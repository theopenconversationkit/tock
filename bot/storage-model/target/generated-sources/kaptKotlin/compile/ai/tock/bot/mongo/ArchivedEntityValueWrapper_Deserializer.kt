package ai.tock.bot.mongo

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.EntityValue
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

internal class ArchivedEntityValueWrapper_Deserializer : ValueDeserializer<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ArchivedEntityValuesCol.ArchivedEntityValueWrapper {
    with(p) {
      var _entityValue_: EntityValue? = null
      var _entityValue_set: Boolean = false
      var _actionId_: Id<Action>? = null
      var _actionId_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "entityValue" -> {
            _entityValue_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(EntityValue::class.javaObjectType)
            _entityValue_set = true
          }
          "actionId" -> {
            _actionId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_actionId__reference)
            _actionId_set = true
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
      val result_ = if (_entityValue_set && _actionId_set && _date_set) {
        ArchivedEntityValuesCol.ArchivedEntityValueWrapper(entityValue = _entityValue_, actionId = _actionId_, date = _date_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_entityValue_set) map_[parameters.getValue("entityValue")] = _entityValue_
        if (_actionId_set) map_[parameters.getValue("actionId")] = _actionId_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ArchivedEntityValuesCol.ArchivedEntityValueWrapper> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ArchivedEntityValuesCol.ArchivedEntityValueWrapper::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "entityValue" to primaryConstructor.findParameterByName("entityValue")!!,
            "actionId" to primaryConstructor.findParameterByName("actionId")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
          )
        }

    private val _actionId__reference: TypeReference<Id<Action>> =
        object : TypeReference<Id<Action>>() {}
  }
}
