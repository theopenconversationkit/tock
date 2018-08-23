package fr.vsct.tock.bot.engine.user

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class PlayerId_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, PlayerId?>) : KPropertyPath<T, PlayerId?>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,PlayerId::id)

    val type: KPropertyPath<T, PlayerType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.user.PlayerType?>(this,PlayerId::type)

    val clientId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,PlayerId::clientId)
    companion object {
        val Id: KProperty1<PlayerId, String?>
            get() = PlayerId::id
        val Type: KProperty1<PlayerId, PlayerType?>
            get() = PlayerId::type
        val ClientId: KProperty1<PlayerId, String?>
            get() = PlayerId::clientId}
}

class PlayerId_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<PlayerId>?>) : KCollectionPropertyPath<T, PlayerId?, PlayerId_<T>>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,PlayerId::id)

    val type: KPropertyPath<T, PlayerType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.user.PlayerType?>(this,PlayerId::type)

    val clientId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,PlayerId::clientId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): PlayerId_<T> = PlayerId_(this, customProperty(this, additionalPath))}

class PlayerId_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, PlayerId>?>) : KMapPropertyPath<T, K, PlayerId?, PlayerId_<T>>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,PlayerId::id)

    val type: KPropertyPath<T, PlayerType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.user.PlayerType?>(this,PlayerId::type)

    val clientId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,PlayerId::clientId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): PlayerId_<T> = PlayerId_(this, customProperty(this, additionalPath))}
