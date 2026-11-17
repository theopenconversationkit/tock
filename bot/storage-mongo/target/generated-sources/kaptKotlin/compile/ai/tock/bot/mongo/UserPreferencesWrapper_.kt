@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import java.time.ZoneId
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Email: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
  get() = UserTimelineCol.UserPreferencesWrapper::email

private val __Encrypted: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
  get() = UserTimelineCol.UserPreferencesWrapper::encrypted

private val __FirstName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
  get() = UserTimelineCol.UserPreferencesWrapper::firstName

private val __Gender: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
  get() = UserTimelineCol.UserPreferencesWrapper::gender

private val __InitialLocale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
  get() = UserTimelineCol.UserPreferencesWrapper::initialLocale

private val __LastName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
  get() = UserTimelineCol.UserPreferencesWrapper::lastName

private val __Locale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
  get() = UserTimelineCol.UserPreferencesWrapper::locale

private val __Picture: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
  get() = UserTimelineCol.UserPreferencesWrapper::picture

private val __Test: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
  get() = UserTimelineCol.UserPreferencesWrapper::test

private val __Timezone: KProperty1<UserTimelineCol.UserPreferencesWrapper, ZoneId?>
  get() = UserTimelineCol.UserPreferencesWrapper::timezone

internal class UserPreferencesWrapper_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, UserTimelineCol.UserPreferencesWrapper?>,
) : KPropertyPath<T, UserTimelineCol.UserPreferencesWrapper?>(previous, property) {
  public val email: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Email)

  public val encrypted: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Encrypted)

  public val firstName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __FirstName)

  public val gender: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Gender)

  public val initialLocale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __InitialLocale)

  public val lastName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LastName)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val picture: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Picture)

  public val test: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Test)

  public val timezone: KPropertyPath<T, ZoneId?>
    get() = KPropertyPath(this, __Timezone)

  public companion object {
    public val Email: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
      get() = __Email

    public val Encrypted: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
      get() = __Encrypted

    public val FirstName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
      get() = __FirstName

    public val Gender: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
      get() = __Gender

    public val InitialLocale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
      get() = __InitialLocale

    public val LastName: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
      get() = __LastName

    public val Locale: KProperty1<UserTimelineCol.UserPreferencesWrapper, Locale?>
      get() = __Locale

    public val Picture: KProperty1<UserTimelineCol.UserPreferencesWrapper, String?>
      get() = __Picture

    public val Test: KProperty1<UserTimelineCol.UserPreferencesWrapper, Boolean?>
      get() = __Test

    public val Timezone: KProperty1<UserTimelineCol.UserPreferencesWrapper, ZoneId?>
      get() = __Timezone
  }
}

internal class UserPreferencesWrapper_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<UserTimelineCol.UserPreferencesWrapper?>?>,
) : KCollectionPropertyPath<T, UserTimelineCol.UserPreferencesWrapper?, UserPreferencesWrapper_<T>>(previous, property) {
  public val email: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Email)

  public val encrypted: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Encrypted)

  public val firstName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __FirstName)

  public val gender: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Gender)

  public val initialLocale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __InitialLocale)

  public val lastName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LastName)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val picture: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Picture)

  public val test: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Test)

  public val timezone: KPropertyPath<T, ZoneId?>
    get() = KPropertyPath(this, __Timezone)

  override fun memberWithAdditionalPath(additionalPath: String): UserPreferencesWrapper_<T> = UserPreferencesWrapper_(this, customProperty(this, additionalPath))
}

internal class UserPreferencesWrapper_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, UserTimelineCol.UserPreferencesWrapper?>?>,
) : KMapPropertyPath<T, K, UserTimelineCol.UserPreferencesWrapper?, UserPreferencesWrapper_<T>>(previous, property) {
  public val email: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Email)

  public val encrypted: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Encrypted)

  public val firstName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __FirstName)

  public val gender: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Gender)

  public val initialLocale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __InitialLocale)

  public val lastName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LastName)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val picture: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Picture)

  public val test: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Test)

  public val timezone: KPropertyPath<T, ZoneId?>
    get() = KPropertyPath(this, __Timezone)

  override fun memberWithAdditionalPath(additionalPath: String): UserPreferencesWrapper_<T> = UserPreferencesWrapper_(this, customProperty(this, additionalPath))
}
