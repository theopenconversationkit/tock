@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import java.time.ZonedDateTime
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<Feature, String?>
  get() = Feature::_id

private val __BotId: KProperty1<Feature, String?>
  get() = Feature::botId

private val __Enabled: KProperty1<Feature, Boolean?>
  get() = Feature::enabled

private val __EndDate: KProperty1<Feature, ZonedDateTime?>
  get() = Feature::endDate

private val __Graduation: KProperty1<Feature, Int?>
  get() = Feature::graduation

private val __Key: KProperty1<Feature, String?>
  get() = Feature::key

private val __Namespace: KProperty1<Feature, String?>
  get() = Feature::namespace

private val __StartDate: KProperty1<Feature, ZonedDateTime?>
  get() = Feature::startDate

internal class Feature_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Feature?>,
) : KPropertyPath<T, Feature?>(previous, property) {
  public val _id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val enabled: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Enabled)

  public val endDate: KPropertyPath<T, ZonedDateTime?>
    get() = KPropertyPath(this, __EndDate)

  public val graduation: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Graduation)

  public val key: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Key)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val startDate: KPropertyPath<T, ZonedDateTime?>
    get() = KPropertyPath(this, __StartDate)

  public companion object {
    public val _id: KProperty1<Feature, String?>
      get() = ___id

    public val BotId: KProperty1<Feature, String?>
      get() = __BotId

    public val Enabled: KProperty1<Feature, Boolean?>
      get() = __Enabled

    public val EndDate: KProperty1<Feature, ZonedDateTime?>
      get() = __EndDate

    public val Graduation: KProperty1<Feature, Int?>
      get() = __Graduation

    public val Key: KProperty1<Feature, String?>
      get() = __Key

    public val Namespace: KProperty1<Feature, String?>
      get() = __Namespace

    public val StartDate: KProperty1<Feature, ZonedDateTime?>
      get() = __StartDate
  }
}

internal class Feature_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<Feature?>?>,
) : KCollectionPropertyPath<T, Feature?, Feature_<T>>(previous, property) {
  public val _id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val enabled: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Enabled)

  public val endDate: KPropertyPath<T, ZonedDateTime?>
    get() = KPropertyPath(this, __EndDate)

  public val graduation: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Graduation)

  public val key: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Key)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val startDate: KPropertyPath<T, ZonedDateTime?>
    get() = KPropertyPath(this, __StartDate)

  override fun memberWithAdditionalPath(additionalPath: String): Feature_<T> = Feature_(this, customProperty(this, additionalPath))
}

internal class Feature_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, Feature?>?>,
) : KMapPropertyPath<T, K, Feature?, Feature_<T>>(previous, property) {
  public val _id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val enabled: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Enabled)

  public val endDate: KPropertyPath<T, ZonedDateTime?>
    get() = KPropertyPath(this, __EndDate)

  public val graduation: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Graduation)

  public val key: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Key)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val startDate: KPropertyPath<T, ZonedDateTime?>
    get() = KPropertyPath(this, __StartDate)

  override fun memberWithAdditionalPath(additionalPath: String): Feature_<T> = Feature_(this, customProperty(this, additionalPath))
}
