package fr.vsct.tock.nlp.front.shared.test

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Int
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class IntentTestError_Deserializer : StdDeserializer<IntentTestError>(IntentTestError::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(IntentTestError::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntentTestError {
        with(p) {
        var applicationId: Id<ApplicationDefinition>? = null
        var language: Locale? = null
        var text: String? = null
        var currentIntent: String? = null
        var wrongIntent: String? = null
        var averageErrorProbability: Double? = null
        var count: Int? = null
        var total: Int? = null
        var firstDetectionDate: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "applicationId" -> applicationId = p.readValueAs(applicationId_reference)
        "language" -> language = p.readValueAs(Locale::class.java)
        "text" -> text = p.text
        "currentIntent" -> currentIntent = p.text
        "wrongIntent" -> wrongIntent = p.text
        "averageErrorProbability" -> averageErrorProbability = p.readValueAs(Double::class.java)
        "count" -> count = p.readValueAs(Int::class.java)
        "total" -> total = p.readValueAs(Int::class.java)
        "firstDetectionDate" -> firstDetectionDate = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return IntentTestError(applicationId!!, language!!, text!!, currentIntent!!, wrongIntent!!, averageErrorProbability!!, count!!, total!!, firstDetectionDate!!)
                }
    }
    companion object {
        val applicationId_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}
    }
}
