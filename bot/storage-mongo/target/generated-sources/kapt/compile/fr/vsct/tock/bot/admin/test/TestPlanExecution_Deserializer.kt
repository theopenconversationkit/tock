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
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class TestPlanExecution_Deserializer : StdDeserializer<TestPlanExecution>(TestPlanExecution::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TestPlanExecution::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestPlanExecution {
        with(p) {
        var testPlanId: Id<TestPlan>? = null
        var dialogs: List<DialogExecutionReport>? = null
        var nbErrors: Int? = null
        var date: Instant? = null
        var duration: Duration? = null
        var _id: Id<TestPlanExecution>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "testPlanId" -> testPlanId = p.readValueAs(testPlanId_reference)
        "dialogs" -> dialogs = p.readValueAs(dialogs_reference)
        "nbErrors" -> nbErrors = p.readValueAs(Int::class.java)
        "date" -> date = p.readValueAs(Instant::class.java)
        "duration" -> duration = p.readValueAs(Duration::class.java)
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return TestPlanExecution(testPlanId!!, dialogs!!, nbErrors!!, date!!, duration!!, _id!!) }
    }
    companion object {
        val testPlanId_reference: TypeReference<Id<TestPlan>> =
                object : TypeReference<Id<TestPlan>>() {}

        val dialogs_reference: TypeReference<List<DialogExecutionReport>> =
                object : TypeReference<List<DialogExecutionReport>>() {}

        val _id_reference: TypeReference<Id<TestPlanExecution>> =
                object : TypeReference<Id<TestPlanExecution>>() {}
    }
}
