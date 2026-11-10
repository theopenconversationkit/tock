@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KMapSimplePropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __CreationDate: KProperty1<UserTimelineCol.UserStateWrapper, Instant?>
  get() = UserTimelineCol.UserStateWrapper::creationDate

private val __Flags:
    KProperty1<UserTimelineCol.UserStateWrapper, Map<String, UserTimelineCol.TimeBoxedFlagWrapper>?>
  get() = UserTimelineCol.UserStateWrapper::flags

private val __LastUpdateDate: KProperty1<UserTimelineCol.UserStateWrapper, Instant?>
  get() = UserTimelineCol.UserStateWrapper::lastUpdateDate

internal class UserStateWrapper_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, UserTimelineCol.UserStateWrapper?>,
) : KPropertyPath<T, UserTimelineCol.UserStateWrapper?>(previous, property) {
  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val flags: KMapSimplePropertyPath<T, String?, UserTimelineCol.TimeBoxedFlagWrapper?>
    get() = KMapSimplePropertyPath(this, __Flags)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public companion object {
    public val CreationDate: KProperty1<UserTimelineCol.UserStateWrapper, Instant?>
      get() = __CreationDate

    public val Flags:
        KMapSimplePropertyPath<UserTimelineCol.UserStateWrapper, String?, UserTimelineCol.TimeBoxedFlagWrapper?>
      get() = KMapSimplePropertyPath(null, __Flags)

    public val LastUpdateDate: KProperty1<UserTimelineCol.UserStateWrapper, Instant?>
      get() = __LastUpdateDate
  }
}

internal class UserStateWrapper_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<UserTimelineCol.UserStateWrapper?>?>,
) : KCollectionPropertyPath<T, UserTimelineCol.UserStateWrapper?, UserStateWrapper_<T>>(previous, property) {
  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val flags: KMapSimplePropertyPath<T, String?, UserTimelineCol.TimeBoxedFlagWrapper?>
    get() = KMapSimplePropertyPath(this, __Flags)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  override fun memberWithAdditionalPath(additionalPath: String): UserStateWrapper_<T> = UserStateWrapper_(this, customProperty(this, additionalPath))
}

internal class UserStateWrapper_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, UserTimelineCol.UserStateWrapper?>?>,
) : KMapPropertyPath<T, K, UserTimelineCol.UserStateWrapper?, UserStateWrapper_<T>>(previous, property) {
  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val flags: KMapSimplePropertyPath<T, String?, UserTimelineCol.TimeBoxedFlagWrapper?>
    get() = KMapSimplePropertyPath(this, __Flags)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  override fun memberWithAdditionalPath(additionalPath: String): UserStateWrapper_<T> = UserStateWrapper_(this, customProperty(this, additionalPath))
}
