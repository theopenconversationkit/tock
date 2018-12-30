package fr.vsct.tock.bot.admin.test

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Duration
import java.time.Instant
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class TestPlanExecution_Deserializer :
        StdDeserializer<TestPlanExecution>(TestPlanExecution::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TestPlanExecution::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestPlanExecution {
        with(p) {
            var _testPlanId_: Id<TestPlan>? = null
            var _testPlanId_set = false
            var _dialogs_: MutableList<DialogExecutionReport>? = null
            var _dialogs_set = false
            var _nbErrors_: Int? = null
            var _nbErrors_set = false
            var _date_: Instant? = null
            var _date_set = false
            var _duration_: Duration? = null
            var _duration_set = false
            var __id_: Id<TestPlanExecution>? = null
            var __id_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "testPlanId" -> {
                            _testPlanId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_testPlanId__reference);
                            _testPlanId_set = true
                            }
                    "dialogs" -> {
                            _dialogs_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_dialogs__reference);
                            _dialogs_set = true
                            }
                    "nbErrors" -> {
                            _nbErrors_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _nbErrors_set = true
                            }
                    "date" -> {
                            _date_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    "duration" -> {
                            _duration_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Duration::class.java);
                            _duration_set = true
                            }
                    "_id" -> {
                            __id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_testPlanId_set && _dialogs_set && _nbErrors_set && _date_set && _duration_set
                    && __id_set)
                    TestPlanExecution(testPlanId = _testPlanId_!!, dialogs = _dialogs_!!, nbErrors =
                            _nbErrors_!!, date = _date_!!, duration = _duration_!!, _id = __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_testPlanId_set)
                    map[parameters.getValue("testPlanId")] = _testPlanId_
                    if(_dialogs_set)
                    map[parameters.getValue("dialogs")] = _dialogs_
                    if(_nbErrors_set)
                    map[parameters.getValue("nbErrors")] = _nbErrors_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_
                    if(_duration_set)
                    map[parameters.getValue("duration")] = _duration_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<TestPlanExecution> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                TestPlanExecution::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("testPlanId" to
                primaryConstructor.findParameterByName("testPlanId")!!, "dialogs" to
                primaryConstructor.findParameterByName("dialogs")!!, "nbErrors" to
                primaryConstructor.findParameterByName("nbErrors")!!, "date" to
                primaryConstructor.findParameterByName("date")!!, "duration" to
                primaryConstructor.findParameterByName("duration")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!) }

        private val _testPlanId__reference: TypeReference<Id<TestPlan>> = object :
                TypeReference<Id<TestPlan>>() {}

        private val _dialogs__reference: TypeReference<List<DialogExecutionReport>> = object :
                TypeReference<List<DialogExecutionReport>>() {}

        private val __id__reference: TypeReference<Id<TestPlanExecution>> = object :
                TypeReference<Id<TestPlanExecution>>() {}
    }
}
