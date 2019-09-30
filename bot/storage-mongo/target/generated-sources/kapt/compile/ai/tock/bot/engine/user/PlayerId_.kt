package ai.tock.bot.engine.user

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Id: KProperty1<PlayerId, String?>
    get() = PlayerId::id
private val __Type: KProperty1<PlayerId, PlayerType?>
    get() = PlayerId::type
private val __ClientId: KProperty1<PlayerId, String?>
    get() = PlayerId::clientId
class PlayerId_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, PlayerId?>) :
        KPropertyPath<T, PlayerId?>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Id)

    val type: KPropertyPath<T, PlayerType?>
        get() = KPropertyPath(this,__Type)

    val clientId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientId)

    companion object {
        val Id: KProperty1<PlayerId, String?>
            get() = __Id
        val Type: KProperty1<PlayerId, PlayerType?>
            get() = __Type
        val ClientId: KProperty1<PlayerId, String?>
            get() = __ClientId}
}

class PlayerId_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<PlayerId>?>) : KCollectionPropertyPath<T, PlayerId?,
        PlayerId_<T>>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Id)

    val type: KPropertyPath<T, PlayerType?>
        get() = KPropertyPath(this,__Type)

    val clientId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): PlayerId_<T> = PlayerId_(this,
            customProperty(this, additionalPath))}

class PlayerId_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, PlayerId>?>)
        : KMapPropertyPath<T, K, PlayerId?, PlayerId_<T>>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Id)

    val type: KPropertyPath<T, PlayerType?>
        get() = KPropertyPath(this,__Type)

    val clientId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): PlayerId_<T> = PlayerId_(this,
            customProperty(this, additionalPath))}
