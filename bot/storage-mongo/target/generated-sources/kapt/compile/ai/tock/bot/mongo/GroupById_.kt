package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Date: KProperty1<GroupById, String?>
    get() = GroupById::date
private val __DialogId: KProperty1<GroupById, String?>
    get() = GroupById::dialogId
private val __ConnectorType: KProperty1<GroupById, ConnectorType?>
    get() = GroupById::connectorType
private val __Configuration: KProperty1<GroupById, String?>
    get() = GroupById::configuration
private val __Intent: KProperty1<GroupById, String?>
    get() = GroupById::intent
private val __StoryDefinitionId: KProperty1<GroupById, String?>
    get() = GroupById::storyDefinitionId
private val __ApplicationId: KProperty1<GroupById, Id<BotApplicationConfiguration>?>
    get() = GroupById::applicationId
internal class GroupById_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, GroupById?>) :
        KPropertyPath<T, GroupById?>(previous,property) {
    val date: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Date)

    val dialogId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DialogId)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,GroupById::connectorType)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    companion object {
        val Date: KProperty1<GroupById, String?>
            get() = __Date
        val DialogId: KProperty1<GroupById, String?>
            get() = __DialogId
        val ConnectorType: ConnectorType_<GroupById>
            get() = ConnectorType_(null,__ConnectorType)
        val Configuration: KProperty1<GroupById, String?>
            get() = __Configuration
        val Intent: KProperty1<GroupById, String?>
            get() = __Intent
        val StoryDefinitionId: KProperty1<GroupById, String?>
            get() = __StoryDefinitionId
        val ApplicationId: KProperty1<GroupById, Id<BotApplicationConfiguration>?>
            get() = __ApplicationId}
}

internal class GroupById_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<GroupById>?>) : KCollectionPropertyPath<T, GroupById?,
        GroupById_<T>>(previous,property) {
    val date: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Date)

    val dialogId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DialogId)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,GroupById::connectorType)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): GroupById_<T> = GroupById_(this,
            customProperty(this, additionalPath))}

internal class GroupById_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        GroupById>?>) : KMapPropertyPath<T, K, GroupById?, GroupById_<T>>(previous,property) {
    val date: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Date)

    val dialogId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DialogId)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,GroupById::connectorType)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): GroupById_<T> = GroupById_(this,
            customProperty(this, additionalPath))}
