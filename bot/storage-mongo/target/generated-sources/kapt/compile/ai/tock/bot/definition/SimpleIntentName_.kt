package ai.tock.bot.definition

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Name: KProperty1<SimpleIntentName, String?>
    get() = SimpleIntentName::name
class SimpleIntentName_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        SimpleIntentName?>) : KPropertyPath<T, SimpleIntentName?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    companion object {
        val Name: KProperty1<SimpleIntentName, String?>
            get() = __Name}
}

class SimpleIntentName_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<SimpleIntentName>?>) : KCollectionPropertyPath<T, SimpleIntentName?,
        SimpleIntentName_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SimpleIntentName_<T> =
            SimpleIntentName_(this, customProperty(this, additionalPath))}

class SimpleIntentName_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        SimpleIntentName>?>) : KMapPropertyPath<T, K, SimpleIntentName?,
        SimpleIntentName_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SimpleIntentName_<T> =
            SimpleIntentName_(this, customProperty(this, additionalPath))}
