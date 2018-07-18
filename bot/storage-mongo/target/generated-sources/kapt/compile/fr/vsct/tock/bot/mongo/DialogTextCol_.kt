package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.dialog.Dialog
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class DialogTextCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, DialogTextCol?>) : KPropertyPath<T, DialogTextCol?>(previous,property) {
    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogTextCol::text)

    val dialogId: KProperty1<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogTextCol::dialogId)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogTextCol::date)
    companion object {
        val Text: KProperty1<DialogTextCol, String?>
            get() = DialogTextCol::text
        val DialogId: KProperty1<DialogTextCol, Id<Dialog>?>
            get() = DialogTextCol::dialogId
        val Date: KProperty1<DialogTextCol, Instant?>
            get() = DialogTextCol::date}
}

internal class DialogTextCol_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<DialogTextCol>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, DialogTextCol?>(previous,property,additionalPath) {
    override val arrayProjection: DialogTextCol_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = DialogTextCol_Col(null, this as KProperty1<*, Collection<DialogTextCol>?>, "$")

    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogTextCol::text)

    val dialogId: KProperty1<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogTextCol::dialogId)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogTextCol::date)
}
