package ai.tock.bot.mongo

import java.time.ZoneId
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __FirstName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
    get() = UserTimelineCol.UserPreferencesWrapper::firstName
private val __LastName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
    get() = UserTimelineCol.UserPreferencesWrapper::lastName
private val __Email: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
    get() = UserTimelineCol.UserPreferencesWrapper::email
private val __Timezone: KProperty1<UserTimelineCol.UserPreferencesWrapper, ZoneId?>
    get() = UserTimelineCol.UserPreferencesWrapper::timezone
private val __Locale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
    get() = UserTimelineCol.UserPreferencesWrapper::locale
private val __Picture: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
    get() = UserTimelineCol.UserPreferencesWrapper::picture
private val __Gender: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
    get() = UserTimelineCol.UserPreferencesWrapper::gender
private val __InitialLocale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
    get() = UserTimelineCol.UserPreferencesWrapper::initialLocale
private val __Test: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
    get() = UserTimelineCol.UserPreferencesWrapper::test
private val __Encrypted: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
    get() = UserTimelineCol.UserPreferencesWrapper::encrypted
internal class UserPreferencesWrapper_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        UserTimelineCol.UserPreferencesWrapper?>) : KPropertyPath<T,
        UserTimelineCol.UserPreferencesWrapper?>(previous,property) {
    val firstName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__FirstName)

    val lastName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__LastName)

    val email: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Email)

    val timezone: KPropertyPath<T, ZoneId?>
        get() = KPropertyPath(this,__Timezone)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val picture: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Picture)

    val gender: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Gender)

    val initialLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__InitialLocale)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val encrypted: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Encrypted)

    companion object {
        val FirstName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = __FirstName
        val LastName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = __LastName
        val Email: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = __Email
        val Timezone: KProperty1<UserTimelineCol.UserPreferencesWrapper, ZoneId?>
            get() = __Timezone
        val Locale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
            get() = __Locale
        val Picture: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = __Picture
        val Gender: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = __Gender
        val InitialLocale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
            get() = __InitialLocale
        val Test: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
            get() = __Test
        val Encrypted: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
            get() = __Encrypted}
}

internal class UserPreferencesWrapper_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<UserTimelineCol.UserPreferencesWrapper>?>) : KCollectionPropertyPath<T,
        UserTimelineCol.UserPreferencesWrapper?, UserPreferencesWrapper_<T>>(previous,property) {
    val firstName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__FirstName)

    val lastName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__LastName)

    val email: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Email)

    val timezone: KPropertyPath<T, ZoneId?>
        get() = KPropertyPath(this,__Timezone)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val picture: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Picture)

    val gender: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Gender)

    val initialLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__InitialLocale)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val encrypted: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Encrypted)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserPreferencesWrapper_<T> =
            UserPreferencesWrapper_(this, customProperty(this, additionalPath))}

internal class UserPreferencesWrapper_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, UserTimelineCol.UserPreferencesWrapper>?>) : KMapPropertyPath<T, K,
        UserTimelineCol.UserPreferencesWrapper?, UserPreferencesWrapper_<T>>(previous,property) {
    val firstName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__FirstName)

    val lastName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__LastName)

    val email: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Email)

    val timezone: KPropertyPath<T, ZoneId?>
        get() = KPropertyPath(this,__Timezone)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val picture: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Picture)

    val gender: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Gender)

    val initialLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__InitialLocale)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val encrypted: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Encrypted)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserPreferencesWrapper_<T> =
            UserPreferencesWrapper_(this, customProperty(this, additionalPath))}
