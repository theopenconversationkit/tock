@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.admin.answer.AnswerConfigurationType
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<DialogFlowStateCol, Id<DialogFlowStateCol>?>
  get() = DialogFlowStateCol::_id

private val __BotId: KProperty1<DialogFlowStateCol, String?>
  get() = DialogFlowStateCol::botId

private val __Entities: KProperty1<DialogFlowStateCol, Set<String>?>
  get() = DialogFlowStateCol::entities

private val __Intent: KProperty1<DialogFlowStateCol, String?>
  get() = DialogFlowStateCol::intent

private val __Namespace: KProperty1<DialogFlowStateCol, String?>
  get() = DialogFlowStateCol::namespace

private val __Step: KProperty1<DialogFlowStateCol, String?>
  get() = DialogFlowStateCol::step

private val __StoryDefinitionId: KProperty1<DialogFlowStateCol, String?>
  get() = DialogFlowStateCol::storyDefinitionId

private val __StoryName: KProperty1<DialogFlowStateCol, String?>
  get() = DialogFlowStateCol::storyName

private val __StoryType: KProperty1<DialogFlowStateCol, AnswerConfigurationType?>
  get() = DialogFlowStateCol::storyType

internal class DialogFlowStateCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowStateCol?>,
) : KPropertyPath<T, DialogFlowStateCol?>(previous, property) {
  public val _id: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val entities: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __Entities)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  public val storyName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryName)

  public val storyType: KPropertyPath<T, AnswerConfigurationType?>
    get() = KPropertyPath(this, __StoryType)

  public companion object {
    public val _id: KProperty1<DialogFlowStateCol, Id<DialogFlowStateCol>?>
      get() = ___id

    public val BotId: KProperty1<DialogFlowStateCol, String?>
      get() = __BotId

    public val Entities: KCollectionSimplePropertyPath<DialogFlowStateCol, String?>
      get() = KCollectionSimplePropertyPath(null, __Entities)

    public val Intent: KProperty1<DialogFlowStateCol, String?>
      get() = __Intent

    public val Namespace: KProperty1<DialogFlowStateCol, String?>
      get() = __Namespace

    public val Step: KProperty1<DialogFlowStateCol, String?>
      get() = __Step

    public val StoryDefinitionId: KProperty1<DialogFlowStateCol, String?>
      get() = __StoryDefinitionId

    public val StoryName: KProperty1<DialogFlowStateCol, String?>
      get() = __StoryName

    public val StoryType: KProperty1<DialogFlowStateCol, AnswerConfigurationType?>
      get() = __StoryType
  }
}

internal class DialogFlowStateCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowStateCol?>?>,
) : KCollectionPropertyPath<T, DialogFlowStateCol?, DialogFlowStateCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val entities: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __Entities)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  public val storyName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryName)

  public val storyType: KPropertyPath<T, AnswerConfigurationType?>
    get() = KPropertyPath(this, __StoryType)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateCol_<T> = DialogFlowStateCol_(this, customProperty(this, additionalPath))
}

internal class DialogFlowStateCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowStateCol?>?>,
) : KMapPropertyPath<T, K, DialogFlowStateCol?, DialogFlowStateCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val entities: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __Entities)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  public val storyName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryName)

  public val storyType: KPropertyPath<T, AnswerConfigurationType?>
    get() = KPropertyPath(this, __StoryType)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateCol_<T> = DialogFlowStateCol_(this, customProperty(this, additionalPath))
}
