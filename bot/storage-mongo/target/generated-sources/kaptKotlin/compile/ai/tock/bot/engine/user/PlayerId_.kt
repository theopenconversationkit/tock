@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.engine.user

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ClientId: KProperty1<PlayerId, String?>
  get() = PlayerId::clientId

private val __Id: KProperty1<PlayerId, String?>
  get() = PlayerId::id

private val __Type: KProperty1<PlayerId, PlayerType?>
  get() = PlayerId::type

public class PlayerId_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, PlayerId?>,
) : KPropertyPath<T, PlayerId?>(previous, property) {
  public val clientId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ClientId)

  public val id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Id)

  public val type: KPropertyPath<T, PlayerType?>
    get() = KPropertyPath(this, __Type)

  public companion object {
    public val ClientId: KProperty1<PlayerId, String?>
      get() = __ClientId

    public val Id: KProperty1<PlayerId, String?>
      get() = __Id

    public val Type: KProperty1<PlayerId, PlayerType?>
      get() = __Type
  }
}

public class PlayerId_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<PlayerId?>?>,
) : KCollectionPropertyPath<T, PlayerId?, PlayerId_<T>>(previous, property) {
  public val clientId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ClientId)

  public val id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Id)

  public val type: KPropertyPath<T, PlayerType?>
    get() = KPropertyPath(this, __Type)

  override fun memberWithAdditionalPath(additionalPath: String): PlayerId_<T> = PlayerId_(this, customProperty(this, additionalPath))
}

public class PlayerId_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, PlayerId?>?>,
) : KMapPropertyPath<T, K, PlayerId?, PlayerId_<T>>(previous, property) {
  public val clientId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ClientId)

  public val id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Id)

  public val type: KPropertyPath<T, PlayerType?>
    get() = KPropertyPath(this, __Type)

  override fun memberWithAdditionalPath(additionalPath: String): PlayerId_<T> = PlayerId_(this, customProperty(this, additionalPath))
}
