package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.dialog.Dialog
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ConnectorMessageColId_Deserializer : StdDeserializer<ConnectorMessageColId>(ConnectorMessageColId::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ConnectorMessageColId::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorMessageColId {
        with(p) {
        var actionId: Id<Action>? = null
        var dialogId: Id<Dialog>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "actionId" -> actionId = p.readValueAs(actionId_reference)
        "dialogId" -> dialogId = p.readValueAs(dialogId_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ConnectorMessageColId(actionId!!, dialogId!!) }
    }
    companion object {
        val actionId_reference: TypeReference<Id<Action>> = object : TypeReference<Id<Action>>() {}

        val dialogId_reference: TypeReference<Id<Dialog>> = object : TypeReference<Id<Dialog>>() {}
    }
}
