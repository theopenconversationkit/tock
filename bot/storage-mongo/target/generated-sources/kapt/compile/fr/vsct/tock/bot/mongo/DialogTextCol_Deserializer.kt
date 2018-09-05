package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.dialog.Dialog
import java.time.Instant
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class DialogTextCol_Deserializer : StdDeserializer<DialogTextCol>(DialogTextCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogTextCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogTextCol {
        with(p) {
        var text: String? = null
        var dialogId: Id<Dialog>? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "text" -> text = p.text
        "dialogId" -> dialogId = p.readValueAs(dialogId_reference)
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return DialogTextCol(text!!, dialogId!!, date!!) }
    }
    companion object {
        val dialogId_reference: TypeReference<Id<Dialog>> = object : TypeReference<Id<Dialog>>() {}
    }
}
