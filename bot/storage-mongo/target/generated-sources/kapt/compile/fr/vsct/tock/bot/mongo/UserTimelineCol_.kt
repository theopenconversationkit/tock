package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.user.PlayerId_
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class UserTimelineCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        UserTimelineCol?>) : KPropertyPath<T, UserTimelineCol?>(previous,property) {
    val _id: KPropertyPath<T, Id<UserTimelineCol>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.mongo.UserTimelineCol>?>(this,UserTimelineCol::_id)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,UserTimelineCol::playerId)

    val userPreferences: UserPreferencesWrapper_<T>
        get() = UserPreferencesWrapper_(this,UserTimelineCol::userPreferences)

    val userState: UserStateWrapper_<T>
        get() = UserStateWrapper_(this,UserTimelineCol::userState)

    val temporaryIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::temporaryIds)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::applicationIds)

    val lastActionText: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::lastActionText)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,UserTimelineCol::lastUpdateDate)

    val lastUserActionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,UserTimelineCol::lastUserActionDate)

    companion object {
        val _id: KProperty1<UserTimelineCol, Id<UserTimelineCol>?>
            get() = UserTimelineCol::_id
        val PlayerId: PlayerId_<UserTimelineCol>
            get() = PlayerId_<UserTimelineCol>(null,UserTimelineCol::playerId)
        val UserPreferences: UserPreferencesWrapper_<UserTimelineCol>
            get() = UserPreferencesWrapper_<UserTimelineCol>(null,UserTimelineCol::userPreferences)
        val UserState: UserStateWrapper_<UserTimelineCol>
            get() = UserStateWrapper_<UserTimelineCol>(null,UserTimelineCol::userState)
        val TemporaryIds: KCollectionSimplePropertyPath<UserTimelineCol, String?>
            get() = KCollectionSimplePropertyPath(null, UserTimelineCol::temporaryIds)
        val ApplicationIds: KCollectionSimplePropertyPath<UserTimelineCol, String?>
            get() = KCollectionSimplePropertyPath(null, UserTimelineCol::applicationIds)
        val LastActionText: KProperty1<UserTimelineCol, String?>
            get() = UserTimelineCol::lastActionText
        val LastUpdateDate: KProperty1<UserTimelineCol, Instant?>
            get() = UserTimelineCol::lastUpdateDate
        val LastUserActionDate: KProperty1<UserTimelineCol, Instant?>
            get() = UserTimelineCol::lastUserActionDate}
}

internal class UserTimelineCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<UserTimelineCol>?>) : KCollectionPropertyPath<T, UserTimelineCol?,
        UserTimelineCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<UserTimelineCol>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.mongo.UserTimelineCol>?>(this,UserTimelineCol::_id)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,UserTimelineCol::playerId)

    val userPreferences: UserPreferencesWrapper_<T>
        get() = UserPreferencesWrapper_(this,UserTimelineCol::userPreferences)

    val userState: UserStateWrapper_<T>
        get() = UserStateWrapper_(this,UserTimelineCol::userState)

    val temporaryIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::temporaryIds)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::applicationIds)

    val lastActionText: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::lastActionText)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,UserTimelineCol::lastUpdateDate)

    val lastUserActionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,UserTimelineCol::lastUserActionDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserTimelineCol_<T> =
            UserTimelineCol_(this, customProperty(this, additionalPath))}

internal class UserTimelineCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, UserTimelineCol>?>) : KMapPropertyPath<T, K, UserTimelineCol?,
        UserTimelineCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<UserTimelineCol>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.mongo.UserTimelineCol>?>(this,UserTimelineCol::_id)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,UserTimelineCol::playerId)

    val userPreferences: UserPreferencesWrapper_<T>
        get() = UserPreferencesWrapper_(this,UserTimelineCol::userPreferences)

    val userState: UserStateWrapper_<T>
        get() = UserStateWrapper_(this,UserTimelineCol::userState)

    val temporaryIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::temporaryIds)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::applicationIds)

    val lastActionText: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol::lastActionText)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,UserTimelineCol::lastUpdateDate)

    val lastUserActionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,UserTimelineCol::lastUserActionDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserTimelineCol_<T> =
            UserTimelineCol_(this, customProperty(this, additionalPath))}
