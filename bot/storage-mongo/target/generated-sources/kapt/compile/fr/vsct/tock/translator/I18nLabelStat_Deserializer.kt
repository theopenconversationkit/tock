package fr.vsct.tock.translator

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class I18nLabelStat_Deserializer : StdDeserializer<I18nLabelStat>(I18nLabelStat::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(I18nLabelStat::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): I18nLabelStat {
        with(p) {
        var labelId: Id<I18nLabel>? = null
        var namespace: String? = null
        var locale: Locale? = null
        var interfaceType: UserInterfaceType? = null
        var connectorId: String? = null
        var count: Int? = null
        var lastUpdate: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "labelId" -> labelId = p.readValueAs(labelId_reference)
        "namespace" -> namespace = p.text
        "locale" -> locale = p.readValueAs(Locale::class.java)
        "interfaceType" -> interfaceType = p.readValueAs(UserInterfaceType::class.java)
        "connectorId" -> connectorId = p.text
        "count" -> count = p.readValueAs(Int::class.java)
        "lastUpdate" -> lastUpdate = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return I18nLabelStat(labelId!!, namespace!!, locale!!, interfaceType!!, connectorId, count!!, lastUpdate!!)
                }
    }
    companion object {
        val labelId_reference: TypeReference<Id<I18nLabel>> =
                object : TypeReference<Id<I18nLabel>>() {}
    }
}
