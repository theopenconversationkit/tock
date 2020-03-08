package ai.tock.bot.definition

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Name: KProperty1<IntentWithoutNamespace, String?>
    get() = IntentWithoutNamespace::name
class IntentWithoutNamespace_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        IntentWithoutNamespace?>) : KPropertyPath<T, IntentWithoutNamespace?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    companion object {
        val Name: KProperty1<IntentWithoutNamespace, String?>
            get() = __Name}
}

class IntentWithoutNamespace_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<IntentWithoutNamespace>?>) : KCollectionPropertyPath<T, IntentWithoutNamespace?,
        IntentWithoutNamespace_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentWithoutNamespace_<T> =
            IntentWithoutNamespace_(this, customProperty(this, additionalPath))}

class IntentWithoutNamespace_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, IntentWithoutNamespace>?>) : KMapPropertyPath<T, K, IntentWithoutNamespace?,
        IntentWithoutNamespace_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentWithoutNamespace_<T> =
            IntentWithoutNamespace_(this, customProperty(this, additionalPath))}
