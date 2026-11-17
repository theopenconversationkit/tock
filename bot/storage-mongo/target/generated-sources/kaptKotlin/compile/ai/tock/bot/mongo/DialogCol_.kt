@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerId_Col
import java.time.Instant
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<DialogCol, Id<Dialog>?>
  get() = DialogCol::_id

private val __ApplicationIds: KProperty1<DialogCol, Set<String>?>
  get() = DialogCol::applicationIds

private val __GroupId: KProperty1<DialogCol, String?>
  get() = DialogCol::groupId

private val __LastUpdateDate: KProperty1<DialogCol, Instant?>
  get() = DialogCol::lastUpdateDate

private val __Namespace: KProperty1<DialogCol, String?>
  get() = DialogCol::namespace

private val __PlayerIds: KProperty1<DialogCol, Set<PlayerId>?>
  get() = DialogCol::playerIds

private val __Rating: KProperty1<DialogCol, Int?>
  get() = DialogCol::rating

private val __Review: KProperty1<DialogCol, String?>
  get() = DialogCol::review

private val __State: KProperty1<DialogCol, DialogCol.DialogStateMongoWrapper?>
  get() = DialogCol::state

private val __Stories: KProperty1<DialogCol, List<DialogCol.StoryMongoWrapper>?>
  get() = DialogCol::stories

private val __Test: KProperty1<DialogCol, Boolean?>
  get() = DialogCol::test

internal class DialogCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogCol?>,
) : KPropertyPath<T, DialogCol?>(previous, property) {
  public val _id: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, ___id)

  public val applicationIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __ApplicationIds)

  public val groupId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __GroupId)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val playerIds: PlayerId_Col<T>
    get() = PlayerId_Col(this, __PlayerIds)

  public val rating: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Rating)

  public val review: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Review)

  public val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
    get() = KPropertyPath(this, __State)

  public val stories: StoryMongoWrapper_Col<T>
    get() = StoryMongoWrapper_Col(this, __Stories)

  public val test: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Test)

  public companion object {
    public val _id: KProperty1<DialogCol, Id<Dialog>?>
      get() = ___id

    public val ApplicationIds: KCollectionSimplePropertyPath<DialogCol, String?>
      get() = KCollectionSimplePropertyPath(null, __ApplicationIds)

    public val GroupId: KProperty1<DialogCol, String?>
      get() = __GroupId

    public val LastUpdateDate: KProperty1<DialogCol, Instant?>
      get() = __LastUpdateDate

    public val Namespace: KProperty1<DialogCol, String?>
      get() = __Namespace

    public val PlayerIds: PlayerId_Col<DialogCol>
      get() = PlayerId_Col(null, __PlayerIds)

    public val Rating: KProperty1<DialogCol, Int?>
      get() = __Rating

    public val Review: KProperty1<DialogCol, String?>
      get() = __Review

    public val State: KProperty1<DialogCol, DialogCol.DialogStateMongoWrapper?>
      get() = __State

    public val Stories: StoryMongoWrapper_Col<DialogCol>
      get() = StoryMongoWrapper_Col(null, __Stories)

    public val Test: KProperty1<DialogCol, Boolean?>
      get() = __Test
  }
}

internal class DialogCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogCol?>?>,
) : KCollectionPropertyPath<T, DialogCol?, DialogCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, ___id)

  public val applicationIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __ApplicationIds)

  public val groupId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __GroupId)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val playerIds: PlayerId_Col<T>
    get() = PlayerId_Col(this, __PlayerIds)

  public val rating: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Rating)

  public val review: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Review)

  public val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
    get() = KPropertyPath(this, __State)

  public val stories: StoryMongoWrapper_Col<T>
    get() = StoryMongoWrapper_Col(this, __Stories)

  public val test: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Test)

  override fun memberWithAdditionalPath(additionalPath: String): DialogCol_<T> = DialogCol_(this, customProperty(this, additionalPath))
}

internal class DialogCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogCol?>?>,
) : KMapPropertyPath<T, K, DialogCol?, DialogCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, ___id)

  public val applicationIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __ApplicationIds)

  public val groupId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __GroupId)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val playerIds: PlayerId_Col<T>
    get() = PlayerId_Col(this, __PlayerIds)

  public val rating: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Rating)

  public val review: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Review)

  public val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
    get() = KPropertyPath(this, __State)

  public val stories: StoryMongoWrapper_Col<T>
    get() = StoryMongoWrapper_Col(this, __Stories)

  public val test: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Test)

  override fun memberWithAdditionalPath(additionalPath: String): DialogCol_<T> = DialogCol_(this, customProperty(this, additionalPath))
}
