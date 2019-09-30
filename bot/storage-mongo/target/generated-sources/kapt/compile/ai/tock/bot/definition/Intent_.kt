package ai.tock.bot.definition

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Name: KProperty1<Intent, String?>
    get() = Intent::name
class Intent_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Intent?>) :
        KPropertyPath<T, Intent?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    companion object {
        val Name: KProperty1<Intent, String?>
            get() = __Name}
}

class Intent_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<Intent>?>) :
        KCollectionPropertyPath<T, Intent?, Intent_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Intent_<T> = Intent_(this,
            customProperty(this, additionalPath))}

class Intent_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, Intent>?>) :
        KMapPropertyPath<T, K, Intent?, Intent_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Intent_<T> = Intent_(this,
            customProperty(this, additionalPath))}
