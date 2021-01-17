package ai.tock.bot.mongo

import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerId_
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val ___id: KProperty1<UserTimelineCol, Id<UserTimelineCol>?>
    get() = UserTimelineCol::_id
private val __PlayerId: KProperty1<UserTimelineCol, PlayerId?>
    get() = UserTimelineCol::playerId
private val __UserPreferences: KProperty1<UserTimelineCol, UserTimelineCol.UserPreferencesWrapper?>
    get() = UserTimelineCol::userPreferences
private val __UserState: KProperty1<UserTimelineCol, UserTimelineCol.UserStateWrapper?>
    get() = UserTimelineCol::userState
private val __TemporaryIds: KProperty1<UserTimelineCol, Set<String>?>
    get() = UserTimelineCol::temporaryIds
private val __ApplicationIds: KProperty1<UserTimelineCol, Set<String>?>
    get() = UserTimelineCol::applicationIds
private val __LastActionText: KProperty1<UserTimelineCol, String?>
    get() = UserTimelineCol::lastActionText
private val __LastUpdateDate: KProperty1<UserTimelineCol, Instant?>
    get() = UserTimelineCol::lastUpdateDate
private val __LastUserActionDate: KProperty1<UserTimelineCol, Instant?>
    get() = UserTimelineCol::lastUserActionDate
private val __Namespace: KProperty1<UserTimelineCol, String?>
    get() = UserTimelineCol::namespace
private val __CreationDate: KProperty1<UserTimelineCol, Instant?>
    get() = UserTimelineCol::creationDate
internal class UserTimelineCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        UserTimelineCol?>) : KPropertyPath<T, UserTimelineCol?>(previous,property) {
    val _id: KPropertyPath<T, Id<UserTimelineCol>?>
        get() = KPropertyPath(this,___id)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,UserTimelineCol::playerId)

    val userPreferences: UserPreferencesWrapper_<T>
        get() = UserPreferencesWrapper_(this,UserTimelineCol::userPreferences)

    val userState: UserStateWrapper_<T>
        get() = UserStateWrapper_(this,UserTimelineCol::userState)

    val temporaryIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,UserTimelineCol::temporaryIds)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,UserTimelineCol::applicationIds)

    val lastActionText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__LastActionText)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val lastUserActionDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUserActionDate)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    companion object {
        val _id: KProperty1<UserTimelineCol, Id<UserTimelineCol>?>
            get() = ___id
        val PlayerId: PlayerId_<UserTimelineCol>
            get() = PlayerId_(null,__PlayerId)
        val UserPreferences: UserPreferencesWrapper_<UserTimelineCol>
            get() = UserPreferencesWrapper_(null,__UserPreferences)
        val UserState: UserStateWrapper_<UserTimelineCol>
            get() = UserStateWrapper_(null,__UserState)
        val TemporaryIds: KCollectionSimplePropertyPath<UserTimelineCol, String?>
            get() = KCollectionSimplePropertyPath(null, __TemporaryIds)
        val ApplicationIds: KCollectionSimplePropertyPath<UserTimelineCol, String?>
            get() = KCollectionSimplePropertyPath(null, __ApplicationIds)
        val LastActionText: KProperty1<UserTimelineCol, String?>
            get() = __LastActionText
        val LastUpdateDate: KProperty1<UserTimelineCol, Instant?>
            get() = __LastUpdateDate
        val LastUserActionDate: KProperty1<UserTimelineCol, Instant?>
            get() = __LastUserActionDate
        val Namespace: KProperty1<UserTimelineCol, String?>
            get() = __Namespace
        val CreationDate: KProperty1<UserTimelineCol, Instant?>
            get() = __CreationDate}
}

internal class UserTimelineCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<UserTimelineCol>?>) : KCollectionPropertyPath<T, UserTimelineCol?,
        UserTimelineCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<UserTimelineCol>?>
        get() = KPropertyPath(this,___id)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,UserTimelineCol::playerId)

    val userPreferences: UserPreferencesWrapper_<T>
        get() = UserPreferencesWrapper_(this,UserTimelineCol::userPreferences)

    val userState: UserStateWrapper_<T>
        get() = UserStateWrapper_(this,UserTimelineCol::userState)

    val temporaryIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,UserTimelineCol::temporaryIds)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,UserTimelineCol::applicationIds)

    val lastActionText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__LastActionText)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val lastUserActionDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUserActionDate)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserTimelineCol_<T> =
            UserTimelineCol_(this, customProperty(this, additionalPath))}

internal class UserTimelineCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, UserTimelineCol>?>) : KMapPropertyPath<T, K, UserTimelineCol?,
        UserTimelineCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<UserTimelineCol>?>
        get() = KPropertyPath(this,___id)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,UserTimelineCol::playerId)

    val userPreferences: UserPreferencesWrapper_<T>
        get() = UserPreferencesWrapper_(this,UserTimelineCol::userPreferences)

    val userState: UserStateWrapper_<T>
        get() = UserStateWrapper_(this,UserTimelineCol::userState)

    val temporaryIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,UserTimelineCol::temporaryIds)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,UserTimelineCol::applicationIds)

    val lastActionText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__LastActionText)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val lastUserActionDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUserActionDate)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserTimelineCol_<T> =
            UserTimelineCol_(this, customProperty(this, additionalPath))}
