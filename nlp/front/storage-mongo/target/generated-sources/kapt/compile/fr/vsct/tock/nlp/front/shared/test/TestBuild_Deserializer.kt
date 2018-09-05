package fr.vsct.tock.nlp.front.shared.test

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.Int
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class TestBuild_Deserializer : StdDeserializer<TestBuild>(TestBuild::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TestBuild::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestBuild {
        with(p) {
        var applicationId: Id<ApplicationDefinition>? = null
        var language: Locale? = null
        var startDate: Instant? = null
        var buildModelDuration: Duration? = null
        var testSentencesDuration: Duration? = null
        var nbSentencesInModel: Int? = null
        var nbSentencesTested: Int? = null
        var nbErrors: Int? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "applicationId" -> applicationId = p.readValueAs(applicationId_reference)
        "language" -> language = p.readValueAs(Locale::class.java)
        "startDate" -> startDate = p.readValueAs(Instant::class.java)
        "buildModelDuration" -> buildModelDuration = p.readValueAs(Duration::class.java)
        "testSentencesDuration" -> testSentencesDuration = p.readValueAs(Duration::class.java)
        "nbSentencesInModel" -> nbSentencesInModel = p.readValueAs(Int::class.java)
        "nbSentencesTested" -> nbSentencesTested = p.readValueAs(Int::class.java)
        "nbErrors" -> nbErrors = p.readValueAs(Int::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return TestBuild(applicationId!!, language!!, startDate!!, buildModelDuration!!, testSentencesDuration!!, nbSentencesInModel!!, nbSentencesTested!!, nbErrors!!)
                }
    }
    companion object {
        val applicationId_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}
    }
}
