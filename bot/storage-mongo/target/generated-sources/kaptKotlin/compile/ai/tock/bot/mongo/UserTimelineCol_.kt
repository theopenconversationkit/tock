@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerId_
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.MutableSet
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<UserTimelineCol, Id<UserTimelineCol>?>
  get() = UserTimelineCol::_id

private val __ApplicationIds: KProperty1<UserTimelineCol, MutableSet<String>?>
  get() = UserTimelineCol::applicationIds

private val __CreationDate: KProperty1<UserTimelineCol, Instant?>
  get() = UserTimelineCol::creationDate

private val __LastActionText: KProperty1<UserTimelineCol, String?>
  get() = UserTimelineCol::lastActionText

private val __LastUpdateDate: KProperty1<UserTimelineCol, Instant?>
  get() = UserTimelineCol::lastUpdateDate

private val __LastUserActionDate: KProperty1<UserTimelineCol, Instant?>
  get() = UserTimelineCol::lastUserActionDate

private val __Namespace: KProperty1<UserTimelineCol, String?>
  get() = UserTimelineCol::namespace

private val __PlayerId: KProperty1<UserTimelineCol, PlayerId?>
  get() = UserTimelineCol::playerId

private val __TemporaryIds: KProperty1<UserTimelineCol, MutableSet<String>?>
  get() = UserTimelineCol::temporaryIds

private val __UserPreferences: KProperty1<UserTimelineCol, UserTimelineCol.UserPreferencesWrapper?>
  get() = UserTimelineCol::userPreferences

private val __UserState: KProperty1<UserTimelineCol, UserTimelineCol.UserStateWrapper?>
  get() = UserTimelineCol::userState

internal class UserTimelineCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, UserTimelineCol?>,
) : KPropertyPath<T, UserTimelineCol?>(previous, property) {
  public val _id: KPropertyPath<T, Id<UserTimelineCol>?>
    get() = KPropertyPath(this, ___id)

  public val applicationIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __ApplicationIds)

  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val lastActionText: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LastActionText)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val lastUserActionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUserActionDate)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val playerId: PlayerId_<T>
    get() = PlayerId_(this, __PlayerId)

  public val temporaryIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __TemporaryIds)

  public val userPreferences: UserPreferencesWrapper_<T>
    get() = UserPreferencesWrapper_(this, __UserPreferences)

  public val userState: UserStateWrapper_<T>
    get() = UserStateWrapper_(this, __UserState)

  public companion object {
    public val _id: KProperty1<UserTimelineCol, Id<UserTimelineCol>?>
      get() = ___id

    public val ApplicationIds: KCollectionSimplePropertyPath<UserTimelineCol, String?>
      get() = KCollectionSimplePropertyPath(null, __ApplicationIds)

    public val CreationDate: KProperty1<UserTimelineCol, Instant?>
      get() = __CreationDate

    public val LastActionText: KProperty1<UserTimelineCol, String?>
      get() = __LastActionText

    public val LastUpdateDate: KProperty1<UserTimelineCol, Instant?>
      get() = __LastUpdateDate

    public val LastUserActionDate: KProperty1<UserTimelineCol, Instant?>
      get() = __LastUserActionDate

    public val Namespace: KProperty1<UserTimelineCol, String?>
      get() = __Namespace

    public val PlayerId: PlayerId_<UserTimelineCol>
      get() = PlayerId_(null, __PlayerId)

    public val TemporaryIds: KCollectionSimplePropertyPath<UserTimelineCol, String?>
      get() = KCollectionSimplePropertyPath(null, __TemporaryIds)

    public val UserPreferences: UserPreferencesWrapper_<UserTimelineCol>
      get() = UserPreferencesWrapper_(null, __UserPreferences)

    public val UserState: UserStateWrapper_<UserTimelineCol>
      get() = UserStateWrapper_(null, __UserState)
  }
}

internal class UserTimelineCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<UserTimelineCol?>?>,
) : KCollectionPropertyPath<T, UserTimelineCol?, UserTimelineCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<UserTimelineCol>?>
    get() = KPropertyPath(this, ___id)

  public val applicationIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __ApplicationIds)

  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val lastActionText: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LastActionText)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val lastUserActionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUserActionDate)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val playerId: PlayerId_<T>
    get() = PlayerId_(this, __PlayerId)

  public val temporaryIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __TemporaryIds)

  public val userPreferences: UserPreferencesWrapper_<T>
    get() = UserPreferencesWrapper_(this, __UserPreferences)

  public val userState: UserStateWrapper_<T>
    get() = UserStateWrapper_(this, __UserState)

  override fun memberWithAdditionalPath(additionalPath: String): UserTimelineCol_<T> = UserTimelineCol_(this, customProperty(this, additionalPath))
}

internal class UserTimelineCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, UserTimelineCol?>?>,
) : KMapPropertyPath<T, K, UserTimelineCol?, UserTimelineCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<UserTimelineCol>?>
    get() = KPropertyPath(this, ___id)

  public val applicationIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __ApplicationIds)

  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val lastActionText: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LastActionText)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val lastUserActionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUserActionDate)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val playerId: PlayerId_<T>
    get() = PlayerId_(this, __PlayerId)

  public val temporaryIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __TemporaryIds)

  public val userPreferences: UserPreferencesWrapper_<T>
    get() = UserPreferencesWrapper_(this, __UserPreferences)

  public val userState: UserStateWrapper_<T>
    get() = UserStateWrapper_(this, __UserState)

  override fun memberWithAdditionalPath(additionalPath: String): UserTimelineCol_<T> = UserTimelineCol_(this, customProperty(this, additionalPath))
}
