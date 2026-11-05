@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApplicationId: KProperty1<GroupById, Id<BotApplicationConfiguration>?>
  get() = GroupById::applicationId

private val __Configuration: KProperty1<GroupById, String?>
  get() = GroupById::configuration

private val __ConnectorType: KProperty1<GroupById, ConnectorType?>
  get() = GroupById::connectorType

private val __Date: KProperty1<GroupById, String?>
  get() = GroupById::date

private val __DialogId: KProperty1<GroupById, String?>
  get() = GroupById::dialogId

private val __Intent: KProperty1<GroupById, String?>
  get() = GroupById::intent

private val __StoryDefinitionId: KProperty1<GroupById, String?>
  get() = GroupById::storyDefinitionId

internal class GroupById_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, GroupById?>,
) : KPropertyPath<T, GroupById?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val configuration: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Configuration)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val date: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __DialogId)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  public companion object {
    public val ApplicationId: KProperty1<GroupById, Id<BotApplicationConfiguration>?>
      get() = __ApplicationId

    public val Configuration: KProperty1<GroupById, String?>
      get() = __Configuration

    public val ConnectorType: ConnectorType_<GroupById>
      get() = ConnectorType_(null, __ConnectorType)

    public val Date: KProperty1<GroupById, String?>
      get() = __Date

    public val DialogId: KProperty1<GroupById, String?>
      get() = __DialogId

    public val Intent: KProperty1<GroupById, String?>
      get() = __Intent

    public val StoryDefinitionId: KProperty1<GroupById, String?>
      get() = __StoryDefinitionId
  }
}

internal class GroupById_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<GroupById?>?>,
) : KCollectionPropertyPath<T, GroupById?, GroupById_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val configuration: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Configuration)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val date: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __DialogId)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  override fun memberWithAdditionalPath(additionalPath: String): GroupById_<T> = GroupById_(this, customProperty(this, additionalPath))
}

internal class GroupById_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, GroupById?>?>,
) : KMapPropertyPath<T, K, GroupById?, GroupById_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val configuration: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Configuration)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val date: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __DialogId)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  override fun memberWithAdditionalPath(additionalPath: String): GroupById_<T> = GroupById_(this, customProperty(this, additionalPath))
}
