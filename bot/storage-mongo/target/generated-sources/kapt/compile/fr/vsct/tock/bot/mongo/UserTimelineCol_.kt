package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.user.PlayerId_
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class UserTimelineCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, UserTimelineCol?>) : KPropertyPath<T, UserTimelineCol?>(previous,property) {
    val _id: KProperty1<T, Id<UserTimelineCol>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::_id)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,UserTimelineCol::playerId)

    val userPreferences: UserPreferencesWrapper_<T>
        get() = UserPreferencesWrapper_(this,UserTimelineCol::userPreferences)

    val userState: UserStateWrapper_<T>
        get() = UserStateWrapper_(this,UserTimelineCol::userState)

    val temporaryIds: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::temporaryIds)

    val applicationIds: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::applicationIds)

    val lastActionText: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::lastActionText)

    val lastUpdateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::lastUpdateDate)

    val lastUserActionDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::lastUserActionDate)
    companion object {
        val _id: KProperty1<UserTimelineCol, Id<UserTimelineCol>?>
            get() = UserTimelineCol::_id
        val PlayerId: PlayerId_<UserTimelineCol>
            get() = PlayerId_<UserTimelineCol>(null,UserTimelineCol::playerId)
        val UserPreferences: UserPreferencesWrapper_<UserTimelineCol>
            get() = UserPreferencesWrapper_<UserTimelineCol>(null,UserTimelineCol::userPreferences)
        val UserState: UserStateWrapper_<UserTimelineCol>
            get() = UserStateWrapper_<UserTimelineCol>(null,UserTimelineCol::userState)
        val TemporaryIds: KProperty1<UserTimelineCol, Set<String>?>
            get() = UserTimelineCol::temporaryIds
        val ApplicationIds: KProperty1<UserTimelineCol, Set<String>?>
            get() = UserTimelineCol::applicationIds
        val LastActionText: KProperty1<UserTimelineCol, String?>
            get() = UserTimelineCol::lastActionText
        val LastUpdateDate: KProperty1<UserTimelineCol, Instant?>
            get() = UserTimelineCol::lastUpdateDate
        val LastUserActionDate: KProperty1<UserTimelineCol, Instant?>
            get() = UserTimelineCol::lastUserActionDate}
}

internal class UserTimelineCol_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<UserTimelineCol>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, UserTimelineCol?>(previous,property,additionalPath) {
    override val arrayProjection: UserTimelineCol_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = UserTimelineCol_Col(null, this as KProperty1<*, Collection<UserTimelineCol>?>, "$")

    val _id: KProperty1<T, Id<UserTimelineCol>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::_id)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,UserTimelineCol::playerId)

    val userPreferences: UserPreferencesWrapper_<T>
        get() = UserPreferencesWrapper_(this,UserTimelineCol::userPreferences)

    val userState: UserStateWrapper_<T>
        get() = UserStateWrapper_(this,UserTimelineCol::userState)

    val temporaryIds: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::temporaryIds)

    val applicationIds: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::applicationIds)

    val lastActionText: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::lastActionText)

    val lastUpdateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::lastUpdateDate)

    val lastUserActionDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol::lastUserActionDate)
}
