package fr.vsct.tock.bot.definition

import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class Intent_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Intent?>) : KPropertyPath<T, Intent?>(previous,property) {
    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,Intent::name)
    companion object {
        val Name: KProperty1<Intent, String?>
            get() = Intent::name}
}

class Intent_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<Intent>?>) : KPropertyPath<T, Collection<Intent>?>(previous,property) {
    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,Intent::name)
}
