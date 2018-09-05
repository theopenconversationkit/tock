package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Long
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ClassifiedSentenceCol_Deserializer : StdDeserializer<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClassifiedSentenceMongoDAO.ClassifiedSentenceCol {
        with(p) {
        var text: String? = null
        var fullText: String? = null
        var language: Locale? = null
        var applicationId: Id<ApplicationDefinition>? = null
        var creationDate: Instant? = null
        var updateDate: Instant? = null
        var status: ClassifiedSentenceStatus? = null
        var classification: Classification? = null
        var lastIntentProbability: Double? = null
        var lastEntityProbability: Double? = null
        var lastUsage: Instant? = null
        var usageCount: Long? = null
        var unknownCount: Long? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "text" -> text = p.text
        "fullText" -> fullText = p.text
        "language" -> language = p.readValueAs(Locale::class.java)
        "applicationId" -> applicationId = p.readValueAs(applicationId_reference)
        "creationDate" -> creationDate = p.readValueAs(Instant::class.java)
        "updateDate" -> updateDate = p.readValueAs(Instant::class.java)
        "status" -> status = p.readValueAs(ClassifiedSentenceStatus::class.java)
        "classification" -> classification = p.readValueAs(Classification::class.java)
        "lastIntentProbability" -> lastIntentProbability = p.doubleValue
        "lastEntityProbability" -> lastEntityProbability = p.doubleValue
        "lastUsage" -> lastUsage = p.readValueAs(Instant::class.java)
        "usageCount" -> usageCount = p.longValue
        "unknownCount" -> unknownCount = p.longValue
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ClassifiedSentenceMongoDAO.ClassifiedSentenceCol(text!!, fullText!!, language!!, applicationId!!, creationDate!!, updateDate!!, status!!, classification!!, lastIntentProbability, lastEntityProbability, lastUsage, usageCount, unknownCount)
                }
    }
    companion object {
        val applicationId_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}
    }
}
