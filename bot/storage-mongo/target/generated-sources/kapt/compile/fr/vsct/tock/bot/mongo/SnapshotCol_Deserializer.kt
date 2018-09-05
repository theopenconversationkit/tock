package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Snapshot
import java.time.Instant
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class SnapshotCol_Deserializer : StdDeserializer<SnapshotCol>(SnapshotCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(SnapshotCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SnapshotCol {
        with(p) {
        var _id: Id<Dialog>? = null
        var snapshots: List<Snapshot>? = null
        var lastUpdateDate: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "_id" -> _id = p.readValueAs(_id_reference)
        "snapshots" -> snapshots = p.readValueAs(snapshots_reference)
        "lastUpdateDate" -> lastUpdateDate = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return SnapshotCol(_id!!, snapshots!!, lastUpdateDate!!) }
    }
    companion object {
        val _id_reference: TypeReference<Id<Dialog>> = object : TypeReference<Id<Dialog>>() {}

        val snapshots_reference: TypeReference<List<Snapshot>> =
                object : TypeReference<List<Snapshot>>() {}
    }
}
