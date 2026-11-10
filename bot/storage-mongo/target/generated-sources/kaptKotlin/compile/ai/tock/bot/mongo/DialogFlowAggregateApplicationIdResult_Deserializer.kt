package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import java.time.LocalDateTime
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
import org.litote.kmongo.Id
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class DialogFlowAggregateApplicationIdResult_Deserializer : ValueDeserializer<DialogFlowAggregateApplicationIdResult>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogFlowAggregateApplicationIdResult::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowAggregateApplicationIdResult {
    with(p) {
      var _applicationId_: Id<BotApplicationConfiguration>? = null
      var _applicationId_set: Boolean = false
      var _date_: LocalDateTime? = null
      var _date_set: Boolean = false
      var _count_: Int? = null
      var _count_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "applicationId" -> {
            _applicationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationId__reference)
            _applicationId_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(LocalDateTime::class.javaObjectType)
            _date_set = true
          }
          "count" -> {
            _count_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _count_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_applicationId_set && _date_set && _count_set) {
        DialogFlowAggregateApplicationIdResult(applicationId = _applicationId_!!, date = _date_!!, count = _count_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        if (_count_set) map_[parameters.getValue("count")] = _count_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogFlowAggregateApplicationIdResult> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogFlowAggregateApplicationIdResult::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
            "count" to primaryConstructor.findParameterByName("count")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<BotApplicationConfiguration>> =
        object : TypeReference<Id<BotApplicationConfiguration>>() {}
  }
}
