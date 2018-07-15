package fr.vsct.tock.bot.mongo

import java.time.ZoneId
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

internal class UserPreferencesWrapper_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, UserTimelineCol.UserPreferencesWrapper?>) : KPropertyPath<T, UserTimelineCol.UserPreferencesWrapper?>(previous,property) {
    val firstName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::firstName)

    val lastName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::lastName)

    val email: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::email)

    val timezone: KProperty1<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::timezone)

    val locale: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::locale)

    val picture: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::picture)

    val gender: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::gender)

    val test: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::test)

    val encrypted: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::encrypted)
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

internal class UserPreferencesWrapper_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<UserTimelineCol.UserPreferencesWrapper>?>) : KPropertyPath<T, Collection<UserTimelineCol.UserPreferencesWrapper>?>(previous,property) {
    val firstName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::firstName)

    val lastName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::lastName)

    val email: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::email)

    val timezone: KProperty1<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::timezone)

    val locale: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::locale)

    val picture: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::picture)

    val gender: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::gender)

    val test: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::test)

    val encrypted: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,UserTimelineCol.UserPreferencesWrapper::encrypted)
}
