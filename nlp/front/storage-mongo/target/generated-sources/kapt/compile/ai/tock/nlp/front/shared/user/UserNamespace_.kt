package ai.tock.nlp.front.shared.user

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Login: KProperty1<UserNamespace, String?>
    get() = UserNamespace::login
private val __Namespace: KProperty1<UserNamespace, String?>
    get() = UserNamespace::namespace
private val __Owner: KProperty1<UserNamespace, Boolean?>
    get() = UserNamespace::owner
private val __Current: KProperty1<UserNamespace, Boolean?>
    get() = UserNamespace::current
class UserNamespace_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, UserNamespace?>) :
        KPropertyPath<T, UserNamespace?>(previous,property) {
    val login: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Login)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val owner: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Owner)

    val current: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Current)

    companion object {
        val Login: KProperty1<UserNamespace, String?>
            get() = __Login
        val Namespace: KProperty1<UserNamespace, String?>
            get() = __Namespace
        val Owner: KProperty1<UserNamespace, Boolean?>
            get() = __Owner
        val Current: KProperty1<UserNamespace, Boolean?>
            get() = __Current}
}

class UserNamespace_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<UserNamespace>?>) : KCollectionPropertyPath<T, UserNamespace?,
        UserNamespace_<T>>(previous,property) {
    val login: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Login)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val owner: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Owner)

    val current: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Current)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserNamespace_<T> =
            UserNamespace_(this, customProperty(this, additionalPath))}

class UserNamespace_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        UserNamespace>?>) : KMapPropertyPath<T, K, UserNamespace?,
        UserNamespace_<T>>(previous,property) {
    val login: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Login)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val owner: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Owner)

    val current: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Current)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserNamespace_<T> =
            UserNamespace_(this, customProperty(this, additionalPath))}
