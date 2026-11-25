package ai.tock.bot.admin.test

import java.time.Duration
import java.time.Instant
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
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

internal class TestPlanExecution_Deserializer : ValueDeserializer<TestPlanExecution>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(TestPlanExecution::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestPlanExecution {
    with(p) {
      var _testPlanId_: Id<TestPlan>? = null
      var _testPlanId_set: Boolean = false
      var _dialogs_: List<DialogExecutionReport>? = null
      var _dialogs_set: Boolean = false
      var _nbErrors_: Int? = null
      var _nbErrors_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var _duration_: Duration? = null
      var _duration_set: Boolean = false
      var __id_: Id<TestPlanExecution>? = null
      var __id_set: Boolean = false
      var _status_: TestPlanExecutionStatus? = null
      var _status_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "testPlanId" -> {
            _testPlanId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_testPlanId__reference)
            _testPlanId_set = true
          }
          "dialogs" -> {
            _dialogs_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_dialogs__reference)
            _dialogs_set = true
          }
          "nbErrors" -> {
            _nbErrors_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _nbErrors_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _date_set = true
          }
          "duration" -> {
            _duration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Duration::class.javaObjectType)
            _duration_set = true
          }
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "status" -> {
            _status_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(TestPlanExecutionStatus::class.javaObjectType)
            _status_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_testPlanId_set && _dialogs_set && _nbErrors_set && _date_set && _duration_set && __id_set && _status_set) {
        TestPlanExecution(testPlanId = _testPlanId_!!, dialogs = _dialogs_!!, nbErrors = _nbErrors_!!, date = _date_!!, duration = _duration_!!, _id = __id_!!, status = _status_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_testPlanId_set) map_[parameters.getValue("testPlanId")] = _testPlanId_
        if (_dialogs_set) map_[parameters.getValue("dialogs")] = _dialogs_
        if (_nbErrors_set) map_[parameters.getValue("nbErrors")] = _nbErrors_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        if (_duration_set) map_[parameters.getValue("duration")] = _duration_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_status_set) map_[parameters.getValue("status")] = _status_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<TestPlanExecution> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { TestPlanExecution::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "testPlanId" to primaryConstructor.findParameterByName("testPlanId")!!,
            "dialogs" to primaryConstructor.findParameterByName("dialogs")!!,
            "nbErrors" to primaryConstructor.findParameterByName("nbErrors")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
            "duration" to primaryConstructor.findParameterByName("duration")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "status" to primaryConstructor.findParameterByName("status")!!,
          )
        }

    private val _testPlanId__reference: TypeReference<Id<TestPlan>> =
        object : TypeReference<Id<TestPlan>>() {}

    private val _dialogs__reference: TypeReference<List<DialogExecutionReport>> =
        object : TypeReference<List<DialogExecutionReport>>() {}

    private val __id__reference: TypeReference<Id<TestPlanExecution>> =
        object : TypeReference<Id<TestPlanExecution>>() {}
  }
}
