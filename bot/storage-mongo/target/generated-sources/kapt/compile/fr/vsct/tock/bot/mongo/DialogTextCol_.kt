package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.dialog.Dialog
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class DialogTextCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        DialogTextCol?>) : KPropertyPath<T, DialogTextCol?>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,DialogTextCol::text)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,DialogTextCol::dialogId)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,DialogTextCol::date)

    companion object {
        val Text: KProperty1<DialogTextCol, String?>
            get() = DialogTextCol::text
        val DialogId: KProperty1<DialogTextCol, Id<Dialog>?>
            get() = DialogTextCol::dialogId
        val Date: KProperty1<DialogTextCol, Instant?>
            get() = DialogTextCol::date}
}

internal class DialogTextCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<DialogTextCol>?>) : KCollectionPropertyPath<T, DialogTextCol?,
        DialogTextCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,DialogTextCol::text)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,DialogTextCol::dialogId)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,DialogTextCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogTextCol_<T> =
            DialogTextCol_(this, customProperty(this, additionalPath))}

internal class DialogTextCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, DialogTextCol>?>) : KMapPropertyPath<T, K, DialogTextCol?,
        DialogTextCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,DialogTextCol::text)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,DialogTextCol::dialogId)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,DialogTextCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogTextCol_<T> =
            DialogTextCol_(this, customProperty(this, additionalPath))}
