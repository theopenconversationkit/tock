package fr.vsct.tock.bot.mongo

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

internal class UserPreferencesWrapper_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        UserTimelineCol.UserPreferencesWrapper?>) : KPropertyPath<T,
        UserTimelineCol.UserPreferencesWrapper?>(previous,property) {
    val firstName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::firstName)

    val lastName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::lastName)

    val email: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::email)

    val timezone: KPropertyPath<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.ZoneId?>(this,UserTimelineCol.UserPreferencesWrapper::timezone)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,UserTimelineCol.UserPreferencesWrapper::locale)

    val picture: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::picture)

    val gender: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::gender)

    val test: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Boolean?>(this,UserTimelineCol.UserPreferencesWrapper::test)

    val encrypted: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Boolean?>(this,UserTimelineCol.UserPreferencesWrapper::encrypted)

    companion object {
        val FirstName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = UserTimelineCol.UserPreferencesWrapper::firstName
        val LastName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = UserTimelineCol.UserPreferencesWrapper::lastName
        val Email: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = UserTimelineCol.UserPreferencesWrapper::email
        val Timezone: KProperty1<UserTimelineCol.UserPreferencesWrapper, ZoneId?>
            get() = UserTimelineCol.UserPreferencesWrapper::timezone
        val Locale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
            get() = UserTimelineCol.UserPreferencesWrapper::locale
        val Picture: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = UserTimelineCol.UserPreferencesWrapper::picture
        val Gender: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
            get() = UserTimelineCol.UserPreferencesWrapper::gender
        val Test: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
            get() = UserTimelineCol.UserPreferencesWrapper::test
        val Encrypted: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
            get() = UserTimelineCol.UserPreferencesWrapper::encrypted}
}

internal class UserPreferencesWrapper_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<UserTimelineCol.UserPreferencesWrapper>?>) : KCollectionPropertyPath<T,
        UserTimelineCol.UserPreferencesWrapper?, UserPreferencesWrapper_<T>>(previous,property) {
    val firstName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::firstName)

    val lastName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::lastName)

    val email: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::email)

    val timezone: KPropertyPath<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.ZoneId?>(this,UserTimelineCol.UserPreferencesWrapper::timezone)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,UserTimelineCol.UserPreferencesWrapper::locale)

    val picture: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::picture)

    val gender: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::gender)

    val test: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Boolean?>(this,UserTimelineCol.UserPreferencesWrapper::test)

    val encrypted: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Boolean?>(this,UserTimelineCol.UserPreferencesWrapper::encrypted)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserPreferencesWrapper_<T> =
            UserPreferencesWrapper_(this, customProperty(this, additionalPath))}

internal class UserPreferencesWrapper_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, UserTimelineCol.UserPreferencesWrapper>?>) : KMapPropertyPath<T, K,
        UserTimelineCol.UserPreferencesWrapper?, UserPreferencesWrapper_<T>>(previous,property) {
    val firstName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::firstName)

    val lastName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::lastName)

    val email: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::email)

    val timezone: KPropertyPath<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.ZoneId?>(this,UserTimelineCol.UserPreferencesWrapper::timezone)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,UserTimelineCol.UserPreferencesWrapper::locale)

    val picture: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::picture)

    val gender: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,UserTimelineCol.UserPreferencesWrapper::gender)

    val test: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Boolean?>(this,UserTimelineCol.UserPreferencesWrapper::test)

    val encrypted: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Boolean?>(this,UserTimelineCol.UserPreferencesWrapper::encrypted)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserPreferencesWrapper_<T> =
            UserPreferencesWrapper_(this, customProperty(this, additionalPath))}
